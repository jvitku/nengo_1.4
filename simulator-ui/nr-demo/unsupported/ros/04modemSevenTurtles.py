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


################################## 
################# turtle 3
g3 = NodeGroup("zelvicka", True)
g3.addNC(turtlesim, "zelva", "native")  # start native node called zelva
g3.addNC(modemCls,"turtlemodem","modem")  
g3.startGroup()

modem3 = g3.getModem()
bigneuron3 = NeuralModule('TurtleController3',modem3)
bigneuron3.createDecoder("turtle1/pose", "pose")                 # origin
bigneuron3.createDecoder("turtle1/color_sensor", "color")        # origin
bigneuron3.createEncoder("turtle1/command_velocity", "velocity") # termination
many3=net.add(bigneuron3)
#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input3=FunctionInput('Randomized input 3', [FourierFunction(.7, 13, 7, 10),
    FourierFunction(3.1, 11, 6, 1)],
    Units.UNK) 
# Add the input node to the network and connect it to the smart enuron
net.add(input3)  
net.connect(input3,many3.getTermination('turtle1/command_velocity'))
# make neural network and connect it to the smart neuron 
C=net.make('PositionData3',neurons=10,dimensions=5,radius=30)
net.connect(many3.getOrigin('turtle1/pose'),C)
# make neural network and connect it to the smart neuron 
D=net.make('ColorData3',neurons=10,dimensions=3,radius=130)  # RGB values observed in range of 100
net.connect(many3.getOrigin('turtle1/color_sensor'),D)

################################## 
################# turtle 4
g4 = NodeGroup("zelvicka", True)
g4.addNC(turtlesim, "zelva", "native")  # start native node called zelva
g4.addNC(modemCls,"turtlemodem","modem")  
g4.startGroup()

modem4 = g4.getModem()
bigneuron4 = NeuralModule('TurtleController4',modem4)
bigneuron4.createDecoder("turtle1/pose", "pose")                 # origin
bigneuron4.createDecoder("turtle1/color_sensor", "color")        # origin
bigneuron4.createEncoder("turtle1/command_velocity", "velocity") # termination
many4=net.add(bigneuron4)
#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input4=FunctionInput('Randomized input 4', [FourierFunction(.7, 14, 7, 10),
    FourierFunction(4.1, 11, 6, 1)],
    Units.UNK) 
# Add the input node to the network and connect it to the smart enuron
net.add(input4)  
net.connect(input4,many4.getTermination('turtle1/command_velocity'))
# make neural network and connect it to the smart neuron 
C=net.make('PositionData4',neurons=10,dimensions=5,radius=40)
net.connect(many4.getOrigin('turtle1/pose'),C)
# make neural network and connect it to the smart neuron 
D=net.make('ColorData4',neurons=10,dimensions=4,radius=140)  # RGB values observed in range of 100
net.connect(many4.getOrigin('turtle1/color_sensor'),D)

################################## 
################# turtle 5
g5 = NodeGroup("zelvicka", True)
g5.addNC(turtlesim, "zelva", "native")  # start native node called zelva
g5.addNC(modemCls,"turtlemodem","modem")  
g5.startGroup()

modem5 = g5.getModem()
bigneuron5 = NeuralModule('TurtleController5',modem5)
bigneuron5.createDecoder("turtle1/pose", "pose")                 # origin
bigneuron5.createDecoder("turtle1/color_sensor", "color")        # origin
bigneuron5.createEncoder("turtle1/command_velocity", "velocity") # termination
many5=net.add(bigneuron5)
#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input5=FunctionInput('Randomized input 5', [FourierFunction(.7, 15, 7, 10),
    FourierFunction(5.1, 11, 6, 1)],
    Units.UNK) 
# Add the input node to the network and connect it to the smart enuron
net.add(input5)  
net.connect(input5,many5.getTermination('turtle1/command_velocity'))
# make neural network and connect it to the smart neuron 
C=net.make('PositionData5',neurons=10,dimensions=5,radius=50)
net.connect(many5.getOrigin('turtle1/pose'),C)
# make neural network and connect it to the smart neuron 
D=net.make('ColorData5',neurons=10,dimensions=5,radius=150)  # RGB values observed in range of 100
net.connect(many5.getOrigin('turtle1/color_sensor'),D)

################################## 
################# turtle 6
g6 = NodeGroup("zelvicka", True)
g6.addNC(turtlesim, "zelva", "native")  # start native node called zelva
g6.addNC(modemCls,"turtlemodem","modem")  
g6.startGroup()

modem6 = g6.getModem()
bigneuron6 = NeuralModule('TurtleController6',modem6)
bigneuron6.createDecoder("turtle1/pose", "pose")                 # origin
bigneuron6.createDecoder("turtle1/color_sensor", "color")        # origin
bigneuron6.createEncoder("turtle1/command_velocity", "velocity") # termination
many6=net.add(bigneuron6)
#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input6=FunctionInput('Randomized input 6', [FourierFunction(.7, 16, 7, 10),
    FourierFunction(6.1, 11, 6, 1)],
    Units.UNK) 
# Add the input node to the network and connect it to the smart enuron
net.add(input6)  
net.connect(input6,many6.getTermination('turtle1/command_velocity'))
# make neural network and connect it to the smart neuron 
C=net.make('PositionData6',neurons=10,dimensions=6,radius=60)
net.connect(many6.getOrigin('turtle1/pose'),C)
# make neural network and connect it to the smart neuron 
D=net.make('ColorData6',neurons=10,dimensions=6,radius=160)  # RGB values observed in range of 100
net.connect(many6.getOrigin('turtle1/color_sensor'),D)

################################## 
################# turtle 7
g7 = NodeGroup("zelvicka", True)
g7.addNC(turtlesim, "zelva", "native")  # start native node called zelva
g7.addNC(modemCls,"turtlemodem","modem")  
g7.startGroup()

modem7 = g7.getModem()
bigneuron7 = NeuralModule('TurtleController7',modem7)
bigneuron7.createDecoder("turtle1/pose", "pose")                 # origin
bigneuron7.createDecoder("turtle1/color_sensor", "color")        # origin
bigneuron7.createEncoder("turtle1/command_velocity", "velocity") # termination
many7=net.add(bigneuron7)
#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input7=FunctionInput('Randomized input 7', [FourierFunction(.7, 17, 7, 10),
    FourierFunction(7.1, 11, 7, 1)],
    Units.UNK) 
# Add the input node to the network and connect it to the smart enuron
net.add(input7)  
net.connect(input7,many7.getTermination('turtle1/command_velocity'))
# make neural network and connect it to the smart neuron 
C=net.make('PositionData7',neurons=10,dimensions=7,radius=70)
net.connect(many7.getOrigin('turtle1/pose'),C)
# make neural network and connect it to the smart neuron 
D=net.make('ColorData7',neurons=10,dimensions=7,radius=170)  # RGB values observed in range of 100
net.connect(many7.getOrigin('turtle1/color_sensor'),D)





print "OK, configuration done."

