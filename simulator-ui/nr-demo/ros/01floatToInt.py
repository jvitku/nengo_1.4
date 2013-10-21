# Create the NeuralModule which receives 4 float values, finds min and max, converts them to int and passes to the output.
#
# starts: 
#   -ROS-java node (class extending the org.ros.Node) which does exactly the thing described above
#   -NeuralModule with modem that communicates with the ROS node 
#
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Neural module - find min and max and round to integer')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one


################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
#RosUtils.prefferJroscore(True)  # preffer jroscore before the roscore? 


################# Define the Group of nodes
finder = "resender.mpt.F2IPubSub";          # Java (ROS) node that does this job
g = NodeGroup("MinMaxFinder", True);        # create independent (True) group called..
g.addNode(finder, "Finder", "java");        # start java node and name it finder

################# Setup the Neural Module and add it to the Nengo network
neuron = NeuralModule('MinMaxFinder', g)    # construct the Neural Module
neuron.createEncoder("ann2rosFloatArr", "float",4)  # termination = input of neuron (4xfloat)
neuron.createDecoder("ros2annFloatArr", "int",2)    # origin = output of neuron (min and max)

many=net.add(neuron)                        # add it into the network

#Create a white noise input function with params: baseFreq, maxFreq [rad/s], RMS, seed
input=FunctionInput('Randomized input', [FourierFunction(.1, 10,1, 12),
    FourierFunction(.4, 20,1.5, 11),
    FourierFunction(.1, 10,0.9, 10),
    FourierFunction(.5, 11,1.6, 17)],Units.UNK) 

net.add(input) # Add to the network and connect to neuron
net.connect(input,neuron.getTermination('ann2rosFloatArr'))

# create ANN with 100 neurons which approximates it's input
A=net.make('A',neurons=100,dimensions=2,radius=3)    
net.connect(neuron.getOrigin('ros2annFloatArr'),A)

print 'Configuration complete.'
