# Create the NeuralModule which implements discrete source of motivation.
#
# the source of motivaiton has:
#   -n inputs, these are summed, if the value is equal or more  than threshold (1 now), the reinforcement is received, state variable is set back to limbo
#   -2 outputs defining: {reward just received, size of current motivation}
#
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule

#from org.hanns.rl.discrete.ros.sarsa import QLambda as QLambda
from org.hanns.physiology.statespace.ros import BasicMotivation as Motivation

# java classes
classs = "org.hanns.physiology.statespace.ros.BasicMotivation"

# Synchronous NeuralModule implementing simple source of agents motivation
def basic(name, noInputs=Motivation.DEF_NOINPUTS, decay=Motivation.DEF_DECAY, logPeriod=Motivation.DEF_LOGPERIOD,
rewVal=Motivation.DEF_REWARD, rewThr=Motivation.DEF_REWTHRESHOLD, synchronous=True):

    # configure the node during startup from "the commandline"
	command = [classs, '_'+Motivation.noInputsConf+ ':=' + str(noInputs), 
	'_'+Motivation.decayConf+':='+str(decay),
	'_'+Motivation.logPeriodConf+':='+str(logPeriod)]

	g = NodeGroup("Motivation", True);
	g.addNode(command, "Motivation", "java");
	module = NeuralModule(name+'_Motivation', g, synchronous)

    # connect the decay parameter to the Nengoros network (changed online)
	module.createConfigEncoder(Motivation.topicDecay,"float", 1); 		# decay config (unconneced=Motivation.DEF_DECAY)

	module.createDecoder(Motivation.topicDataOut, "float", 2)           # decode float[]{reward,motivation}
	module.createEncoder(Motivation.topicDataIn, "float", noInputs) 	# encode input data (sum rewards here)
	
	module.createDecoder(Motivation.topicProsperity,"float", 1);		# float[]{prosperity}  = MSD from the limbo area

	return module

# adds to the network MotivationSource and constant source of config signal, returns the source
def basicConfigured(name, net, noInputs=Motivation.DEF_NOINPUTS, decay=Motivation.DEF_DECAY, logPeriod=Motivation.DEF_LOGPERIOD, 
rewVal=Motivation.DEF_REWARD, rewThr=Motivation.DEF_REWTHRESHOLD, synchronous=True): 

	# build the node
	bb = basic(name, noInputs, decay, logPeriod,rewThr,rewVal,synchronous)
	net.add(bb)

	# define the signal source which provides value of the default decay
	net.make_input(name+'_decay',[Motivation.DEF_DECAY])

	# connect to the decay port
	net.connect(name+'_decay', bb.getTermination(Motivation.topicDecay))

	return bb
