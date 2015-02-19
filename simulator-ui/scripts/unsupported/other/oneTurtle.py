# Demo showing how the nengoros is able to launch group of nodes consisting of:
#   -java node actuators    = java node publishing commands by means of ROS network
#   -java node sensors      = java node modem subscribed to messages about turtle position
#   -native node            = native application (installed turtlesim, demo simulator shipped with ROS)

# as a result, (native application) turtle is controlled by java node actuators and sends messages to java node sensors
# all these nodes are in one nodeGroup

# note: that turtle application may run probably only on Ubuntu 12.04 64bit, ROS installation recommended
# note: that this is direct equivalent to Java class: nengoros.comm.nodeFactory.demo.turtle.OneTurtle
 
# by Jaroslav Vitku

import nef
import math
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units

# creates python class and calls constructor __init__
net=nef.Network('Random Turtle Control Demo')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

# some default imports copied from nef
from ca.nengo.util import VisiblyMutableUtils
import java
import inspect
import warnings
# import my custom classes here:
from nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils

################################## 
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)  # Do we want to autorun roscore and rxgraph? (tru by default)
RosUtils.prefferJroscore(False) # Turlte prefers roscore before jroscore (don't know why..) 

################################## 
################# define the group 

turtlesim = "../testnodes/turtlesim/turtlesim_node"  # turtle under this project
act = "resender.turtle.Controller";
sense = "resender.turtle.PositionSensor";

# create group with a name
g = NodeGroup("zelvicka", True);
		
# add node configurations to the group
g.addNC(turtlesim, "zelva", "native");  # start native node called zelva
g.addNC(act, "actuators", "java");      # start java nactuator ndoe called actuators
g.addNC(sense, "sensors", "java");      # start java sensoric node called sensors

################################## 
################# start the group 
g.startGroup()

# that is it, this will run forever.. 
print "OK, configuration done."
print "Note that this demo can be stopped only by closing the Nengo simulator window"




