# Test weighted connections for purposes of neuroevolution
#
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from rosnodes import logic_gates

from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule

AND 		= "org.hanns.logic.crisp.gates.impl.AND";
XOR 		= "org.hanns.logic.crisp.gates.impl.XOR";
OR 			= "org.hanns.logic.crisp.gates.impl.OR";
NAND 		= "org.hanns.logic.crisp.gates.impl.NAND";
NOT 		= "org.hanns.logic.crisp.gates.impl.NOT";

# Add nodes
def or_node(name):
	g = NodeGroup("OR", True);
	g.addNode(OR, "OR", "java");
	module = NeuralModule(name+'_OR', g)
	module.createEncoder("logic/gates/ina", "bool", 1)
	module.createEncoder("logic/gates/inb", "bool", 1)
	module.createDecoder("logic/gates/outa", "bool", 1)
	return module

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Weighted connections test 1')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

# Create function generators, white noise with params: baseFreq, maxFreq [rad/s], RMS, seed
gen1=FunctionInput('Randomized input 1', [FourierFunction(.1, 10,3, 12)],Units.UNK) 
gen2=FunctionInput('Randomized input 2', [FourierFunction(.3, 30,2, 2)],Units.UNK) 
net.add(gen1)
net.add(gen2)


myOR=or_node("myOR")	
net.add(myOR)

myNOT = logic_gates.not_node("myNOT")
net.add(myNOT)

# Wire inputs
net.connect(gen1, myOR.getTermination('logic/gates/ina'))
net.connect(gen2, myOR.getTermination('logic/gates/inb'))

net.connect(myOR.getOrigin('logic/gates/outa'), myNOT.getTermination('logic/gates/ina'), weight=1)


#net.connect(gen1, myNOT.getTermination('logic/gates/ina'))

# Wire outputs
#E=net.make('NOT_net',neurons=100,dimensions=1,radius=1)    
#net.connect(myNOT.getOrigin('logic/gates/outa'), E)


print 'Configuration complete.'
