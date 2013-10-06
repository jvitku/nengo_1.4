# Agent which is controlled by proportional controller and navigates through the maze. 
# Noise is added into the control signal by use of ANN.
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
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (true by default)

# initializes the simulator
def initVivae(numsensors):
    modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem";   # custom modem here
    server = "vivae.ros.simulatorControlsServer.ControlsServer"        # call Vivae as a thread in Java from this process
    # Call Vivae as an external process
    #server = ["./sb/../../../../simulators/vivae/build/install/vivae/bin/vivae","vivae.ros.simulatorControlsServer.ControlsServer"]

    # create group of nodes
    g = NodeGroup("vivae", True);               # create default group of nodes
    g.addNC(server, "vivaeSimulator", "java");   # run the simulator..
    g.addNC(modem,"modem","modem")              # add default modem..
    g.startGroup()                              # start group normally

    modem = g.getModem()
    #time.sleep(3)    # if the process is native, it takes longer time to init the services !!                 
    simulator = NeuralModule('VivaeSimulator',modem)  # create NeuralModule which is able to add/remove agents

    vivae = simulator.getControls();     # this starts the control services..
    vivae.setVisible(True);              # make simulation window visible..
    many=net.add(simulator)                 # add it to the Nengo network

    vivae.loadMap('data/scenarios/test/wallsII.svg')  

    #addAgent(name,numSensors, maxDistance, frictionSensor)  (note that you will actually get numSensors+1 floats +(speed))
    vivae.addAgent('a',2*numsensors,    120          ,0)
    vivae.start()
    return simulator;

# controller mixes sinusoid with actual closed-loop signal
class Controller(simplemodule.SimpleModule):
    def init(self,inputdims,outputdims,numpars):

        self.inputdims=inputdims;    # no of agents sensors (2x distance [used] 2xfriction [unused] for simplicity)
        self.outputdims=outputdims;  # actuators - wheel speeds (left and right)
        self.numpars=numpars;
        self.output = range(self.outputdims)
        
        self.k = 0.12;               # regulator parameters
        self.p = 0.25;
        self.pi = 0.04;
        
        self.prevL = 0;
        self.prevR = 0;
        
    # data from agent are: [[distance sensors from left to right], [friction sensors from left to right], [speed]]
    def termination_inputs(self,values):
        self.t = self.t+1;
        self.prevL = self.prevL/self.t + values[1];         # "integrate"
        self.prevR = self.prevR/self.t + values[2];
        
        I = [self.pi*self.prevL, self.pi*self.prevR];
        P = [self.p*values[1], self.p*values[2]];       
        
        self.output = [P[0]+I[0]+self.k, P[1]+I[1]+self.k]; # add forward speed 
        
        #print 'speed '+repr(values[8])
        
    def origin_outputs(self):
        return self.output
    
# stores data into csv files each time step
class DataSaver(nef.SimpleNode):
    def tick(self):

        i=file('data/sensoryData.csv','a+')  # data from robot sensors
        o=file('data/actuatorData.csv','a+') # data comming from regulator to robot actuators

        sensorydata = simulator.getAgent('a').getOrigin().getValues().getValues(); # columns: time, [distance sensors from the left, friction sensors from the left]
        actuatordata = controller.getOrigin('outputs').getValues().getValues();    # columns: time, left_wheel, right_wheel

        i.write('%1.3f,%s\n'%(self.t,list(sensorydata)))
        o.write('%1.3f,%s\n'%(self.t,list(actuatordata)))
        i.close()
        o.close()
        
#################################################################################
net=nef.Network('Vivae - PI controller for agent')
net.add_to_nengo()  

numsensors=4                                # number of agents sensors (if changed, need to read correct values in the Controller)
simulator = initVivae(numsensors);    # build simulator and access its controls
vivae = simulator.getControls();

controller = net.add(Controller('Agent controller',2*numsensors+1,2,0)); # build controller

net.connect(simulator.getAgent('a').getOrigin(),controller.getTermination('inputs'))    # connect agent with controller
net.connect(controller.getOrigin('outputs'), simulator.getAgent('a').getTermination())  # connect controller to the agent


#saver = net.add(DataSaver('saver')) # save data ??

t=2;
dt=0.001;
print "OK, configuration done. Simulating network for "+repr(t)+" seconds"
#net.run(t,dt)

print "Simulation done, will reset and show the interactive simulation window."
net.view()
