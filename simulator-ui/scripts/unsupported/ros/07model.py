# Shows how to load custom map and control many agents in it 

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
net=nef.Network('Vivae tests')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

################################## 
################# define the group and start it
modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; # custom modem here
vv = "vivae.ros.simulatorControlsServer.ControlsServer"       # actual Vivae simulator, provies services

g = NodeGroup("vivae", True);           # create default group of nodes
g.addNC(vv, "vivaeSimulator", "java");  # add vivae ControlsServer (node that handles simulation)
g.addNC(modem,"modem","modem")          # add default modem..
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
#Controls.loadMap('data/scenarios/arena1.svg') 
Controls.loadMap('data/scenarios/arena1.svg') 

Controls.addAgent('a',4)
Controls.addAgent('b',18)
Controls.addAgent('c',18,30,50)
Controls.addAgent('d')  # adding more agents than is in the simulation
Controls.addAgent('e',8)# will result just in error message in console
Controls.addAgent('f',20,10,50) # these agents are not created
Controls.addAgent('g')
Controls.addAgent('h',39)
Controls.addAgent('i',22,30,1)
Controls.addAgent('j')

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
print "-Agents have custom sensors"
print "-Aget can be cotrolled from arbitrary source, there are some restriction on IO dimension"
print "-Agent also does not have to be controlled at all."



