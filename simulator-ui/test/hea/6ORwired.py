# Second HANNS, even more interresting behavior!
# TODO: EA
import nef
import random
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units

from design.ea.matrix.hnn import HNNWMatrixEA as EA
from design.ea.matrix.hnn.impl import HNNInd as Ind

from hanns import inFactory
import modules
from modules import mathNodes
from modules import routing
from hanns import spiketofloat
from hanns import floattospike
import inspect
import os
from ca.nengo.model.impl import NetworkImpl, NoiseFactory, FunctionInput, NetworkArrayImpl
from nengoros.comm.rosutils import RosUtils as RosUtils

RosUtils.prefferJroscore(False) 

actualIn=0;
actualOut=0;
# how to read input and output weights from individual in java
def setInW(w):
    useDesigned = True
    if (useDesigned):
        w = ind.getMatrixEncoder().get2DInMatrixNo(actualIn);
    else:
        for i in range(len(w)):
            for j in range(len(w[i])):
                w[i][j]=0.2
    return w;
def setOutW(w): # TODO: support for multidimensional outputs
    useDesigned=True
    if (useDesigned):
        w = ind.getMatrixEncoder().get2DOutMatrixNo(actualOut);
    else:
        for i in range(len(w)):
            for j in range(len(w[i])):
                w[i][j]=0.2
    return w;
    


# this method builds ANN from individual's "genome"
def buildModel(ind):
    # model
    model = nef.Network('model')
    # input
    inputs = model.add(routing.twoDimsRouter('inputs')); # TODO: general IO dimensions 
    
    # output
    output = inFactory.makeLifNeuron(mr, 'spike', model, name='output',intercept=0);
    #output = inFactory.makeLinearNeuron(mr, 'spike', model, name='output',intercept=0);
    #output = inFactory.makeSigmoidNeuron(mr, 'spike', model, name='output', inflection=0.4);
    CI = spiketofloat.controlledIntegrator(model,0);
    outputt = model.add(routing.getFirstDim('outputt')); # TODO: general IO dimensions
    model.connect(output,CI,transform=[1,0],pstc=0.1)
    model.connect(CI,outputt.getTermination('in'))
    ##model.connect(inputs.getOrigin('out'), output, index_pre=1)
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
        #inFactory.makeSigmoidNeuron(mr, 'spike', model, num=d);
        inFactory.makeLifNeuron(mr, 'spike', model, num=d,intercept=0);
        #inFactory.makeBioSigmoidNeuron(mr,d,model,'spike');
        # connect shared input to input neuron and neuron to ANN with weights
        actualIn=d;
        model.connect(inputs.getOrigin('out'), 'in_neuron_%d'%d, index_pre=d)
        model.connect('in_neuron_%d'%d, 'ANN',weight_func=setInW)

    return model;
    
connectNo=0;
# connect only one given index
def connectOnlyOne(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            #print repr(i)+' ' +repr(j)
            if(i==connectNo):
                w[i][j]=1
            else:
                w[i][j]=0
    return w;
def aaa(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            #print repr(i)+' ' +repr(j)
            if(i==connectNo):
                w[i][j]=11
            else:
                w[i][j]=11
    return w;

hiddenW = 0;

def setReccurent(w):
    #useDesigned=True
    print 'reccurent weights are:'
    if (useRecurrent):
        w = ind.m.getRecurrent();
        for i in range(len(w)):
            for j in range(len(w[i])):
                print w[i][j]
    else:
        for i in range(len(w)):
            for j in range(len(w[i])):
                w[i][j]=0
    return w;

    

def buildNeurons(net,N,matrix):
    model = nef.Network('model')
    # IN router
    inputs = model.add(routing.twoDimsRouter('inputs')); # TODO: general IO dimensions
    # spiking ANN
    model.make('ANN', neurons=N, dimensions=INdim, mode='spike',
        encoders = ([1,0], [0,1]), intercept= (ii, ii), max_rate = (mr,mr))
    # OUT router
    outputt = model.add(routing.sumatorFive('outputt'));    # TODO: general IO dimensions
  
    hiddenW = matrix.getW();
    inputW = matrix.getInW();
    outputW = matrix.getOutW();
  
    # FUZZY OR
    orModule = model.add(modules.mmisoOR('Fuzzy_OR_module'));
    model.connect(inputs.getOrigin('out'),orModule.getTermination('inputs0'))      # 2D from input to OR (must be 2x2)
    model.connect(inputs.getOrigin('out'),orModule.getTermination('inputs1'))      # 2D from input to OR (must be 2x2)
    zeroOrIN = [inputW[0][0],inputW[0][1]];
    orModule.swt_inputs_0(zeroOrIN);
    firstOrIN = [inputW[1][0],inputW[1][1]];
    orModule.swt_inputs_1(firstOrIN);
    
    model.connect(orModule.getOrigin('output'),orModule.getTermination('inputs_10'))# recurrent 1 
    orModule.swt_inputs_10(hiddenW[0][2]);
    model.connect(orModule.getOrigin('output'),orModule.getTermination('inputs_11'))# recurrent 2 
    orModule.swt_inputs_11(hiddenW[0][1]);
    model.connect(orModule.getOrigin('output'),outputt.getTermination('in_4'))      # directly to OUT
    outputt.swt_in_4(outputW[0][0]);
    
    outputt.swt_in_2(0); # unused inputs..
    outputt.swt_in_3(0);

    # wire it:
    model.connect('ANN','ANN',weight_func=setReccurent);    # recurrent conencitons
    # for each input, make one neuron and connect it
    for d in range(N):
        # INPUT_i
        fts = floattospike.lif(model, maxrate=mr,num=d);
        actualIn=d;
        model.connect(inputs.getOrigin('out'),'FloatToSpike_%d'%d,transform=[[abs(d),abs(d-1)]]) # 2D IN to spikes
        model.connect(fts, 'ANN', index_post=d)             # spiking input_i to ANN (neuron_i)
        
        # OUTPUT_i - integrator
        stf = spiketofloat.convertor(model,mr,d); 
        #model.connect('ANN',stf, transform=[1,0])                               # ANN to spikes
        # 
        model.connect('ANN',stf, index_pre=d,index_post=0)                               # ANN to spikes
        
        model.connect('SpikeToFloatInt_%d'%d, outputt.getTermination('in_%d'%d)) # Spikes to OUT
        result = getattr(outputt, 'swt_in_%d'%d)(d)     
        
        # wire OR module
        model.connect(orModule.getOrigin('output'),'FloatToSpike_%d'%d,transform=[[1]])  # OR to neuron
        model.connect('SpikeToFloatInt_%d'%d,orModule.getTermination('inputs_%d0'%(d+2))) # neuron to OR1
        result = getattr(orModule, 'swt_inputs_%d0'%(d+2))(10)
        model.connect('SpikeToFloatInt_%d'%d,orModule.getTermination('inputs_%d1'%(d+2))) # neuron to OR2
        result = getattr(orModule, 'swt_inputs_%d1'%(d+2))(10)
    
    return model;
        
# builds enire experiment
def buildExperiment(ind):
    net=nef.Network('EA designed recurrent HNN')  
    net.add_to_nengo();
    # generator
    # function .1 base freq, max freq 10 rad/s, and RMS of .5; 12 is a seed
    generator=FunctionInput('generator',[FourierFunction(.1, 5,.3, 12),
        FourierFunction(.5, 7, .7, 112)],Units.UNK)
    net.add(generator);
    # model
    model= buildNeurons(net,2,ind.m);
    net.add(model.network); 
    # plant
    plant = net.add(modules.OR('Fuzzy OR'));
    #plant = net.add(mathNodes.SUMABS('SUM'));
    # error estimation
    err = net.make('error',1,1,mode='direct');
    mse = net.add(modules.mse('MSE'));
    # wiring
    modelIn = model.get('inputs');  # get the input model
    model.network.exposeTermination(modelIn.getTermination('in'),'subInput'); # expose its termination
    net.connect(generator,model.network.getTermination('subInput'),weight=1)  # generator to termination
    net.connect(generator,plant.getTermination('inputs'))                     # generator to plant
    #
    modelOut = model.get('outputt');
    model.network.exposeOrigin(modelOut.getOrigin('out'),'subOutput');        # expose its termination
    net.connect(model.network.getOrigin('subOutput'), err, weight=-1)
    #
    net.connect(plant.getOrigin('output'),err)  
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
mr = 25;
ii = 0;

INdim = 2;
OUTdim = 1;
minw = -0.0;
maxw = 1;
numIns = 4;
numOuts = 3;

t = 3;
dt = 0.001;

# which setup to use?
config=1

if config == 1: # this works pretty well (approximates sum)
    useRecurrent = True
    pMut = 0.1
    pCross = 0.9;
    popsize = 3;
    maxgen = 1;


#################
ea = EA(INdim,OUTdim, numIns,numOuts, maxgen,popsize,minw,maxw);
ea.setProbabilities(pMut,pCross);
ea.initPop();
print 'starting build'
# evolution insert here
# 
expNo = 0;
print expNo
f = open('data/ea_%d.txt'%expNo, 'w');

# evolution insert here
while ea.wantsEval():
    print 'Gen: '+repr(ea.generation())+'/'+repr(maxgen)+' actual ind is ' +repr(ea.actualOne())+'/'+repr(popsize)+' best so far: '+repr(ea.getBestFitness());
    
    ind = ea.getInd();
    #ind.printMatrix();        

    error = evalInd(ind);
    ind.getFitness().setError(error);

    print 'Ind: '+repr(ea.actualOne())+' Error is: '+repr(error) +' fitness is: '+repr(ind.getFitness().get());

#    print ea.getActualWeights();

    # evaluated the last individual in the generatio? write stats
    if (ea.actualOne() == (popsize-1)):
        print 'check: '+repr(ea.generation())
        fit = ea.getBestInd().getFitness().get();
        er = ea.getBestInd().getFitness().getError();
        print '%d %.5f %.5f\n' % (ea.generation(),fit,er)
        f.write('%d %.8f %.8f\n' % (ea.generation(),fit,er))
        f.flush()
        os.fsync(f.fileno()) # just write it to disk

    # poc++ and check end of ea
    ea.nextIndividual();


f.close()

# load the best one found
ind = ea.getIndNo(ea.getBest());
net = buildExperiment(ind);
print 'best fitness is:'
print ind.getFitness().get();

print 'build done \n\n'






