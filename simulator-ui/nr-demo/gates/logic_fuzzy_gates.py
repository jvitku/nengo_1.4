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
from rosnodes import logic_gates

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Demo script which shows network of fuzzy logic gates')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

# Create function generators, white noise with params: baseFreq, maxFreq [rad/s], RMS, seed
gen1=FunctionInput('Randomized input 1', [FourierFunction(.1, 10,3, 12)],Units.UNK) 
gen2=FunctionInput('Randomized input 2', [FourierFunction(.3, 30,2, 2)],Units.UNK) 
net.add(gen1)
net.add(gen2)

# Add nodes
myAND = logic_gates.fuzzyand_node("myAND")
net.add(myAND)

myOR = logic_gates.fuzzyor_node("myOR")
net.add(myOR)

myNOT = logic_gates.fuzzynot_node("myNOT")
net.add(myNOT)

# Wire inputs
net.connect(gen1, myAND.getTermination('logic/gates/ina'))
net.connect(gen2, myAND.getTermination('logic/gates/inb'))

net.connect(gen1, myOR.getTermination('logic/gates/ina'))
net.connect(gen2, myOR.getTermination('logic/gates/inb'))

net.connect(gen1, myNOT.getTermination('logic/gates/ina'))

# Wire outputs
A=net.make('AND_net',neurons=100,dimensions=1,radius=1)    
net.connect(myAND.getOrigin('logic/gates/outa'), A)

D=net.make('OR_net',neurons=100,dimensions=1,radius=1)    
net.connect(myOR.getOrigin('logic/gates/outa'), D)

E=net.make('NOT_net',neurons=100,dimensions=1,radius=1)    
net.connect(myNOT.getOrigin('logic/gates/outa'), E)


print 'Configuration complete.'
