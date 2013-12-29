# This demo shows how the new version of Nengoros is able to connect  multiple Origins to one "input"
# (Encoder) of NeuralModule. 
# 
# One Encoder has purpose of encoding and sending ROS messages to ROS nodes associated with the Module.
# Until now, each Encoder corresponded to one Termination (input of NeuralModule). Now, each Encoder
# has one Termination by default, but also can add more weighted terminations to the Encoder.
#
# @see ctu.nengoros.modules.NeuralModule
#
#
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]
#
# Nengo
import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
# Nengoros
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
from ctu.nengoros.testsuit.demo.nodes.minmax import F2IPubSub
# Jython scripts for ROS nodes (unused)
import basic_nodes

# Add nodes
def buildModule(name):
    minmaxint =   	"ctu.nengoros.testsuit.demo.nodes.minmax.F2IPubSub"
    g = NodeGroup("MinMaxInt", True);        			# create independent (True) group called..
    g.addNode(minmaxint, "MinMaxIntNode", "java");     	# start java node and name it finder
    module = NeuralModule(name+'_MinMaxInt', g, True)  # construct the Neural Module and run it as synchrnonous (True)
    
    # Add an Encoder to the module (input), which receives 4 floats and sends them to the ROS node 
    # This adds one (correspondingly named) Termination to the NeuralModule 
    module.createEncoder(F2IPubSub.ann2ros, "float", 4) 
    
    # Add a Decoder to the module (output)
    # The Decoder receives ROS messages (topic name==first parameter), decodes them into Nengo RealOutputImpl data,
    # and places values on the (correspondingly named) Origin
    # In case of synchronous usage of NeuralModule, the Nengo simulator waits for new ROS messages each sim. step
    module.createDecoder(F2IPubSub.ros2ann, "int", 2)
    return module

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Demo showing multiple weighted inputs connected to one Encoder')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

# Create function generators, white noise with params: baseFreq, maxFreq [rad/s], RMS, seed
gen1=FunctionInput('Generator1', [FourierFunction(.1, 10,1, 12),
    FourierFunction(.4, 20,1.5, 11),
    FourierFunction(.1, 10,0.9, 10),
    FourierFunction(.5, 11,1.6, 17)],Units.UNK)

gen2=FunctionInput('Generator2', [FourierFunction(.1, 10,1, 12),
    FourierFunction(.4, 20,1.5, 11),
    FourierFunction(.1, 10,0.9, 10),
    FourierFunction(.5, 11,1.6, 17)],Units.UNK)

gen3=FunctionInput('Generator3', [FourierFunction(.1, 10,1, 12),
    FourierFunction(.4, 20,1.5, 11),
    FourierFunction(.1, 10,0.9, 10),
    FourierFunction(.5, 11,1.6, 17)],Units.UNK)

net.add(gen1)
net.add(gen2)
net.add(gen3)

# create the neural module with one Origin and one default Termination 
module1=buildModule("defaultModule")	
net.add(module1)

#module2=buildModule("multiTerminationModule")	
#net.add(module2)

# Wire things together
#net.connect(gen1, module1.getTermination(F2IPubSub.ann2ros))    # connect generator to default Termination of Module

#net.connect(gen1, module2.newTerminationFor(F2IPubSub.ann2ros)) # Connect generator to newly created Termination


#net.connect(myOR.getOrigin('logic/gates/outa'), myNOT.getTermination('logic/gates/ina'), weight=1)

print 'Configuration complete.'
