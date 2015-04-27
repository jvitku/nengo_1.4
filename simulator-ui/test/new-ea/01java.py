# now it is doing something, with rate IO neurons

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

# OK: this is hopefully implementation of a standard ANN with weight matrix..
# both inputs are connected to one neuron (0), it excites other neurons in populations and these inhibe it
def make_model(name, INdims, OUTdims, N, intercept=[0]):

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

    mr = 100;
    ii = 0; 

    abs_val = nef.Network(name)

    abs_val.make('input', neurons=INdims, dimensions=INdims, mode='rate',
        max_rate= (mr,mr), intercept=(ii,ii),encoders=[[1,0],[0,1]])
    abs_val.make('output', neurons=OUTdims, dimensions=OUTdims, mode='rate',
        max_rate= (mr,mr), intercept=(ii,ii),encoders=[[1,0],[0,1]]) # create output relay

    # spiking ANN
    abs_val.make('ANN', neurons=N, dimensions=INdims, mode='spike',
        encoders = ([1,0], [0,1]), intercept= (ii, ii), max_rate = (mr,mr))

    abs_val.connect('ANN','ANN',weight_func=reccurent)      # recurrent conencitons
    abs_val.connect('ANN','output',weight_func=ident)       # connecitons to output

    # for each input dimension get one, create ensemble with one neuron..
    # and connect this neuron to ANN
    for d in range(INdims):
        abs_val.make('in_neuron_%d'%d, neurons=1, dimensions=1, encoders=[[1]], 
            intercept=intercept, mode='rate',max_rate=(mr,mr))
            
        abs_val.connect('input', 'in_neuron_%d'%d, index_pre=d)
        abs_val.connect('in_neuron_%d'%d, 'ANN',weight_func=ident) # todo funciton here

    return abs_val.network

ind = 2;
outd = 2;

net=nef.Network('Error test')       # Create the network object
#net.add(make_model(name='model', INdims=ind, OUTdims=outd,N=10));
#plant=net.make('plant',ind,outd) 

# function .1 base freq, max freq 10 rad/s, and RMS of .5; 12 is a seed
generator=FunctionInput('generator',
    [FourierFunction(.1, 10,.5, 12),FourierFunction(.5, 11,.6, 12)],
    Units.UNK) 

net.add(generator) 


#def runExperiment(INdim, OUTdim, N):
    

INdim = 2;
OUTdim = 1;
N = 5;
minw = -10;
maxw = 10;
popsize = 5;
maxgen = 1;

ea = EA(INdim, OUTdim, N, maxgen,popsize,minw,maxw);
ea.initPop();

while ea.wantsEval():
    print ea.actualOne();
    
    ind = ea.getInd();
    ind.printMatrix();        

    #print ea.generation();

    # poc++ and check end of ea
    ea.nextIndividual();
    


net.add_to_nengo()

