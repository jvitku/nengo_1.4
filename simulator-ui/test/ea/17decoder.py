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
from modules import routing
import inspect

from ca.nengo.model.impl import NetworkImpl, NoiseFactory, FunctionInput, NetworkArrayImpl

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

def buildcontrolledIntegrator(net):
    #input=net.make_input('integratorInput',[0])       
    control=net.make_input('integratorTunning',[-0.3])  #Create a controllable input function
    
    N=225;
    # Make a population with N neurons, 2 dimensions, and a 
    #  larger radius to accommodate large simulataneous inputs
    A=net.make('A',N,2,radius=2.5,mode='direct',quick=True)
    # Connect all the relevant objects with the relevant 1x2
    #  mappings, postsynaptic time constant is 10ms
    #net.connect(input,A,transform=[0.1,0],pstc=0.1)
    net.connect(control,A,transform=[0,2],pstc=0.1)

    def feedback(x):
        return x[0]*x[1]
    # Create the recurrent connection mapping the 1D function 'feedback'
    #  into the 2D population using the 1x2 transform
    net.connect(A,A,transform=[1,0],func=feedback,pstc=0.1)
    return A;


# this method builds ANN from individual's "genome"
def buildModel(ind):
    # model
    model = nef.Network('model')

    # input
    inputs = model.add(routing.twoDimsRouter('inputs')); # TODO: general IO dimensions 

    # output
#    output = model.make('output', neurons=OUTdim, dimensions=OUTdim, mode='spike',
 #       max_rate= (mr,mr), intercept=(ii,ii),encoders=[[1],[1]],decoder_sign=+1);
    output = inFactory.makeLifNeuron(50, 'spike', model, name='output',intercept=0);
    #output = inFactory.makeSigmoidNeuron(mr, 'spike', model, name='output');
    CI = buildcontrolledIntegrator(model);
    outputt = model.add(routing.getFirstDim('outputt')); # TODO: general IO dimensions
    
    model.connect(output,CI,transform=[2,0],pstc=0.1)
    model.connect(CI,outputt.getTermination('in'))
    model.connect(inputs.getOrigin('out'), output, index_pre=1)

    return model;
    
# builds enire experiment
def buildExperiment(ind):
    net=nef.Network('Output decoder testing')  
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
    modelIn = model.get('inputs');  # get the input model
    model.network.exposeTermination(modelIn.getTermination('in'),'subInput'); # expose its termination
    net.connect(generator,model.network.getTermination('subInput'),weight=1)  # generator to termination
    net.connect(generator,plant.getTermination('inputs'))                     # generator to plant

    modelOut = model.get('outputt');
    model.network.exposeOrigin(modelOut.getOrigin('out'),'subOutput');        # expose its termination
    net.connect(model.network.getOrigin('subOutput'),err,weight=-1)

#    net.connect('model.output',err,weight=-1)                                # compute error
    #net.connect('model.A',err,weight=-1)
#    net.connect(plant.getOrigin('output'),err)  
    net.connect(generator,err,index_pre=1)                     # generator to plant
                   
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
mr = 100;
ii = 0;

INdim = 2;
OUTdim = 1;
N = 1;
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

