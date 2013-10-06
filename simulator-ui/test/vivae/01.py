# this is the same demo as 5vivae.py
# the difference is that this launches the java node natively, so instead of 
# calling methid ControlsServer from the source code, it calls script which
# launches installed jar library

# this is the way how most ROS nodes will be launched

# note that this way we get abolutely independent processes, which communicate only over the ROS network
# process can be only started / killed by nengo

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
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
RosUtils.prefferJroscore(False) # Turlte prefers roscore before jroscore (don't know why..) 

################################## 
################# define the group and start it
modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; # custom modem here
# this is launch command which calls the RosRun with name of ControlsServer as parameter
vvv = ["./sb/../../../../simulators/vivae/build/install/vivae/bin/vivae","vivae.ros.simulatorControlsServer.ControlsServer"]

# create group of nodes
g = NodeGroup("vivae", True);           # create default group of nodes
g.addNC(vvv, "vivaeSimulator", "native");  # run the simulator..
g.addNC(modem,"modem","modem")          # add default modem..
g.startGroup()                          # start group normally

################################## 
modem = g.getModem()
time.sleep(3)    # if the process is native, it takes longer time to init the services !!                 
simulator = NeuralModule('VivaeTest',modem)  # create NeuralModule which is able to add/remove agents

Controls = simulator.getControls();     # this starts the control services..
many=net.add(simulator)                 # add it to the Nengo network

#Controls.loadMap('data/scenarios/arena2.svg')  
#Controls.loadMap('data/scenarios/ushape.svg')
Controls.loadMap('data/scenarios/arena1.svg') 
#Controls.loadMap('data/scenarios/manyAgents.svg') 

Controls.addAgent('a',4)
Controls.addAgent('b',18)
Controls.addAgent('c',18,30,50)

"""
Controls.addAgent('d')
Controls.addAgent('e',8)
Controls.addAgent('f',20,10,50)
Controls.addAgent('g')
Controls.addAgent('h',39)
Controls.addAgent('i',22,30,1)
Controls.addAgent('j')
"""
#time.sleep(1);      # concurent modification exception
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


