# Demo showing how to use crisp logic gates implemented as ROS nodes in the Nengoros.
#
#
# To launch this script: open the Nengo gui (probably nengo/simulator-ui/nengo) and weite into the command line:
# 	run nr-demo/gates/logic_crisp_gates.py
# Note that this cript has to be either symlinked or copied into the location: nengo/simulator-ui/nengo/nr-demo/gates/
#
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from rosnodes import logic_gates

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Demo script which shows network of logic gates')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

# Create function generators, white noise with params: baseFreq, maxFreq [rad/s], RMS, seed
gen1=FunctionInput('Randomized input 1', [FourierFunction(.1, 10,3, 12)],Units.UNK) 
gen2=FunctionInput('Randomized input 2', [FourierFunction(.3, 30,2, 2)],Units.UNK) 
net.add(gen1)
net.add(gen2)

# Add nodes
myAND = logic_gates.and_node("myAND")
net.add(myAND)

myXOR = logic_gates.xor_node("myXOR")
net.add(myXOR)

myNAND = logic_gates.nand_node("myNAND")
net.add(myNAND)

myOR = logic_gates.or_node("myOR")
net.add(myOR)

myNOT = logic_gates.not_node("myNOT")
net.add(myNOT)

# Wire inputs
net.connect(gen1, myAND.getTermination('logic/gates/ina'))
net.connect(gen2, myAND.getTermination('logic/gates/inb'))

net.connect(gen1, myXOR.getTermination('logic/gates/ina'))
net.connect(gen2, myXOR.getTermination('logic/gates/inb'))

net.connect(gen1, myNAND.getTermination('logic/gates/ina'))
net.connect(gen2, myNAND.getTermination('logic/gates/inb'))

net.connect(gen1, myOR.getTermination('logic/gates/ina'))
net.connect(gen2, myOR.getTermination('logic/gates/inb'))

net.connect(gen1, myNOT.getTermination('logic/gates/ina'))

# Wire outputs
A=net.make('AND_net',neurons=100,dimensions=1,radius=1)    
net.connect(myAND.getOrigin('logic/gates/outa'), A)

B=net.make('XOR_net',neurons=100,dimensions=1,radius=1)    
net.connect(myXOR.getOrigin('logic/gates/outa'), B)

C=net.make('NAND_net',neurons=100,dimensions=1,radius=1)    
net.connect(myNAND.getOrigin('logic/gates/outa'), C)

D=net.make('OR_net',neurons=100,dimensions=1,radius=1)    
net.connect(myOR.getOrigin('logic/gates/outa'), D)

E=net.make('NOT_net',neurons=100,dimensions=1,radius=1)    
net.connect(myNOT.getOrigin('logic/gates/outa'), E)


print 'Configuration complete.'
