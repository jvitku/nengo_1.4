# Control some agent(s)?
#
# by Jaroslav Vitku


import nef
import time
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl.vivae import VivaeNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils
from nengoros.modules.impl.vivae.impl import SimulationControls as Controls
#from vivae.ros import Simulator as Simulator

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Vivae - one agent')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

################################## 
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
RosUtils.prefferJroscore(False) # Turlte prefers roscore before jroscore (don't know why..) 

################################## 
################# define the group and start it
modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; # custom modem here
# this is launch command which calls the RosRun with name of ControlsServer as parameter
#vvv = ["./sb/../../../../simulators/vivae/build/install/vivae/bin/vivae","vivae.ros.simulatorControlsServer.ControlsServer"]


vvv = "vivae.ros.simulatorControlsServer.ControlsServer"         # actual Vivae simulator, provies services

# create group of nodes
g = NodeGroup("vivae", True);               # create default group of nodes
g.addNC(vvv, "vivaeSimulator", "java");   # run the simulator..
g.addNC(modem,"modem","modem")              # add default modem..
g.startGroup()                              # start group normally

################################## 
modem = g.getModem()
time.sleep(3)    # if the process is native, it takes longer time to init the services !!                 
simulator = NeuralModule('VivaeTest',modem)  # create NeuralModule which is able to add/remove agents

Controls = simulator.getControls();     # this starts the control services..
Controls.setVisible(True);
many=net.add(simulator)                 # add it to the Nengo network


#Controls.loadMap('data/scenarios/test/arena2.svg')  
Controls.loadMap('data/scenarios/test/oneagent.svg')  
#Controls.loadMap('data/scenarios/test/walls.svg')  

#Controls.loadMap('data/scenarios/ushape.svg')
#Controls.loadMap('data/scenarios/arena1.svg') 
#Controls.loadMap('data/scenarios/manyAgents.svg') 


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

#addAgent(name,numSensors, maxDistance, frictionSensor) 
Controls.addAgent('a',4,    70          ,0)

Controls.start()

#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input=FunctionInput('Randomized input', [FourierFunction(.5, 10, 6, 12),
    FourierFunction(2, 11, 5, 17)],
    Units.UNK) 

# Add the input node to the network and connect it to the smart enuron
net.add(input)  

# make neural network and connect it to the smart neuron 
A=net.make('PositionData',neurons=10,dimensions=2,radius=20)

net.connect(input,simulator.getAgent('a').getTermination())
net.connect(simulator.getAgent('a').getOrigin(), A)

print "OK, configuration done. Simulating.."

t=0.5;
dt=0.001;

net.reset()
net.run(t,dt)

print "Simulaiton done, calling reset.."

Controls.reset()
net.reset()
net.view()
