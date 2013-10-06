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
import random
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.neurons.impl.test import SecondOne as SmartNeuron
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils

from design.ea.matrix.ann import WMatrixEA as EA
from design.ea.matrix.ann.impl import Ind as Ind

from hanns import inFactory
import modules
import inspect

RosUtils.prefferJroscore(False) 

actualIn=0;
actualOut=0;
# how to read input and output weights from individual in java
def setInW(w):
    w = ind.getMatrix().get2DInMatrixNo(actualIn);
    return w;
def setOutW(w): # TODO: support for multidimensional outputs
    w = ind.getMatrix().get2DOutMatrixNo(actualOut);
    return w
def setReccurent(w):
    w = ind.getMatrix().getWeights();
    return w;

# this method builds ANN from individual's "genome"
def buildModel(ind):
    # model
    model = nef.Network('model')

    # input
    model.make('input', neurons=INdim, dimensions=INdim, mode='spike',
        max_rate= (mr,mr), intercept=(ii,ii), encoders=[[1,0],[0,1]])
    # output
    model.make('output', neurons=OUTdim, dimensions=OUTdim, mode='spike',
        max_rate= (mr,mr), intercept=(ii,ii),encoders=[[1],[1]],decoder_sign=+1);
    # spiking ANN
    model.make('ANN', neurons=N, dimensions=INdim, mode='spike',
        encoders = ([1,0], [0,1]), intercept= (ii, ii), max_rate = (mr,mr))

    # wire it:
    model.connect('ANN','ANN',weight_func=setReccurent);    # recurrent conencitons
    model.connect('ANN','output',weight_func=setOutW);      # ANN to output
    # for each input, make one neuron and connect it
    for d in range(INdim):
        # choose factory for input neurons:
        #inFactory.makeLinearNeuron(mr,d,model,'spike');
        #inFactory.makeBioLinearNeuron(mr,d,model,'spike');
        inFactory.makeSigmoidNeuron(mr, d, model,'spike');
        #inFactory.makeBioSigmoidNeuron(mr,d,model,'spike');
     
        # connect shared input to input neuron and neuron to ANN with weights
        actualIn=d;
        model.connect('input', 'in_neuron_%d'%d, index_pre=d)
        model.connect('in_neuron_%d'%d, 'ANN',weight_func=setInW)

    return model;
    
# builds enire experiment
def buildExperiment(ind):
    net=nef.Network('IO neuron transfer fcn test')  
    net.add_to_nengo();
    # generator
    # function .1 base freq, max freq 10 rad/s, and RMS of .5; 12 is a seed
    generator=FunctionInput('generator',[FourierFunction(.1, 12,.3, 12),
        FourierFunction(.5, 20, .7, 112)],Units.UNK) 
    net.add(generator);

    # model
    model= buildModel(ind);
    net.add(model.network); 

    # plant
    plant = net.add(modules.OR('Fuzzy OR'));

    # error estimation
    err = net.make('error',1,1,mode='direct');
    mse = net.add(modules.mse('MSE'));

    # wiring
    net.connect(generator,'model.input',weight=1) # feed the signal into plant and model
    net.connect(generator,plant.getTermination('inputs'))

    net.connect('model.output',err,weight=-1)
    net.connect(plant.getOrigin('output'),err)                     # compute difference
    net.connect(err,mse.getTermination('error'))
    return net;

# runs the experiment and returns value of MSE
def runExperiment(net,t,dt):
    # see: http://nengo.ca/docs/html/nef.Network.html
    net.reset()
    net.run(t,dt)
    mse = net.get('MSE')    # get MSE measuring node
    return mse.getMse()     # read MSE

def evalInd(ind):
    net = buildExperiment(ind);
    error = runExperiment(net,t,dt);
    ind.setFitness(error[0]);
    return error[0]
    

# experiment setup - constants
mr = 30;
ii = 0;

INdim = 2;
OUTdim = 1;
N = 5;
minw = -1;
maxw = 1;
popsize = 1;
maxgen = 1;

t = 5;
dt = 0.001;

# init EA
ea = EA(INdim, OUTdim, N, maxgen,popsize,minw,maxw);
ea.initPop();

# evolution insert here
while ea.wantsEval():
    print 'actual is ' +repr(ea.actualOne())

    ind = ea.getInd();
    ind.printMatrix();        
    
    error = evalInd(ind);
    print 'Ind: '+repr(ea.actualOne())+' Error is: '+repr(error) +' fitness is: '+repr(ind.getFitness().get());
    
    # poc++ and check end of ea
    ea.nextIndividual();

# load the best one found
ind = ea.getIndNo(ea.getBest());
net = buildExperiment(ind);
print 'best fitness is:'
print ind.getFitness().get();

print 'done\n\n\n'

