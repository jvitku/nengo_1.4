# the same as previous, but here, the coverage and reward are combined into the prosperity
# 
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
from org.hanns.rl.discrete.ros.sarsa import QLambda as QLambda
import ca.nengo.model.impl.RealOutputImpl
from org.hanns.physiology.statespace.ros import BasicMotivation as Motivation
import motivation

import rl_sarsa
import gridworld

class ProsperitySaver(nef.SimpleNode):
	def init(self):
		self.val = [0,0,0];
	def termination_data(self, values, dimensions=3):
		self.val = values;
	def tick(self):
		#f=file('data.txt','a+')
		f=file(self.name,'a+')
		f.write('%1.3f %s\n'%(self.t,' '.join(map(str, self.val))))
		f.close()

# build configuration of the experiment with given RL parameters
def buildSimulation(alpha, gamma, lambdaa, importance,expName='test0'):
	net=nef.Network('Single-motivation-driven agent architecture in a 2D map')
	net.add_to_nengo()  
	#rl = rl_sarsa.qlambda("RL", noStateVars=2, noActions=4, noValues=20, logPeriod=2000)
	rl = rl_sarsa.qlambdaMOO("RL", noStateVars=2, noActions=4, noValues=20, logPeriod=2000) # RL
	world = gridworld.benchmarkA("map_20x20","BenchmarkGridWorldNodeC",10000);              # world
	# params: name,noInputs,decay per step (from 1 to 0), log period, rewardValue, rewardThreshold
	source = motivation.basic("BasicMotivation", 1, Motivation.DEF_DECAY, 15, 1)            # motivation source
    
	net.add(rl)									    # place them into the network
	net.add(world)
	net.add(source)
	
	# connect them together
	net.connect(world.getOrigin(QLambda.topicDataIn), rl.newTerminationFor(QLambda.topicDataIn))
	net.connect(rl.getOrigin(QLambda.topicDataOut), world.getTermination(QLambda.topicDataOut))
	
	net.connect(source.getOrigin(Motivation.topicDataOut), rl.newTerminationFor(QLambda.topicDataIn,[1,0,0]))

	# define the parameter sources (controllable from the simulation window)
	net.make_input('alpha',[alpha])
	net.make_input('gamma',[gamma])
	net.make_input('lambda',[lambdaa])
	net.make_input('importance',[importance])

	# connect signal sources to the RL node
	net.connect('alpha', rl.getTermination(QLambda.topicAlpha))
	net.connect('gamma', rl.getTermination(QLambda.topicGamma))
	net.connect('lambda', rl.getTermination(QLambda.topicLambda))
	net.connect('importance', rl.getTermination(QLambda.topicImportance))
	
	saver = net.add(ProsperitySaver('data_'+expName+'.txt'))
	net.connect(rl.getOrigin(QLambda.topicProsperity),saver.getTermination("data"));
	return net
	
# build configuration and run the eperiment for given amount of time, return the prosperity
def evalConfiguration(alpha,gamma, lambdaa, importance,t,dt,name):
	net = buildSimulation(alpha, gamma, lambdaa, importance,name)
	
	rl = net.get("RL_QLambda")
	net.reset()
	net.run(t,dt)
	prosp = rl.getOrigin(QLambda.topicProsperity).getValues().getValues(); # read the prosperity
	return prosp;

#f = open('data/tmp/ea_%d.txt'%expNo, 'w');
#sx = Saver('ea_%d_agents.txt'%expNo);		# saves best agent from actual generation during the evolution into a file


#t = 20	# 20/0.001= 20 000 steps ~ 10 000 RL steps 
t = 10 #80
dt = 0.001
runs = 0
base = 'noea_moo_'
# run the experiment several times, plot average in the matlab
#for i in range(runs):
#	name = base + '_%d'%i;
#	print '----------------- starting experiment named: '+name
#	prosp = evalConfiguration(QLambda.DEF_ALPHA,QLambda.DEF_GAMMA,QLambda.DEF_LAMBDA,QLambda.DEF_IMPORTANCE,t,dt,name)
#	print '----------------- exp named: '+name+' done, the value is '+str(prosp[0])
	
#prosp = evalConfiguration(QLambda.DEF_ALPHA,QLambda.DEF_GAMMA,QLambda.DEF_LAMBDA,QLambda.DEF_IMPORTANCE,t,dt)#0.01)#


net = buildSimulation(QLambda.DEF_ALPHA,QLambda.DEF_GAMMA,QLambda.DEF_LAMBDA,QLambda.DEF_IMPORTANCE,'name')



