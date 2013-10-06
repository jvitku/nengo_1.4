# Demo showing how the nengoros is able to launch group of nodes consisting of:
#   -java node modem      = modem - class that converts data between ROS-Nengo. Add custom inputs/outputs
#   -ROS installed node   = native application (installed turtlesim here, demo simulator shipped with ROS)

# as a result, neural subsystem in nengo contains entire turtlesim:
#   -inputs to the subsystem control the turtle (linear and angular speeds)
#   -outputs from subsystem correspond to turtle sensori data ( X,Y,theta, linear and angular speeds)

# note: that turtle application may run probably only on Ubuntu 12.04, ROS installation recommended
 
# by Jaroslav Vitku

# imports of Java
import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.neurons.impl.test import SecondOne as SmartNeuron
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

###############################3 tady tady: 
rosnodetester = "rosnode info zelvicka/zelva";   # nengoros should execute "whic rosnode", then "rosnode turt..  [some trash..]"
turtlesim = "rosrun turtlesim turtlesim_node"   # command to start turtle installed in ROS
act = "resender.turtle.Controller";             
modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; # add a default model to the turtle

# create group with a name
g = NodeGroup("zelvicka", True);        # create group of ROS nodes (equals to one neural subsystem)
g.addNC(turtlesim, "zelva", "native");  # add Node Configuration (turtle) to the group of nodes
g.addNC(modem,"turtlemodem","modem")    # add modem configuration to the group

g.addNC(rosnodetester,"rosnodetest","native")    # teeeeeeeeeeeeeeeeest

g.startGroup()                          # start all nodes in the group

################################## 
################# get modem, create neural subsystem with inputs/outputs

modem = g.getModem()                                        # get neuron  
module = SmartNeuron('TurtleController',modem)              # create a module in Nengo with turtle modem

# configure modem, that is:
# -createDecoder = create origin (output) of Nengo module 
#   -what comes from ROS modules is decoded and passed to the output of subsystem in Nengo
# -createEncoder = create termination (input) of Nengo module
#   -what comes from ANN in Nengo is encoded into messages and sent to ROS modules
# modem should be able to encode/decode (all) messages used in own NodeGroup

module.createDecoder("turtle1/pose", "pose")                 # origin
module.createDecoder("turtle1/color_sensor", "color")        # origin
module.createEncoder("turtle1/command_velocity", "velocity") # termination

# build the subsystem in Nengo, thats it
subsystem=net.add(module)


################################## 
################# create other ANN components (in Nengo)

# Create a white noise generator with parameters: baseFreq, maxFreq (rad/s), RMS, Seed
input=FunctionInput('Randomized input', [FourierFunction(.5, 10, 6, 12),
    FourierFunction(2, 11, 5, 17)],
    Units.UNK) 
net.add(input)  # add to Nengi gui

# make two neural networks which approximate its inputs
A=net.make('PositionData',neurons=10,dimensions=5,radius=20) 
B=net.make('ColorData',neurons=10,dimensions=3,radius=120)  # RGB values observed in range of 100


################################## 
################# wire the network in Nengo (make connection between modules)

net.connect(input,subsystem.getTermination('turtle1/command_velocity')) # signal generator to velocity
net.connect(subsystem.getOrigin('turtle1/pose'),A)              # network A approximates turtle position
net.connect(subsystem.getOrigin('turtle1/color_sensor'),B)      # network B approximates colors seen..


print "OK, configuration done."

