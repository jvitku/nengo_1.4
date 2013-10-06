# Create Synchronous NeuralModule which has one asynchornous input, should not be awaited at all
# by Jaroslav Vitku

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl import DefaultSyncNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Testing synchronous communication with ROS')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

################################## 
################# setup the ROS utils (optional) 
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (tru by default)
#RosUtils.prefferJroscore(True)  # preffer jroscore before the roscore? 

################################## 
################# define the group and start it
finder = "resender.mpt.F2IPubSub";          # Jva (ROS) node that does this job
modem  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; 

g = NodeGroup("MinMaxFinder", True);
g.addNC(finder, "Finder", "java");  
g.addNC(modem,"Modem","modem")     	
g.startGroup()

################################## 
################# setup the smart neuron and add it to the Nengo network
modem = g.getModem()
neuron = NeuralModule('MinMaxFinder', modem) 

neuron.createEncoder("ann2rosFloatArr", "float",4)      
neuron.createDecoder("ros2annFloatArr", "int",2,False)    # HERE: decorer is asynchronous

many=net.add(neuron)                   

#Create a white noise input function with params: baseFreq, maxFreq [rad/s], RMS, seed
input=FunctionInput('Randomized input', [FourierFunction(.1, 10,1, 12),
    FourierFunction(.4, 20,1.5, 11),
    FourierFunction(.1, 10,0.9, 10),
    FourierFunction(.5, 11,1.6, 17)],Units.UNK) 

net.add(input) # Add to the network and connect to neuron
net.connect(input,neuron.getTermination('ann2rosFloatArr'))


print 'Configuration complete.'
