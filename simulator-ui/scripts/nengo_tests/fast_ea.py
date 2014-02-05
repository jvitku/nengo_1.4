# Find RL parameters by the EA.
#
# This one starts several models in a row (evaluates a quality of given individuals). 
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

import design.ea.algorithm.impl.RealVectorEA
from design.ea.algorithm.impl import RealVectorEA
import design.ea.ind.fitness.simple.impl.RealValFitness
import design.ea.ind.genome.vector.impl.RealVector
import design.ea.ind.individual.Individual
import design.ea.strategies.mutation.RealGaussianUniformMutation

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
def buildSimulation(alpha, gamma, lambdaa, importance,expName='test0',indNo=1):
	net=nef.Network('HandWired parameters of RL node to bias')
	net.add_to_nengo()  

	#rl = rl_sarsa.qlambda("RL", noStateVars=2, noActions=4, noValues=20, logPeriod=2000)
	rl = rl_sarsa.qlambdaMOO("RL", noStateVars=2, noActions=4, noValues=20, logPeriod=2000)
	world = gridworld.benchmarkA("map_20x20","BenchmarkGridWorldNodeC",10000);
	net.add(rl)									    # place them into the network
	net.add(world)

	# connect them together
	net.connect(world.getOrigin(QLambda.topicDataIn), rl.newTerminationFor(QLambda.topicDataIn))
	net.connect(rl.getOrigin(QLambda.topicDataOut), world.getTermination(QLambda.topicDataOut))

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
	
	#if(indNo==0):
	#    saver = net.add(ProsperitySaver(expName+'.txt'))
	#    net.connect(rl.getOrigin(QLambda.topicProsperity),saver.getTermination("data"));
	    
	return net
	
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
t = 4#000
dt = 0.1
# t = 50
# dt = 0.001

#runs = 5    # TODO
base = 'ea_params_'

###################################### set the EA parameters
genomelen = 3           # alpha, gamma, lambda
popSize = 3
maxGens = 50   
pMut = 0.05             # probability of mutating each gene
stDev = 1               # standard deviation of applied gaussian to a gene
pCross = 0.8            # probability of crossing two selected inds
minw = 0
maxw = 1                # range of all parameters

ea = RealVectorEA(genomelen, False, maxGens, popSize, minw, maxw);
ea.mutate.setStdev(stDev);
ea.setProbabilities(pMut, pCross);

prevgen=0

filename = 'prosperities.txt'

# now run the EA, store the course of the prosperity (per time step) only for the first one (best one)
while(ea.wantsEval()):
    
    if(prevgen!=ea.generation()):
        prevgen=ea.generation()
        f=file(filename,'a+')
        f.write('%1.1f %1.6f %s\n'%((prevgen-1),ea.getBestInd().getFitness().getValue(),' '.join(map(str, ea.getBestInd().getGenome().getVector()))))
        f.close()
    
    ind = ea.getCurrentInd();
    indNo = ea.currentOne();
    val = ind.getGenome().getVector();
    
    name = base+"g"+str(ea.generation())+"_i"+str(ea.currentOne()) # set the name (for file storing)
    prosp = evalConfiguration(val[0],val[1],val[2],QLambda.DEF_IMPORTANCE,t,dt,name,indNo) # eval
    print '----------------- exp named: '+name+' done, the value is '+str(prosp[0])

    ind.getFitness().setValue(prosp[0]);                # set the fitness
    ea.nextIndividual();                                # continue

# Should be something about 2-3000
fitness = ea.getBestInd().getFitness().getValue();
print "==== The result is: "+ea.getBestInd().toString()

