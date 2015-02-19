# The same as rl_13 but here, conenction of the R directly to the R is disabled.
#
# shorter genome - 14
# # Script which will use the EA to determine connecitons between the:
#
# -motivaiton source
# -RL module 
# -gridWorld
#
# Based on the previous experiment, parameters of particular components are predefined to default values.
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
def addModel(net, modelName='model0'):
    """
    Add a new independent model into the network, return array of model components.
    These can be then used for configuring the model.
    """
    ############################# define components
    rl                = rl_sarsa.qlambdaASM(modelName, noStateVars=2, noActions=4, noValues=20, logPeriod=2000, synchronous=False, classname="org.hanns.rl.discrete.ros.sarsa.config.QLambdaCoverageRewardFile") 
    world             = gridworld.benchmarkA(modelName+"_map_15x15",mapName="BenchmarkGridWorldNodeC",logPeriod=100000);
    source            = motivation.basic(modelName, 1, Motivation.DEF_DECAY, logPeriod=10000) 

    net.add(rl)									    # place them into the network
    net.add(world)
    net.add(source)

    ################################ connect components together
    # create tranform matrix which connects states dim of GridWorld (states) to the first dim of RL (dataIn=states)
	# note: first dim is reward
    tstates           = [[0 for j in range(3)] for i in range(3)]
    tstates[1][1]     = 1;
    tstates[2][2]     = 1;	# identity transform without first dimension (do not connect reward directly!)
    #print tstates
    net.connect(world.getOrigin(QLambda.topicDataIn), rl.newTerminationFor(QLambda.topicDataIn,tstates)) # world -> rl (states)
    net.connect(rl.getOrigin(QLambda.topicDataOut), world.getTermination(QLambda.topicDataOut))			 # rl -> world (actions)

    # connect RL to the RL provided by/(passed through) the Motivaton source
    treward           = [[0 for j in range(1)] for i in range(3)] # yx (y is second dim)
    treward[0][0]     = 1;
    net.connect(world.getOrigin(QLambda.topicDataIn), source.newTerminationFor(Motivation.topicDataIn,treward)) # world -> motivation (reward)

    # create tranform matrix which connects first dim of Motivation (reward) to the first dim of RL (reward)
    trr                 = [[0 for j in range(3)] for i in range(2)] # yx (y is second dim)
    trr[0][0]           = 1;
    net.connect(source.getOrigin(Motivation.topicDataOut), rl.newTerminationFor(QLambda.topicDataIn,trr))  # motivation (r) -> rl (reward)

    # connect Importance input of the RL to the Motivation provided by the Motivaton source 
    timportance       = [[0 for j in range(1)] for i in range(2)] # yx
    timportance[1][0] = 1;
    #print timportance
    net.connect(source.getOrigin(Motivation.topicDataOut), rl.newTerminationFor(QLambda.topicImportance,timportance)) # motivation -> rl (importance)

    # connect GridWorld to the Importance input of RL module
    tsi       = [[0 for j in range(1)] for i in range(3)] # yx
    #print tsi
    net.connect(world.getOrigin(QLambda.topicDataIn), rl.newTerminationFor(QLambda.topicImportance, tsi)) # world -> rl (importance)

    # connect to the decay port	
    net.make_input(modelName+'_decay',[0.01])    #net.make_input(name+'_decay',[Motivation.DEF_DECAY])
    net.connect(modelName+'_decay', source.getTermination(Motivation.topicDecay))

    ########################## misc goes here
    net.make_input(modelName+'_importance',[0])
    net.connect(modelName+'_importance', rl.getTermination(QLambda.topicImportance))

    saver = net.add(ProsperitySaver(modelName+'_saver.txt')) # TODO no need for name here?
    net.connect(rl.getOrigin(QLambda.topicProsperity),saver.getTermination("RLprosperity"));

    # return all new components 
    return [rl, world, source, saver, modelName, tstates, modelName]

def configure(genome, model):
    if( len(genome)!= 14 ):
        print 'XXXXXXXXXXXXXXX error wrong length of genome, ignoring this one!'
        return
    
    print 'configuring model '+model[6]+'with these matrixes: [tstates, trr, timportance, tsi] stored in: '+str(genome)

    ###################################### genotype -> phenotype = read transformation matrixes from given genome 
    
    ########## tstates - all without first input dimension (reward from the world) 3x3 (2x3)
    tstates           = [[0 for j in range(3)] for i in range(3)]
    #tstates[1][0] = genome[0]
    tstates[1][1] = genome[0]
    tstates[1][2] = genome[1]
    #tstates[2][0] = genome[3]
    tstates[2][1] = genome[2]
    tstates[2][2] = genome[3] 
    print 'tstates: '+str(tstates)
    
    ############ trr  - all (2x3)
    trr                 = [[0 for j in range(3)] for i in range(2)] # yx (y is second dim)
    trr[0][0] = genome[4]
    trr[0][1] = genome[5]
    trr[0][2] = genome[6]
    trr[1][0] = genome[7]
    trr[1][1] = genome[8]
    trr[1][2] = genome[9]
    print 'trr: '+str(trr)
    
    ############ timportance - all (2x1)
    timportance       = [[0 for j in range(1)] for i in range(2)] # yx
    timportance[0][0] = genome[10]
    timportance[1][0] = genome[11]
    print 'timportance: '+str(timportance)
    
    ############### tsi - all without first input dimension (reward from the world) 3x1 (2x1)
    tsi       = [[0 for j in range(1)] for i in range(3)] # yx
    tsi[1][0] = genome[12]
    tsi[2][0] = genome[13]
    print 'tsi: '+str(tsi)
    
    ###################################### set transformation matrixes on the corresponding terminations (which implement these transformations)
    rl = model[0]
    st = rl.getTermination(QLambda.topicDataIn+"_0"); # first one created
    st.setTransformationMatrix(tstates) 
    
    trrt = rl.getTermination(QLambda.topicDataIn+"_1"); # second one
    trrt.setTransformationMatrix(trr) 
    
    tit = rl.getTermination(QLambda.topicImportance+"_0"); # first one
    tit.setTransformationMatrix(timportance) 
    
    tsit = rl.getTermination(QLambda.topicImportance+"_1"); # second one
    tsit.setTransformationMatrix(tsi) 

    
def evalInd(indNo, gen, genome, model):
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
	print 'fileName is: '+EXP_NAME
	if indNo==0:        # first individual in the population (elitism=1 => the best one)
	    SHOULD_WRITE=True
	else:
		SHOULD_WRITE=False
	configure(genome, model)   # configure the model (genotype->phenotype)
	net.reset()
	time.sleep(0.1)     # wait for ROS nodes to reset themselves
	net.run(t,dt)
	
	rl = model[0]
	mo = model[2]
	prosp = rl.getOrigin(QLambda.topicProsperity).getValues().getValues(); 		# read the prosperity
	prospMo = mo.getOrigin(Motivation.topicProsperity).getValues().getValues(); # read the prosperity
	
	#prosperity = (prosp[0]+prospMo[0])/2
	prosperity = prospMo[0]
	print 'prosperity--------------------------  '+str(prosperity)
	#print 'prosperity-------------------------- ('+str(prosp[0])+' + '+str(prospMo[0])+' )/2= '+str(prosperity)
	return prosperity

###################################### set the simulation parameters
#t = 20	# 20/0.001= 20 000 steps ~ 10 000 RL steps 
t = 200#0#0
t = 1000#0#0
#t = 1#0#0
dt = 0.01

SHOULD_WRITE = False    # log data each step to file?
FOLDER = 'generated-data/TEST_ea_data'		# make this folder under the simulator-ui
EXP_NAME ='test'         # filename to write

###################################### setup the EA

genomelen = 14           # alpha, gamma, lambda
popSize = 50
maxGens = 80
pMut = 0.05             # probability of mutating each gene
stDev = 1               # standard deviation of applied gaussian to a gene
pCross = 0.8            # probability of crossing two selected inds
minw = 0
maxw = 1                # range of all parameters

ea = RealVectorEA(genomelen, False, maxGens, popSize, minw, maxw);
ea.mutate.setStdev(stDev);
ea.setProbabilities(pMut, pCross);

net=nef.Network('EA - based RL Single R')
net.add_to_nengo()
# build the model with default values
modelA = addModel(net, 'modelA') 
global rl
rl = modelA[0]
global mo
mo = modelA[2]

# genome = [tstates, trr, timportance, tsi]
target = [1,0,0,1, 1,0,0,0,0,0, 0,1, 0,0] # hand-designed optimal solution (f=)
targett = [1,0,0,1, 0,0,0,1,0,0, 0,1, 0,0] # wrong target

######################### OLD ones!
# solution found by the EA (popSize=50,gen=80) for fitness = (prosp.Phys+prosp.RL)/2 	(f=)
# TODOfound = [0.0, 1.0, 0.512791216373, 0.0,    0.618509888649, 0.652260124683, 0.0, 0.0, 0.0, 0.0,     0.0, 0.763626277447,   0.508366048336, 0.303441107273]
# solution found by the EA (popSize=50,gen=80) for single-objective: fitness=prosp.Phys 	(f=)
gen79found = [0.0, 1.0, 0.512791216373, 0.0,    1.0, 0.0, 0.0, 0.0, 0.0, 0.0,                           0.0, 0.763626277447,   0.43853610754, 0.303441107273] # f = 0.539281487465 0.18347273767 0.30666667223 0.0602787993848
gen80found = [0.0, 1.0, 0.512791216373, 0.0,    0.618509888649, 0.652260124683, 0.0, 0.0, 0.0, 0.0,     0.0, 0.763626277447,   0.508366048336, 0.303441107273] # f = 
gen74found = [0.0, 1.0, 0.512791216373, 0.0,    1.0, 0.0, 0.0, 0.0, 0.0, 0.0,                            0.0, 0.763626277447, 0.43853610754, 0.431555271149] # f= 0.703 

################################################# NOTE!!!! 
#	-the second submatrix IS NOW: HIJ-EFG, and should be EFG-HIJ  -> switch these for publications!!
# 	-seems that all rows (Fig.7-encoding) are upside down!!
# note2: 
#   -ABCD has swoped lines, but text is exported in a swoped way, so do not change these in genome vs. paper
#   
########################## IEEE_1 ones!
moea = [0.06,1,1,0,		0,0,0,1,0,0,  1,0,0,0.23] 	# ea-designed exp_6
moga = [0,1,1,0,		1,1,0,1,0,0,  1,0,0,0] 		# ga-designed exp_9

soea = [1,0,1,1,		1,0,1,0,0,0,  0.83,1,0.9,1] 	# ea-designed exp_6  - gen 80 f=0.753
soea79 = [0.18,1,0.81,0,		1,1,1,0,0,0,  0.83,1,1,1] 	# ea-designed exp_6  - gen 79 f=0.723


soea580 = [0,1,1,0.7,		1,1,0,0,0,0,  0,0.89,1,1] 	    # ea-designed exp_5  - gen 80 f=0.745
soea579 = [0.27,1,1,1,		0.79,0.49,0,0,0,0,  0,0.89,1,1] # ea-designed exp_5  - gen 79 f=0.759

soga = [1,0,1,1,		1,0,1,0,0,0,  0,1,1,1] 		# ga-designed exp_9 - gen 80 - f=0.726

# kuz2014
gen79found = [0.0, 1.0, 0.512791216373, 0.0,    1.0, 0.0, 0.0, 0.0, 0.0, 0.0,       0.0, 0.763626277447,   0.43853610754, 0.303441107273] # f = 0.539281487465 0.18347273767 0.30666667223 0.0602787993848

############# newer SOEA and SOGA
#  best 0.753 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 0.826058030128 1.0 0.907934963703 1.0
#               a                b  c               d                   h               i               j   e     f    g     l    k                     n       m
newSOEA0680 = [1.0,             0.0, 1.0,           1.0,                1.0,            0.0,            1.0, 0.0, 0.0,0.0,   0.826058030128, 1.0,      0.907934963703, 1.0] # f = 0.753
newSOEA0679 = [0.178262799978,  1.0, 0.807606697083,0.0,                1.0,            1.0,            1.0, 0.0, 0.0, 0.0,  0.826058030128, 1.0,      1.0, 1.0]            # f=0.723
newSOEA0580 = [0.0,             1.0, 1.0,           0.712905824184,     1.0,            1.0,            0.0, 0.0, 0.0, 0.0,  0.0, 0.889706015587,      1.0, 1.0]            # f=0.745
newSOEA0579 = [0.273555487394,  1.0, 1.0,           1.0,                0.789694070816, 0.488295674324, 0.0, 0.0, 0.0, 0.0,  0.0, 0.889706015587,      1.0, 1.0]            # f=0.759

#  best 0.699 True True False True      True False True False False False True True True True
newSOGA0680 = [1,1,0,1,		1,0,1,0,0,0,  1,1,1,1] 	# f=0.699
# best 0.735 True False False True      True False False False False False True True True True
newSOGA0679 = [1,0,0,1,		1,0,0,0,0,0,  1,1,1,1] 	# f=0.699
#  best 0.715 True False True True      True True False False False False True True True True
newSOGA0580 = [1,0,1,1,		1,1,0,0,0,0,  1,1,1,1] 	# f=0.715    ///////// does not work somehow
#  best 0.696 True False True True T    rue False True False False False True True False True
newSOGA0579 = [1,0,1,1,		1,0,0,0,0,0,  1,1,0,1] 	# f=0.696
# 
# 
#################### run the individual and evaluate the fitness
#prosp = evalInd(0, 0, target, modelA)   # evaluate target one

##### old ones
#prosp = evalInd(0, 0, gen80found, modelA) 
#prosp = evalInd(0, 0, gen79found, modelA) 
#prosp = evalInd(0, 0, gen74found, modelA) 

##### new ones (IEEE_1)
#prosp = evalInd(0, 0, moea, modelA)
#prosp = evalInd(0, 0, moga, modelA)  
#prosp = evalInd(0, 0, soea, modelA)   
#prosp = evalInd(0, 0, soea79, modelA)   
#prosp = evalInd(0, 0, soea580, modelA)   
#prosp = evalInd(0, 0, soea579, modelA)   
#prosp = evalInd(0, 0, soga, modelA)   
#prosp = evalInd(0, 0, gen79found, modelA)   
#prosp = evalInd(0, 0, targett, modelA)   

prosp = evalInd(0, 0, target, modelA)   
#prosp = evalInd(0, 0, newSOEA0680, modelA)   
#prosp = evalInd(0, 0, newSOEA0679, modelA)   
#prosp = evalInd(0, 0, newSOEA0580, modelA)   
#prosp = evalInd(0, 0, newSOEA0579, modelA)   
# 
#prosp = evalInd(0, 0, newSOGA0680, modelA)   
#prosp = evalInd(0, 0, newSOGA0679, modelA)   
#prosp = evalInd(0, 0, newSOGA0579, modelA)   
#prosp = evalInd(0, 0, newSOGA0580, modelA)   
#net.reset()
#net.view()


