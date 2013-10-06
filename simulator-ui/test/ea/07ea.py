# first steps towards neuroevolution
# now works: design of models in a loop
#   models contain now: signal source (2D) subnetwork (in 2D, out 1D) containing classical ANN
# DONE:
#   use weight matrixes provided by EA
#   automatically run simulation
#   node "error" which measures MSE
#   plant..
#
# TODO:
#  evolution of ANN weights (simple)
#  evolution of weights between eural subsystems (harder..)


import nef
import nef.templates.learned_termination as learning
import nef.templates.gate as gating
import random

from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.neurons.impl.test import SecondOne as SmartNeuron
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils

from design.matrix.ann import WMatrixEA as EA
from design.matrix.ann import Ind as Ind


RosUtils.prefferJroscore(False) 


def runExperiment(INdim, OUTdim, N, infunc1,infunc2,outfunc1,recfunc,t,dt):

    # define Fuzzy OR
    class OR(nef.SimpleNode):    
        def init(self):
            self.orval=0
        def termination_inputs(self,x,dimensions=2,pstc=0.01):
            self.orval=max(x)
            if self.orval<0:
                self.orval=0
        def origin_output(self):
            return [self.orval]


    # define Fuzzy AND
    class AND(nef.SimpleNode):    
        def init(self):
            self.andval=0
        def termination_inputs(self,x,dimensions=2,pstc=0.01):
            self.andval=min(x)
            if self.andval<0:
                self.andval=0
        def origin_output(self):
            return [self.andval]

    # computes mean squared error over the entire simlation run
    # here could be used tick, but this should work too
    class mse(nef.SimpleNode):
        def init(self):
            self.samples=1
            self.mse=0
        def termination_error(self,x,dimensions=1,pstc=0.01):
            self.samples = self.samples+1
            self.mse = self.mse + x[0]*x[0]
        def origin_output(self):
            return [self.mse/self.samples]
        def getMse(self):
            return [self.mse/self.samples]
            


    def make_model(name, INdim, OUTdim, N, infunc1,infunc2,outfunc1,recfunc):

        def reccurent(w):
            for i in range(len(w)):
                for j in range(len(w[i])):
                    if i==j:
                        w[i][j]=0.1
                    else:
                        w[i][j]=-1 
            for i in range(len(w)):
                w[i][0] = 1;
            return w

        def ident(w):
            for i in range(len(w)):
                for j in range(len(w[i])):
                    if i==j:
                        w[i][j]=1
                    else:
                        w[i][j]=0
            return w

        mr = 10;
        ii = 0; 

        model = nef.Network(name)

        model.make('input', neurons=INdim, dimensions=INdim, mode='rate',
            max_rate= (mr,mr), intercept=(ii,ii),encoders=[[1,0],[0,1]])
        model.make('output', neurons=OUTdim, dimensions=OUTdim, mode='rate',
            max_rate= (mr,mr), intercept=(ii,ii),encoders=[[1],[1]]) # create output relay

        # spiking ANN
        model.make('ANN', neurons=N, dimensions=INdim, mode='spike',
            encoders = ([1,0], [0,1]), intercept= (ii, ii), max_rate = (mr,mr))

        model.connect('ANN','ANN',weight_func=recfunc)      # recurrent conencitons
        model.connect('ANN','output',weight_func=ident)       # connecitons to output

        # for each input dimension get one, create ensemble with one neuron..
        # and connect this neuron to ANN
        for d in range(INdim):
            model.make('in_neuron_%d'%d, neurons=1, dimensions=1, encoders=[[1]], 
                intercept=[0], mode='rate',max_rate=(mr,mr))
            
            model.connect('input', 'in_neuron_%d'%d, index_pre=d)
            model.connect('in_neuron_%d'%d, 'ANN',weight_func=ident) # todo funciton here

        return model.network


    net=nef.Network('Error test')  
    # function .1 base freq, max freq 10 rad/s, and RMS of .5; 12 is a seed
    generator=FunctionInput('generator',[FourierFunction(.1, 12,.3, 12),
        FourierFunction(.5, 20, .7, 112)],Units.UNK) 

    net.add(generator);
    
    model = make_model('model', INdim, OUTdim, N,infunc1,infunc2,outfunc1,recfunc);
    net.add(model);
    
    plant = net.add(OR('Fuzzy OR'));
    #plant = net.add(AND('Fuzzy AND'));
    
    err = net.make('error', 1, 1, mode='direct')
    mse = net.add(mse('MSE'))

    net.connect(generator,'model.input',weight=1)           # feed signal to both, plant and model
    net.connect(generator,plant.getTermination('inputs'))

    net.connect(plant.getOrigin('output'),err)                     # compute difference
    net.connect('model.output',err,weight=-1)
    net.connect(err,mse.getTermination('error'))
     
    net.add_to_nengo();

    # see: http://nengo.ca/docs/html/nef.Network.html
    net.reset()
    net.run(t,dt)

    return mse.getMse()

def runBest():

    ind = ea.getIndNo(ea.getBest());
    print 'loading best ind'
    ind.printMatrix();  

    # pass weights as python methods
    def getI1(w):
        w = ind.getMatrix().getInMatrixNo(0);
        return w;
        
    def getI2(w):
        w = ind.getMatrix().getInMatrixNo(1);
        return w;

    def getOut(w):
        w = ind.getMatrix().getOutMatrixNo(0);
        return w;

    def getW(w):
        w = ind.getMatrix().getWeights();
        return w;

    print 'running best ind with fitness:'
    print ind.getFitness().get();
    error = runExperiment(INdim,OUTdim,N,getI1,getI2,getOut,getW,t,dt);

# setup
INdim = 2;
OUTdim = 1;
N = 5;
minw = -1;
maxw = 1;
popsize = 5;
maxgen = 5;

t = 5;
dt = 0.001;

ea = EA(INdim, OUTdim, N, maxgen,popsize,minw,maxw);
ea.initPop();

while ea.wantsEval():
    print ea.actualOne();
    
    ind = ea.getInd();
    ind.printMatrix();        
    
    # pass weights as python methods
    def getI1(w):
        w = ind.getMatrix().getInMatrixNo(0);
        return w;
        
    def getI2(w):
        w = ind.getMatrix().getInMatrixNo(1);
        return w;

    def getOut(w):
        w = ind.getMatrix().getOutMatrixNo(0);
        return w;

    def getW(w):
        w = ind.getMatrix().getWeights();
        return w;

    #print ea.generation();
    error = runExperiment(INdim,OUTdim,N,getI1,getI2,getOut,getW,t,dt);
    ind.setFitness(error[0]);    
    print 'error is:'
    print error[0]
    print 'fitness is:'
    print ind.getFitness().get();
    # poc++ and check end of ea
    ea.nextIndividual();

runBest();



