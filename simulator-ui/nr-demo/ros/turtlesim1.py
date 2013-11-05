# Demo showing how the nengoros is able to launch group of nodes consisting of:
#   -java node modem      = modem - class that converts data between ROS-Nengo. Add custom inputs/outputs
#   -native node          = native application (installed turtlesim here, demo simulator shipped with ROS)

# as a result, NeuralModule in nengo contains entire turtlesim:
#   -inputs to neuron control the turtle (linear and angular speeds)
#   -outputs from neuron correspond to turtle sensori data ( X,Y,theta, linear and angular speeds)

# note: that turtle application may run probably only on Ubuntu 12.04 64bit, ROS installation recommended
 
# by Jaroslav Vitku

import nef
from ca.nengo.math.impl import FourierFunction
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

rosnodetester = "rosnode info zelvicka/zelva";   # nengoros should execute "whic rosnode", then "rosnode turt..  [some trash..]"
turtlesim = "rosrun turtlesim turtlesim_node"   # command to start turtle installed in ROS
act = "resender.turtle.Controller";             
#modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; # add a default model to the turtle

# create group with a name
g = NodeGroup("Turtlesim", True);        # create group of ROS nodes (equals to one neural subsystem)
g.addNode(turtlesim, "Turtlesim", "native");  # add Node Configuration (turtle) to the group of nodes
#g.addNode(modem,"turtlemodem","modem")    # add modem configuration to the group

g.addNode(rosnodetester,"rosnodetest","native")    # teeeeeeeeeeeeeeeeest

module = NeuralModule('TurtleController', g)  

# configure modem, that is:
# -createDecoder = create origin (output) of Nengo module 
#   -what comes from ROS modules is decoded and passed to the output of subsystem in Nengo
# -createEncoder = create termination (input) of Nengo module
#   -what comes from ANN in Nengo is encoded into messages and sent to ROS modules
# modem should be able to encode/decode (all) messages used in own NodeGroup

module.createDecoder("turtle1/pose", "pose")                 # origin
module.createDecoder("turtle1/color_sensor", "color")        # origin
module.createEncoder("turtle1/command_velocity", "velocity") # termination
subsystem=net.add(module)

"""
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
"""

# in order to stop all nodes in the group you can either:
#   -delete the main network from nengo (roscore and graph stay running)
#   -rerun the script  (all nodes will shutdown and start again)
#   -close nengo (everything shuts down)
print "OK, configuration done."

