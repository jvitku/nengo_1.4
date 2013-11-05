# Demo showing how the nengoros is able to launch installed ROS nodes. 
#
# It launches NeuralModule with:
# 	- one Java modem and 
#	- one installed ROS node by means rosrun command
#
# The turtlesim demo node is launched (simulator of simple world with one turtlebot). 
# The turtlebot is subscribed to commands about angular and linear velocities: 2x(x,y,z) 
# 	and publishes its velocities and colors it sees. 
# 
#
# by Jaroslav Vitku

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.modules.impl import DefaultAsynNeuralModule as AsynNeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Random Turtle Control Demo')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

################################## 
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
RosUtils.prefferJroscore(False) # Turlte prefers roscore before jroscore (don't know why..) 

################################## 
#rosnodetester = "rosnode info /Turtlesim/defaultModem"; #  experimental
#rosnodetester2 = "rosnode info /Turtlesim/Turtlesim";   #  this provides information about ROS node - Turtlesim

turtlesim = "rosrun turtlesim turtlesim_node"   # command to start turtle installed in ROS
act = "resender.turtle.Controller";             

# create group with a name
g = NodeGroup("Turtlesim", True);        # create group of ROS nodes (equals to one neural subsystem)
g.addNode(turtlesim, "Turtlesim", "native");  # add Node Configuration (turtle) to the group of nodes
#g.addNode(rosnodetester,"rosnodetest","native")    # experimental
#g.addNode(rosnodetester2,"rosnodetest","native")    #
module = NeuralModule('TurtleController', g)  

# configure modem, that is:
# -createDecoder = create origin (output) of Nengo module 
#   -what comes from ROS modules is decoded and passed to the output of subsystem in Nengo
# -createEncoder = create termination (input) of Nengo module
#   -what comes from ANN in Nengo is encoded into messages and sent to ROS modules
# modem should be able to encode/decode (all) messages used in own NodeGroup
module.createDecoder("turtle1/pose", "pose")                 # origin
module.createDecoder("turtle1/color_sensor", "color")        # origin
module.createEncoder("turtle1/cmd_vel", "geometry_msgs/Twist") # termination
subsystem=net.add(module)


#Create a white noise input function with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input=FunctionInput('Randomized input', [FourierFunction(.5, 10, 6, 12),
    FourierFunction(1, 11, 5, 17),
    FourierFunction(2, 11, 1, 15),
    FourierFunction(2, 16, 5, 12),
    FourierFunction(1, 11, 2, 11),
    FourierFunction(2, 11, 4, 18)],
    Units.UNK) 

# Add the input node to the network and connect it to the smart enuron
net.add(input)  
net.connect(input,module.getTermination('turtle1/cmd_vel'))

# make neural network and connect it to the smart neuron 
A=net.make('PositionData',neurons=10,dimensions=5,radius=20)
net.connect(module.getOrigin('turtle1/pose'),A)

# make neural network and connect it to the smart neuron 
B=net.make('ColorData',neurons=10,dimensions=3,radius=120)  # RGB values observed in range of 100
net.connect(module.getOrigin('turtle1/color_sensor'),B)


# in order to stop all nodes in the group you can either:
#   -delete the main network from nengo (roscore and graph stay running)
#   -rerun the script  (all nodes will shutdown and start again)
#   -close nengo (everything shuts down)
print "OK, configuration done."
