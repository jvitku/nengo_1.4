# EA which searches for optimal parameters alpha, gamma, lambda of a single RL module.
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

import random

class ProsperitySaver(nef.SimpleNode):
	def init(self):
		self.rlp = [0,0,0];
	def termination_RLprosperity(self, values, dimensions=3):
		self.rlp = values;
	def tick(self):

		if not SHOULD_WRITE:	# print only the best ind in the generation?
			return
		ppp = rl.getOrigin(QLambda.topicProsperity).getValues().getValues();
		pp = mo.getOrigin(Motivation.topicProsperity).getValues().getValues()
		#f=file(self.name,'a+')
		f=file(FOLDER+'/'+EXP_NAME,'a+')
		
		#f.write('%1.3f %s %s\n'%(self.t,str(pp[0]),' '.join(map(str, self.rlp))))
		f.write('%1.3f %s %s\n'%(self.t, str(pp[0]),' '.join(map(str, ppp))))
		f.close()

# build configuration of the experiment with given RL parameters
def buildSimulation(alpha, gamma, lambdaa, importance, expName='test0', indNo=1):
	net=nef.Network('HardwiredModel01 oneRewardD + MotivationSource _ea')
	net.add_to_nengo()  

	############################# define components
	rl = rl_sarsa.qlambdaASM("RL", noStateVars=2, noActions=4, noValues=15, logPeriod=1000)
	world = gridworld.benchmarkA("map_15x15",mapName="BenchmarkGridWorldNodeD",logPeriod=100000);
	source = motivation.basic("BasicMotivation", 1, Motivation.DEF_DECAY, logPeriod=10000) 
	
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
	net.make_input('decay',[0.01])    #net.make_input(name+'_decay',[Motivation.DEF_DECAY])
	net.connect('decay', source.getTermination(Motivation.topicDecay))

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
	
	#global EXP_NAME
	#global SHOULD_WRITE
	#EXP_NAME = expName
	#SHOULD_WRITE = True
	#if(indNo==0):
	saver = net.add(ProsperitySaver(expName+'.txt')) # TODO
	net.connect(rl.getOrigin(QLambda.topicProsperity),saver.getTermination("RLprosperity"));
	
	return net
	
def configure(genome):
    if( len(genome)!= 3 ):
        print 'XXXXXXXXXXXXXXX error wrong length of genome, ignoring this!'
        return
    
    alphaIn.setFunctions([ConstantFunction(1,   genome[0])]) #dimension, const. value
    gammaIn.setFunctions([ConstantFunction(1,   genome[1])])
    lambdaIn.setFunctions([ConstantFunction(1,  genome[2])])
    

def evalInd(indNo, gen, genome):
	"""
	Evalueate individual with predefined length of genome (3)
	
	:param integer indNo: index of an individual in the population
	:param ingeger gen: current number of generation
	:param genome: genome defining the individuals configuration
	:type: genome: vector of 3 real-valued numbers defining: alpha, gamma, lambda
	:returns: prosperity value if entire architecture ~ value of the fitness function
	"""
	print 'will eval this '+str(indNo)+' gen: '+str(gen)+' '+str(genome)
	global EXP_NAME #change the global variable
	global SHOULD_WRITE
	EXP_NAME = 'gen'+str(gen)+'_ind'+str(indNo)
	print EXP_NAME
	if indNo==0:        # first individual in the population (elitism=1 => the best one)
		SHOULD_WRITE=True
	else:
		SHOULD_WRITE=False
	configure(genome)   # configure the model (genotype->phenotype)
	net.reset()
	time.sleep(0.1)     # wait for ROS nodes to reset themselves
	net.run(t,dt)
	
	prosp = rl.getOrigin(QLambda.topicProsperity).getValues().getValues(); 		# read the prosperity
	prospMo = mo.getOrigin(Motivation.topicProsperity).getValues().getValues(); # read the prosperity
	
	prosperity = (prosp[0]+prospMo[0])/2
	return prosperity

###################################### set the simulation parameters
#t = 20	# 20/0.001= 20 000 steps ~ 10 000 RL steps 
t = 200#0#0
dt = 0.01

SHOULD_WRITE = False    # log data each step to file?
FOLDER = 'ea_data'		# make this folder under the simulator-ui
EXP_NAME ='TMP'         # filename to write

###################################### setup the EA

genomelen = 3           # alpha, gamma, lambda
popSize = 50
maxGens = 80  
pMut = 0.05             # probability of mutating each gene
stDev = 0.2             # standard deviation of applied gaussian to a gene
pCross = 0.8            # probability of crossing two selected inds
minw = 0
maxw = 1                # range of all parameters

ea = RealVectorEA(genomelen, False, maxGens, popSize, minw, maxw);
ea.mutate.setStdev(stDev);
ea.setProbabilities(pMut, pCross);


# build the model with default values
net = buildSimulation(QLambda.DEF_ALPHA,QLambda.DEF_GAMMA,QLambda.DEF_LAMBDA,0,'name') 
rl = net.get("RL_QLambda")
mo = net.get("BasicMotivation_Motivation")

noExperiments = 10

for i in range(noExperiments):
	FOLDER = 'ea_exp_'+str(i)
	ea = RealVectorEA(genomelen, False, maxGens, popSize, minw, maxw);
	ea.mutate.setStdev(stDev);
	ea.setProbabilities(pMut, pCross);
	
	while(ea.wantsEval()):
		ind = ea.getCurrent()
		indNo = ea.getCurrentIndex()
		gen = ea.generation()
		genome = ind.getGenome().getVector()
		prosp = evalInd(indNo, gen, [QLambda.DEF_ALPHA,QLambda.DEF_GAMMA,QLambda.DEF_LAMBDA])
		#prosp  = random.random()
		print '-------prosperity of ind '+str(indNo)+' gen: '+str(gen)+' is '+str(prosp)
	
		ind.getFitness().setValue(prosp);                	# set the fitness
		
		f=file(FOLDER+'/gen'+str(gen)+'genomes','a+')
		f.write('%1.3f %s\n'%(prosp, ' '.join(map(str, genome))))
		f.close()	# writes to file and closes it
		
		if ea.isTheLastOne():
			bi = ea.getBest()
			ge = bi.getGenome().getVector()
			print '---------------- best one in the gen: '+str(gen)+' is '+str(bi.getFitness().getValue())
		
			f=file(FOLDER+'/gen'+str(gen)+'genomes','a+')
			f.write('----------best one: \n best %1.3f %s\n'%(bi.getFitness().getValue(), ' '.join(map(str, ge))))
			f.close()	# writes to file and closes it
		
		ea.nextIndividual();                                # continue

fitness = ea.getBest().getFitness().getValue();
print "==== The result is: "+ea.getBest().toString()

best = ea.getBest()
print 'genome of the best one is: '+str(best.getGenome().getVector())

configure(best.getGenome().getVector())	# configure the acrchitecture to the best solution found
net.reset()
net.view()

#for i in range(300):
	#EXP_NAME='exp_'+str(i)
#	evalInd(i,0,[QLambda.DEF_ALPHA,QLambda.DEF_GAMMA,QLambda.DEF_LAMBDA]);

#print 'evaling'
#p  = evalInd(9000,0,[0,0,0.01])
#print p

print 'done'
