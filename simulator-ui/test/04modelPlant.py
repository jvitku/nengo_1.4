# based on learn-communicate.py
# testing how the error can be computed by means of NEF
# this will be used for evaluation of solution quality provided by net

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

RosUtils.prefferJroscore(False) 

# define network that will contain classical ANN or some hybrid model
# both are connected to IO by means of IN and OUT "networks" 
def make_model(name, INdims, OUTdims, intercept=[0]):

    abs_val = nef.Network(name)

    abs_val.make('IN', neurons=1, dimensions=INdims, mode='direct')     # create input
    abs_val.make('OUT',neurons=1, dimensions=OUTdims, mode='direct')    # create output
    
    abs_val.connect('IN', 'OUT')
    
    return abs_val.network

ind = 4;
outd = 2;

net=nef.Network('Error test')       # Create the network object
net.add(make_model(name='model', INdims=ind, OUTdims=outd));
plant=net.make('plant',ind,outd) 

# function .1 base freq, max freq 10 rad/s, and RMS of .5; 12 is a seed
generator=FunctionInput('generator',[FourierFunction(.1, 10,.5, 12),
    FourierFunction(.1, 10,.5, 12),FourierFunction(.1, 10,.5, 12),FourierFunction(.1, 10,.5, 12)],
    Units.UNK) 
net.add(generator) 

# Measuring error - direct mode one neuron
diff = net.make('error', 1, 1, mode='direct')

net.connect(generator,'model.IN')           # feed signal to both, plant and model
net.connect(generator,plant)

net.connect(plant,diff)                     # compute difference
net.connect('model.OUT',diff,weight=-1)

net.add_to_nengo()


