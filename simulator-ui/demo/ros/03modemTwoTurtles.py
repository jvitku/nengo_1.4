# the same as demoOneTurtle.py, this shows how groups of nodes are pushed into own namespaces

# by Jaroslav Vitku

import nef
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Random Turtle Control Demo')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

################################## 
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
RosUtils.prefferJroscore(False) # Turlte prefers roscore before jroscore (don't know why..) 

################################## 
################# define the group and start it

turtlesim = "../testnodes/turtlesim/turtlesim_node"  # turtle under this project
act = "resender.turtle.Controller";
modemCls  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; # custom modem here

################################## 
################# turtle 1
g = NodeGroup("zelvicka", True);
g.addNC(turtlesim, "zelva", "native");  # start native node called zelva
g.addNC(modemCls,"turtlemodem","modem")  
g.startGroup()

modem = g.getModem()
bigneuron = NeuralModule('TurtleController',modem)
bigneuron.createDecoder("turtle1/pose", "pose")                 # origin
bigneuron.createDecoder("turtle1/color_sensor", "color")        # origin
bigneuron.createEncoder("turtle1/command_velocity", "velocity") # termination
many=net.add(bigneuron)
#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input=FunctionInput('Randomized input', [FourierFunction(.5, 10, 6, 12),
    FourierFunction(2, 11, 5, 17)],
    Units.UNK) 
# Add the input node to the network and connect it to the smart enuron
net.add(input)  
net.connect(input,many.getTermination('turtle1/command_velocity'))
# make neural network and connect it to the smart neuron 
A=net.make('PositionData',neurons=10,dimensions=5,radius=20)
net.connect(many.getOrigin('turtle1/pose'),A)
# make neural network and connect it to the smart neuron 
B=net.make('ColorData',neurons=10,dimensions=3,radius=120)  # RGB values observed in range of 100
net.connect(many.getOrigin('turtle1/color_sensor'),B)


################################## 
################# turtle 2
g2 = NodeGroup("zelvicka", True)
g2.addNC(turtlesim, "zelva", "native")  # start native node called zelva
g2.addNC(modemCls,"turtlemodem","modem")  
g2.startGroup()

modem2 = g2.getModem()
bigneuron2 = NeuralModule('TurtleController2',modem2)
bigneuron2.createDecoder("turtle1/pose", "pose")                 # origin
bigneuron2.createDecoder("turtle1/color_sensor", "color")        # origin
bigneuron2.createEncoder("turtle1/command_velocity", "velocity") # termination
many2=net.add(bigneuron2)
#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input2=FunctionInput('Randomized input 2', [FourierFunction(.7, 12, 7, 10),
    FourierFunction(2.1, 11, 6, 1)],
    Units.UNK) 
# Add the input node to the network and connect it to the smart enuron
net.add(input2)  
net.connect(input2,many2.getTermination('turtle1/command_velocity'))
# make neural network and connect it to the smart neuron 
C=net.make('PositionData2',neurons=10,dimensions=5,radius=20)
net.connect(many2.getOrigin('turtle1/pose'),C)
# make neural network and connect it to the smart neuron 
D=net.make('ColorData2',neurons=10,dimensions=3,radius=120)  # RGB values observed in range of 100
net.connect(many2.getOrigin('turtle1/color_sensor'),D)



print "OK, configuration done."

