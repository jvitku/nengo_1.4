# creating one neural module - for weight matrixes
# by Jaroslav Vitku

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.neurons.impl import DefaultSmartNeuron as SmartNeuron
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Smart neuron which finds (int)min and (int)max')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

################################## 
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
#RosUtils.prefferJroscore(True)  # preffer jroscore before the roscore? 

################################## 
################# define the group and start it
finder = "resender.mpt.F2IPubSub";      # Java (ROS) node that does this job
modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; 
# create group with a name
g = NodeGroup("MinMaxFinder", True);    # create independent group called..
g.addNC(finder, "Finder", "java");      # start java node and name it finder
g.addNC(modem,"Modem","modem")     # add modem to the group
g.startGroup()

################################## 
################# setup the smart neuron and add it to the Nengo network
modem = g.getModem()
neuron = SmartNeuron('MinMaxFinder', modem) # construct the smart neuron 

neuron.createEncoder("ann2rosFloatArr", "float",4)  # termination = input of neuron (4xfloat)
neuron.createDecoder("ros2annFloatArr", "int",2)    # origin = output of neuron (min and max)

many=net.add(neuron)                    # add it into the network

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
