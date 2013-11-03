# This is python interface for the project logic/gates. This helps using particular ROsJava nodes in Jython scripts for Nengo simulator.
#
# Import this in your scirpt in order to use it, for examples see ../nr-demo/ scripts.
#
# Note: that Decreasing and IncreasingLinear membership functions are TODO (not working in Nengo GUI some..why)
#
# author Jaroslav Vitku

import nef
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
# Note that modules without outputs (Nengo-decoder) cannot use synchronous mode (DefaultNeuralModule).
#from ctu.nengoros.modules.impl import DefaultAsynNeuralModule as NeuralModule
# The simulator waits each simulation step for the output in the synchronous mode, this case:
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule

################# Create Neural Modules containing the ROS nodes

# crisp gates
AND 		= "org.hanns.logic.crisp.gates.impl.AND";
XOR 		= "org.hanns.logic.crisp.gates.impl.XOR";
OR 			= "org.hanns.logic.crisp.gates.impl.OR";
NAND 		= "org.hanns.logic.crisp.gates.impl.NAND";
NOT 		= "org.hanns.logic.crisp.gates.impl.NOT";

# fuzzy gates
fAND 		= "org.hanns.logic.fuzzy.gates.impl.AND";
fOR 		= "org.hanns.logic.fuzzy.gates.impl.OR";
fNOT 		= "org.hanns.logic.fuzzy.gates.impl.NOT";

# fuzzy membership functions
flininc		= "org.hanns.logic.fuzzy.membership.impl.IncreasingLinear";
flindec		= "org.hanns.logic.fuzzy.membership.impl.DecreasingLinear";
ftrap 		= "org.hanns.logic.fuzzy.membership.impl.Trapezoid";
ftriangle 	= "org.hanns.logic.fuzzy.membership.impl.Triangular";


# Initialize ROS(java) node implementning AND function.
#
# Example how to use this in the simulation:
#
# myAND = logic_gates.and_node("ahoj")
# net.add(myAND)
# net.connect(myAND.getOrigin('logic/gates/ina'), myAND.getTermination('hanns/demo/pubsub'))
#
def and_node(name):
	g = NodeGroup("AND", True);
	g.addNode(AND, "AND", "java");
	module = NeuralModule(name+'_AND', g)
	module.createEncoder("logic/gates/ina", "bool", 1)
	module.createEncoder("logic/gates/inb", "bool", 1)
	module.createDecoder("logic/gates/outa", "bool", 1)
	return module

def xor_node(name):
	g = NodeGroup("XOR", True);
	g.addNode(XOR, "XOR", "java");
	module = NeuralModule(name+'_XOR', g)
	module.createEncoder("logic/gates/ina", "bool", 1)
	module.createEncoder("logic/gates/inb", "bool", 1)
	module.createDecoder("logic/gates/outa", "bool", 1)
	return module

def or_node(name):
	g = NodeGroup("OR", True);
	g.addNode(OR, "OR", "java");
	module = NeuralModule(name+'_OR', g)
	module.createEncoder("logic/gates/ina", "bool", 1)
	module.createEncoder("logic/gates/inb", "bool", 1)
	module.createDecoder("logic/gates/outa", "bool", 1)
	return module

def nand_node(name):
	g = NodeGroup("NAND", True);
	g.addNode(NAND, "NAND", "java");
	module = NeuralModule(name+'_NAND', g)
	module.createEncoder("logic/gates/ina", "bool", 1)
	module.createEncoder("logic/gates/inb", "bool", 1)
	module.createDecoder("logic/gates/outa", "bool", 1)
	return module

def not_node(name):
	g = NodeGroup("NOT", True);
	g.addNode(NOT, "NOT", "java");
	module = NeuralModule(name+'_NOT', g)
	module.createEncoder("logic/gates/ina", "bool", 1)
	module.createDecoder("logic/gates/outa", "bool", 1)
	return module


################################################################### fuzzy gates

def fuzzyand_node(name):
	g = NodeGroup("FuzzyAND", True);
	g.addNode(fAND, "FuzzyAND", "java");
	module = NeuralModule(name+'_FuzzyAND', g)
	module.createEncoder("logic/gates/ina", "float", 1)
	module.createEncoder("logic/gates/inb", "float", 1)
	module.createDecoder("logic/gates/outa", "float", 1)
	return module

def fuzzyor_node(name):
	g = NodeGroup("FuzzyOR", True);
	g.addNode(fOR, "FuzzyOR", "java");
	module = NeuralModule(name+'_FuzzyOR', g)
	module.createEncoder("logic/gates/ina", "float", 1)
	module.createEncoder("logic/gates/inb", "float", 1)
	module.createDecoder("logic/gates/outa", "float", 1)
	return module

def fuzzynot_node(name):
	g = NodeGroup("FuzzyNOT", True);
	g.addNode(fNOT, "FuzzyNOT", "java");
	module = NeuralModule(name+'_FuzzyNOT', g)
	module.createEncoder("logic/gates/ina", "float", 1)
	module.createDecoder("logic/gates/outa", "float", 1)
	return module


############################################################ fuzzy membership functions
	
def fuzzyMemDecLin(name):									# ----\____
	g = NodeGroup("FuzzyMemLinDec", True);
	g.addNode(flindec,"FuzzyMemLinDec","java");
	module = NeuralModule(name+'_FuzzyMemLinDec', g)
	module.createEncoder("logic/gates/ina", "float", 1)		# x
	module.createEncoder("logic/gates/confa", "float", 1) 	# alpha
	module.createEncoder("logic/gates/confb", "float", 1) 	# beta
	module.createDecoder("logic/gates/outa", "float", 1)	# y
	return module

def fuzzyMemTriangle(name):									# ____|\____
	g = NodeGroup("FuzzyMemTriangle", True);
	g.addNode(ftriangle,"FuzzyMemTriangle","java");
	module = NeuralModule(name+'_FuzzyMemTriangle', g)
	module.createEncoder("logic/gates/ina", "float", 1)		# x
	module.createEncoder("logic/gates/confa", "float", 1) 	# alpha
	module.createEncoder("logic/gates/confb", "float", 1) 	# beta
	module.createEncoder("logic/gates/confc", "float", 1) 	# gamma
	module.createDecoder("logic/gates/outa", "float", 1)	# y
	return module

def fuzzyMemTrapezoid(name):								# ___|---\___
	g = NodeGroup("FuzzyMemTrapezoid", True);
	g.addNode(ftrap,"FuzzyMemTrapezoid", "java");
	module = NeuralModule(name+'_FuzzyMemTrapezoid', g)
	module.createEncoder("logic/gates/ina", "float", 1)		# x
	module.createEncoder("logic/gates/confa", "float", 1) 	# alpha
	module.createEncoder("logic/gates/confb", "float", 1) 	# beta
	module.createEncoder("logic/gates/confc", "float", 1) 	# gamma
	module.createEncoder("logic/gates/confd", "float", 1) 	# delta
	module.createDecoder("logic/gates/outa", "float", 1)	# y
	return module

def fuzzyMemIncLin(name):									# ____|----
	g = NodeGroup("FuzzyMemIncLin", True);
	g.addNode(flininc,"FuzzyMemIncLin","java");
	module = NeuralModule(name+'_FuzzyMemIncLin', g)
	module.createEncoder("logic/gates/ina", "float", 1)		# x
	module.createEncoder("logic/gates/confa", "float", 1) 	# alpha
	module.createEncoder("logic/gates/confb", "float", 1) 	# beta
	module.createDecoder("logic/gates/outa", "float", 1)	# y
	return module
