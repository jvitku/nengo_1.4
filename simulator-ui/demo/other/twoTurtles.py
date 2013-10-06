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

#####################################################################
# setup the ROS utils (optional, takes effect only if ROS found) 
RosUtils.setAutorun(True)
RosUtils.prefferJroscore(False)

# define nodes I want to use 
turtlesim = "../testnodes/turtlesim/turtlesim_node"  # turtle under this project
act =   "resender.turtle.Controller";
sense = "resender.turtle.PositionSensor";

# create group for turtle
g = NodeGroup("zelvicka", True);    # True menas that group is independent, can be pushed into namespace 
g.addNC(turtlesim, "zelva", "native");  
g.addNC(act, "actuators", "java");      
g.addNC(sense, "sensors", "java");
 
# create identical group for another turtle
g2 = NodeGroup("zelvicka", True);
g2.addNC(turtlesim, "zelva", "native");  
g2.addNC(act, "actuators", "java");      
g2.addNC(sense, "sensors", "java");

# start them
g.startGroup();
g2.startGroup();

# that is it, this will run forever.. 
print "OK, configuration done."
print "Note that this demo can be stopped only by closing the Nengo simulator window"




