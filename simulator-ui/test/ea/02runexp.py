# first steps towards neuroevolution
# now works: design of models in a loop
#   models contain now: signal source (2D) subnetwork (in 2D, out 1D) containing classical ANN
# TODO:
#   use weight matrixes provided by EA
#   automatically run simulation
#   node "error" which measures MSE
#   plant..

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


def runExperiment(INdim, OUTdim, N):

    def make_model(name, INdim, OUTdim, N, intercept=[0]):

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

        model = nef.Network(name)

        model.make('input', neurons=INdim, dimensions=INdim, mode='rate',
            max_rate= (mr,mr), intercept=(ii,ii),encoders=[[1,0],[0,1]])
        model.make('output', neurons=OUTdim, dimensions=OUTdim, mode='rate',
            max_rate= (mr,mr), intercept=(ii,ii),encoders=[[1],[1]]) # create output relay

        # spiking ANN
        model.make('ANN', neurons=N, dimensions=INdim, mode='spike',
            encoders = ([1,0], [0,1]), intercept= (ii, ii), max_rate = (mr,mr))

        model.connect('ANN','ANN',weight_func=reccurent)      # recurrent conencitons
        model.connect('ANN','output',weight_func=ident)       # connecitons to output

        # for each input dimension get one, create ensemble with one neuron..
        # and connect this neuron to ANN
        for d in range(INdim):
            model.make('in_neuron_%d'%d, neurons=1, dimensions=1, encoders=[[1]], 
                intercept=intercept, mode='rate',max_rate=(mr,mr))
            
            model.connect('input', 'in_neuron_%d'%d, index_pre=d)
            model.connect('in_neuron_%d'%d, 'ANN',weight_func=ident) # todo funciton here

        return model.network


    net=nef.Network('Error test')  
    generator=FunctionInput('generator',[FourierFunction(.1, 10,.5, 12),
        FourierFunction(.5, 11,.6, 12)],Units.UNK) 

    net.add(generator);
    
    model = make_model('model', INdim, OUTdim, N);
    net.add(model);
    
    plant=net.make('plant',INdim,OUTdim) 
    diff = net.make('error', 1, 1, mode='direct')

    net.connect(generator,'model.input',weight=1)           # feed signal to both, plant and model
    net.connect(generator,plant)

    net.connect(plant,diff)                     # compute difference
    net.connect('model.output',diff,weight=-1)
    
    net.add_to_nengo();

# setup
INdim = 2;
OUTdim = 1;
N = 5;
minw = -10;
maxw = 10;
popsize = 2;
maxgen = 2;

ea = EA(INdim, OUTdim, N, maxgen,popsize,minw,maxw);
ea.initPop();

while ea.wantsEval():
    print ea.actualOne();
    
    ind = ea.getInd();
    ind.printMatrix();        

    #print ea.generation();

    runExperiment(INdim,OUTdim,N);

    # poc++ and check end of ea
    ea.nextIndividual();
    