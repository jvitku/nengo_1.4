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

from design.ea.matrix.ann import WMatrixEA as EA
from design.ea.matrix.ann.impl import Ind as Ind

# factories
from ca.nengo.model.neuron.impl.PoissonSpikeGenerator import LinearNeuronFactory
from ca.nengo.model.neuron.impl.PoissonSpikeGenerator import SigmoidNeuronFactory
from ca.nengo.model.neuron.impl import LIFNeuronFactory

#from hanns.ioFactory import makeInBiologicalIdentityNeuron
from hanns import inFactory


RosUtils.prefferJroscore(False) 

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
INdim = 2;
N =10;

net=nef.Network('IO neuron transfer fcn test')  


# generator
# function .1 base freq, max freq 10 rad/s, and RMS of .5; 12 is a seed
generator=FunctionInput('generator',[FourierFunction(.1, 12,.3, 12),
    FourierFunction(.5, 20, .7, 112)],Units.UNK) 
net.add(generator);


# model
model = nef.Network('model')

# input
model.make('input', neurons=INdim, dimensions=INdim, mode='direct',
    max_rate= (mr,mr), intercept=(ii,ii), encoders=[[1,0],[0,1]])

# spiking ANN
model.make('ANN', neurons=N, dimensions=INdim, mode='rate',
    encoders = ([1,0], [0,1]), intercept= (ii, ii), max_rate = (mr,mr))



# for each input, make one neuron and connect it
for d in range(INdim):
    
    #inFactory.makeLinearNeuron(100,d,model,'spike');
    #inFactory.makeBioLinearNeuron(100,d,model,'spike');
    inFactory.makeSigmoidNeuron(100, d, model,'spike');
    #inFactory.makeBioSigmoidNeuron(100,d,model,'spike');
    model.connect('input', 'in_neuron_%d'%d, index_pre=d)
    if d==1:
        model.connect('in_neuron_%d'%d, 'ANN',weight_func=ident) # todo better funcitons here
    else:
        model.connect('in_neuron_%d'%d, 'ANN',weight_func=ident)

net.add(model.network); 

# wiring
net.connect(generator,'model.input',weight=1)           # feed signal to both, plant and model
net.add_to_nengo();





