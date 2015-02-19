# This demo shows how to:
#   -launch vivae simulator
#   -select environment map
#   -request to spawn 3 agents with custom parameters
#   -connect these agents to other nodes in the Nengo network
#   -run the simulation 
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

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Vivae tests')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

################################## 
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
RosUtils.prefferJroscore(False) # Turlte prefers roscore before jroscore (don't know why..) 

################################## 
################# define the group and start it
modemCl  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; # custom modem here
vv = "vivae.ros.simulatorControlsServer.ControlsServer"       # actual Vivae simulator, provies services

# create group of nodes
"""
note that each agent does not have own Nengo node, rather NeuralModule corresponding
to vivae simultor will have methods addAgent and removeAgent.
Each call of AddAgent method adds origin and termination to this main modem.
Each call of RemoveAgent should remove corresponding origin and termination from the neuron.  
""" 
g = NodeGroup("vivae", True);           # create default group of nodes
g.addNC(vv, "vivaeSimulator", "java");  # add vivae ControlsServer (node that handles simulation)
g.addNC(modemCl,"modem","modem")          # add default modem..
g.startGroup()                          # start group normally

################################## 
################# get modem, create smart neuron with inputs/outputs

modem = g.getModem()                    
simulator = NeuralModule('VivaeTest',modem)  # create NeuralModule which is able to add/remove agents

Controls = simulator.getControls();     # this starts the control services..
many=net.add(simulator)                 # add it to the Nengo network

time.sleep(2)    # give it some time to init services

#Controls.loadMap('data/scenarios/arena2.svg')  
#Controls.loadMap('data/scenarios/ushape.svg')
Controls.loadMap('data/scenarios/arena1.svg') 
#Controls.loadMap('data/scenarios/manyAgents.svg') 

Controls.addAgent('a',4)
Controls.addAgent('b',18)
Controls.addAgent('c',18,30,50)

Controls.start()

################################## 
################# wire the network

#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input=FunctionInput('Randomized input', [FourierFunction(.5, 10, 6, 12),
    FourierFunction(2, 11, 5, 17)],
    Units.UNK) 

# Add the input node to the network and connect it to the smart enuron
net.add(input)  

# make neural network and connect it to the smart neuron 
A=net.make('PositionData',neurons=10,dimensions=2,radius=20)

net.connect(input,simulator.getAgent('b').getTermination())
net.connect(A,simulator.getAgent('c').getTermination())

net.connect(simulator.getAgent('a').getOrigin(), A)
net.connect(A,simulator.getAgent('a').getTermination())

print "OK, configuration done."
print "-all agents are created inside the vivae node, which holds their origins/terminations"
print "-you can add any number of agents up to the number of agents found in the environment map"
print "-agent publishes custom number of variables, this number depends on properties of the sensory system" 


