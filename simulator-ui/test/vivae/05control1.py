# Control one agent to run the maze.
#
# by Jaroslav Vitku

"""
Notes about agents: (see screenshot to this script)

Friction sensor [dots]
    -measures [binary] presence of ANY OBJECT (including grass)
    -black and blue lines gere
    -road=0, everything other=1

Distance sensor [lines]
    -measures [continuous] distance to the nearest OBSTACLE (not grass, not road)

Note: value 0 to sensor properties should add 0 sendors now
"""


import nef
import time
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl.vivae import VivaeNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils
from nengoros.modules.impl.vivae.impl import SimulationControls as Controls
import simplemodule

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Vivae - hardwired control for agent')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

################################## 
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
RosUtils.prefferJroscore(False) # Turlte prefers roscore before jroscore (don't know why..) 

################################## 
################# define the group and start it
modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem";   # custom modem here
server = "vivae.ros.simulatorControlsServer.ControlsServer"        # call Vivae as a thread in Java from this process
# Call Vivae as an external process
#server = ["./sb/../../../../simulators/vivae/build/install/vivae/bin/vivae","vivae.ros.simulatorControlsServer.ControlsServer"]


def initVivae(modem,server):
    # create group of nodes
    g = NodeGroup("vivae", True);               # create default group of nodes
    g.addNC(server, "vivaeSimulator", "java");   # run the simulator..
    g.addNC(modem,"modem","modem")              # add default modem..
    g.startGroup()                              # start group normally

    modem = g.getModem()
    #time.sleep(3)    # if the process is native, it takes longer time to init the services !!                 
    simulator = NeuralModule('VivaeTest',modem)  # create NeuralModule which is able to add/remove agents

    vivae = simulator.getControls();     # this starts the control services..
    vivae.setVisible(True);              # make simulation window visible..
    many=net.add(simulator)                 # add it to the Nengo network

    vivae.loadMap('data/scenarios/test/walls.svg')  

    #addAgent(name,numSensors, maxDistance, frictionSensor) 
    vivae.addAgent('a',8,    120          ,0)
    vivae.start()
    return simulator;
    
class Controller(simplemodule.SimpleModule):

    # This "constructor" is called before each simulation
    def init(self,inputdims,outputdims,numpars):

        self.inputdims=inputdims;    # no of agents sensors (2x distance [used] 2xfriction [unused] for simplicity)
        self.outputdims=outputdims;  # actuators - wheel speeds (left and right)
        self.numpars=numpars;
        self.speed = 0.1;
        self.c = 0.3;
        self.brake = -0.5;
        # init all variables!
        self.output = range(self.outputdims)
        self.breakedsteps = 0;
        self.maxsteps=50;
        self.waitbreaking=100;
        self.lastbreaked=0;
        self.time = 0;

    # data from agent are: [left distance, right distance, left friction, right friction]    
    def termination_inputs(self,values):
        #self.time = self.time+1;
        #self.output = [self.speed+self.c*values[0], self.speed+self.c*values[1]]; # more close to the wall => smaller speed on the opposite wheel
        self.output = [self.speed+self.c*values[1], self.speed+self.c*values[2]]; # more close to the wall => smaller speed on the opposite wheel
        """
        if((values[0] or values[1]) > 0):
            if ( (self.breakedsteps<self.maxsteps) and ((self.lastbreaked-self.time)>self.waitbreaking) ):            
                self.lastbreaked=self.time
                if values[0]==0:
                    self.output[0] = self.brake;
                    self.breakedsteps=self.breakedsteps+1;
                if values[1]==0:
                    self.output[1] = self.brake;
                    self.breakedsteps=self.breakedsteps+1;
        else:
            self.breakedsteps=0;
        """
    def origin_outputs(self):
        return self.output
    
#################################################################################
simulator = initVivae(modem,server);    # build simulator and access its controls
vivae = simulator.getControls();

#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input=FunctionInput('Randomized input', [FourierFunction(.5, 10, 6, 12),
    FourierFunction(2, 11, 5, 17)],
    Units.UNK) 

controller = net.add(Controller('Agent controller',8,2,0));

net.connect(simulator.getAgent('a').getOrigin(),controller.getTermination('inputs'))
net.connect(controller.getOrigin('outputs'), simulator.getAgent('a').getTermination())

# Add the input node to the network and connect it to the smart enuron
net.add(input)  
# make neural network and connect it to the smart neuron 
#A=net.make('PositionData',neurons=10,dimensions=2,radius=20)

#net.connect(input,simulator.getAgent('a').getTermination())
#net.connect(simulator.getAgent('a').getOrigin(), A)

print "OK, configuration done. Simulating.."

t=0.5;
dt=0.001;

net.reset()
net.run(t,dt)

print "Simulaiton done, calling reset.."

vivae.reset()
net.reset()
net.view()
