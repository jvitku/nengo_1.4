# EA which searches for the correct connection of RL module with the world and motivaiton source.
#
#  
# @author Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils

import ca.nengo.model.impl.RealOutputImpl

import design.ea.algorithm.impl.RealVectorEA
from design.ea.algorithm.impl import RealVectorEA
import design.ea.ind.fitness.simple.impl.RealValFitness
import design.ea.ind.genome.vector.impl.RealVector
import design.ea.ind.individual.Individual
import design.ea.strategies.mutation.RealGaussianUniformMutation
from ca.nengo.math.impl import ConstantFunction

import time

import rl_sarsa
import gridworld
import motivation

from org.hanns.rl.discrete.ros.sarsa import QLambda as QLambda
from org.hanns.physiology.statespace.ros import BasicMotivation as Motivation

class ProsperitySaver(nef.SimpleNode):
	def init(self):
		self.rlp = [0,0,0];
	def termination_RLprosperity(self, values, dimensions=3):
		self.rlp = values;
	def tick(self):
		#f=file('data.txt','a+')
		#print alphaIn this works
		#print rl.getOrigin(QLambda.topicProsperity).getValues().getValues(); 	# composed prosp of rl
		#print mo.getOrigin(Motivation.topicProsperity).getValues().getValues();	# 1-MSD
		ppp = rl.getOrigin(QLambda.topicProsperity).getValues().getValues();
		pp = mo.getOrigin(Motivation.topicProsperity).getValues().getValues()
		#f=file(self.name,'a+')
		f=file(expNAMEE,'a+')
		#f.write('%1.3f %s %s\n'%(self.t,str(pp[0]),' '.join(map(str, self.rlp))))
		f.write('%1.3f %s %s\n'%(self.t, str(pp[0]),' '.join(map(str, ppp))))
		f.close()

# build configuration of the experiment with given RL parameters
def buildSimulation(alpha, gamma, lambdaa, importance, expName='test0', indNo=1):
	net=nef.Network('HardwiredModel01 oneRewardD + MotivationSource _ea')
	net.add_to_nengo()  


	############################# define components
	rl = rl_sarsa.qlambdaASM("RL", noStateVars=2, noActions=4, noValues=15, logPeriod=1000)
	world = gridworld.benchmarkA("map_15x15",mapName="BenchmarkGridWorldNodeD",logPeriod=10000);
	source = motivation.basic("BasicMotivation", 1, Motivation.DEF_DECAY, logPeriod=1000) 
	
	net.add(rl)									    # place them into the network
	net.add(world)
	net.add(source)

	################################ connect components together
	# create tranform matrix which connects states dim of Motivation (reward) to the first dim of RL (reward)
	tstates=[[0 for j in range(3)] for i in range(3)]
	tstates[1][1] = 1;
	tstates[2][2] = 1;	# identity transform without first dimension (do not connect reward directly!)
	print tstates
	net.connect(world.getOrigin(QLambda.topicDataIn), rl.newTerminationFor(QLambda.topicDataIn,tstates))
	net.connect(rl.getOrigin(QLambda.topicDataOut), world.getTermination(QLambda.topicDataOut))
	
	# create tranform matrix which connects first dim of Motivation (reward) to the first dim of RL (reward)
	t=[[0 for j in range(3)] for i in range(2)] # yx (y is second dim)
	t[0][0] = 1;
	net.connect(source.getOrigin(Motivation.topicDataOut), rl.newTerminationFor(QLambda.topicDataIn,t))

	# connect RL to the RL provided by/(passed through) the Motivaton source
	treward=[[0 for j in range(1)] for i in range(3)] # yx (y is second dim)
	treward[0][0] = 1;
	net.connect(world.getOrigin(QLambda.topicDataIn), source.newTerminationFor(Motivation.topicDataIn,treward))
	
	# connect Importance input of the RL to the Motivation provided by the Motivaton source 
	timportance=[[0 for j in range(1)] for i in range(2)] # yx
	timportance[1][0] = 1;
	print timportance
	net.connect(source.getOrigin(Motivation.topicDataOut), rl.newTerminationFor(QLambda.topicImportance,timportance))

	# connect to the decay port	
	net.make_input(name+'_decay',[0.01])    #net.make_input(name+'_decay',[Motivation.DEF_DECAY])
	net.connect(name+'_decay', source.getTermination(Motivation.topicDecay))

    ########################## define the configuration
	global alphaIn	# global variables 
	global gammaIn
	global lambdaIn
	global importanceIn
	# define the parameter sources (controllable from the simulation window)
	alphaIn = net.make_input('alpha',[alpha])
	gammaIn = net.make_input('gamma',[gamma])
	lambdaIn = net.make_input('lambda',[lambdaa])
	importanceIn = net.make_input('importance',[importance])
	
	# change the params as follows: alphaIn.setFunctions([ConstantFunction(1,11)])

	# connect signal sources to the RL node
	net.connect('alpha', rl.getTermination(QLambda.topicAlpha))
	net.connect('gamma', rl.getTermination(QLambda.topicGamma))
	net.connect('lambda', rl.getTermination(QLambda.topicLambda))
	net.connect('importance', rl.getTermination(QLambda.topicImportance))
	

	
	global expNAMEE
	expNAMEE =expName
	#if(indNo==0):
	saver = net.add(ProsperitySaver(expName+'.txt'))
	net.connect(rl.getOrigin(QLambda.topicProsperity),saver.getTermination("RLprosperity"));
	
	return net
	
def configure(genome):
    if( len(genome)!= 3 ):
        print 'XXXXXXXXXXXXXXX error wrong length of genome, ignoring this!'
        return
    
    alphaIn.setFunctions([ConstantFunction(1,   genome[0])]) #dimension, const. value
    gammaIn.setFunctions([ConstantFunction(1,   genome[1])])
    lambdaIn.setFunctions([ConstantFunction(1,  genome[2])])
    
    
# build configuration and run the eperiment for given amount of time, return the prosperity
def evalConfiguration(alpha,gamma, lambdaa, importance,t,dt,name,indNo=1):
	net = buildSimulation(alpha, gamma, lambdaa, importance,name,indNo)
	
	rl = net.get("RL_QLambda")
	net.reset()
	net.run(t,dt)
	prosp = rl.getOrigin(QLambda.topicProsperity).getValues().getValues(); # read the prosperity
	return prosp;

###################################### set the simulation parameters
#t = 20	# 20/0.001= 20 000 steps ~ 10 000 RL steps 
t = 30#0#0
#t = 20000#0#0
dt = 0.1

name = 'x'

# build the model with default values
net = buildSimulation(QLambda.DEF_ALPHA,QLambda.DEF_GAMMA,QLambda.DEF_LAMBDA,0,name) 
rl = net.get("RL_QLambda")
mo = net.get("BasicMotivation_Motivation")

for i in range(1):
	expNAMEE = "expmane"+str(i)+".txt"
	net.reset()
	time.sleep(2)
	net.run(t,dt)

	#name = base+"g"+str(ea.generation())+"_i"+str(ea.currentOne()) # set the name (for file storing)
	#prosp = evalConfiguration(val[0],val[1],val[2],QLambda.DEF_IMPORTANCE,t,dt,name,indNo) # evals
	prosp = rl.getOrigin(QLambda.topicProsperity).getValues().getValues(); 		# read the prosperity
	prospMo = mo.getOrigin(Motivation.topicProsperity).getValues().getValues(); # read the prosperity

	pr = prosp[0]+prospMo[0]
	print '-------final sum of prosperities for run no: '+str(i)+' is '+str(pr)


print 'done'