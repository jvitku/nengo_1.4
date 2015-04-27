# Example model composed of:
#	-RL module with 2D input and 4D output (1ofN ~ 4 actions)
#	-GridWorld module representing simple ismulation environment
#
# Gridworld is composed of 2D grid map with obstacles and one reinforcement
# The reinforcement source is connected directly to the RL reinforcement input
# Agent learns how to reach the reward
# 
# The simulation is ran for 40 000 simulation steps and stopped
# The command net.view() resets the learned knowledge and opens interacvite Nengo gui
# 
# @author Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
from org.hanns.rl.discrete.ros.sarsa import QLambda as QLambda
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
def buildSimulation(alpha, gamma, lambdaa, importance, expName='test0', indNo=1):
	net=nef.Network('HardwiredModel01 oneReward')
	net.add_to_nengo()  

	#rl = rl_sarsa.qlambda("RL", noStateVars=2, noActions=4, noValues=20, logPeriod=2000)
	rl = rl_sarsa.qlambdaMOO("RL", noStateVars=2, noActions=4, noValues=20, logPeriod=200)
	world = gridworld.benchmarkA("map_20x20","BenchmarkGridWorldNodeC",10000);
	net.add(rl)									    # place them into the network
	net.add(world)

	# connect them together
	net.connect(world.getOrigin(QLambda.topicDataIn), rl.newTerminationFor(QLambda.topicDataIn))
	net.connect(rl.getOrigin(QLambda.topicDataOut), world.getTermination(QLambda.topicDataOut))

	global alphaIn # global variables that can be changed outside of this method
	global gammaIn
	global lambdaIn
	global importanceIn
	# define the parameter sources (controllable from the simulation window)
	alphaIn = net.make_input('alpha',[alpha])
	gammaIn = net.make_input('gamma',[gamma])
	lambdaIn = net.make_input('lambda',[lambdaa])
	importanceIn = net.make_input('importance',[importance])
	
	# seems to work: alphaIn.setFunctions([ConstantFunction(1,11)])

	# connect signal sources to the RL node
	net.connect('alpha', rl.getTermination(QLambda.topicAlpha))
	net.connect('gamma', rl.getTermination(QLambda.topicGamma))
	net.connect('lambda', rl.getTermination(QLambda.topicLambda))
	net.connect('importance', rl.getTermination(QLambda.topicImportance))
	
	#if(indNo==0):
	#    saver = net.add(ProsperitySaver(expName+'.txt'))
	#    net.connect(rl.getOrigin(QLambda.topicProsperity),saver.getTermination("data"));
	    
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
t = 4000#0#0
dt = 0.1
# t = 50
# dt = 0.001

#runs = 5    # TODO
base = 'ea_params_'

###################################### set the EA parameters
#genomelen = 3           # alpha, gamma, lambda
#popSize = 3
#maxGens = 1   
#pMut = 0.05             # probability of mutating each gene
#stDev = 1               # standard deviation of applied gaussian to a gene
#pCross = 0.8            # probability of crossing two selected inds
#minw = 0
#maxw = 1                # range of all parameters

#ea = RealVectorEA(genomelen, False, maxGens, popSize, minw, maxw);
#ea.mutate.setStdev(stDev);
#ea.setProbabilities(pMut, pCross);

prevgen=0

filename = 'prosperities.txt'
evaluated = 0
#TODO
name = 'x'
indNo=0

# build the model with default values
net = buildSimulation(QLambda.DEF_ALPHA,QLambda.DEF_GAMMA,QLambda.DEF_LAMBDA,QLambda.DEF_IMPORTANCE,name,indNo) 
rl = net.get("RL_QLambda")

#importanceIn.setFunctions([ConstantFunction(1,0.5)]) # TODO find some optimal value
#alphaIn.setFunctions([ConstantFunction(1,11)])
    
net.reset()
time.sleep(2)
net.run(t,dt)

#name = base+"g"+str(ea.generation())+"_i"+str(ea.currentOne()) # set the name (for file storing)
#prosp = evalConfiguration(val[0],val[1],val[2],QLambda.DEF_IMPORTANCE,t,dt,name,indNo) # evals
prosp = rl.getOrigin(QLambda.topicProsperity).getValues().getValues(); # read the prosperity

print '----------------- exp named: '+name+' done, the value is '+str(prosp[0])

#ind.getFitness().setValue(prosp[0]);                # set the fitness
#ea.nextIndividual();                                # continue

print 'done'

# Should be something about 2-3000
#fitness = ea.getBestInd().getFitness().getValue();
#print "==== The result is: "+ea.getBestInd().toString()
