# based on learn-communicate.py
# testing how the error can be computed by means of NEF
# this will be used for evaluation of solution quality provided by net
N=60
D=1

import nef
import nef.templates.learned_termination as learning
import nef.templates.gate as gating
import random

from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units

random.seed(27)
net=nef.Network('Error test') #Create the network object

# Plant and (network) model
model=net.make('model',N,D) 
plant=net.make('plant',N,D) 

# Signal generator
# #Create a white noise input function .1 base freq, max 
           # freq 10 rad/s, and RMS of .5; 12 is a seed
generator=FunctionInput('generator',[FourierFunction(.1, 10,.5, 12)],
    Units.UNK) 
net.add(generator) 

# Measuring error - direct mode one neuron
diff = net.make('error', 1, 1, mode='direct')

net.connect(generator,model)        # feed signal to both, plant and model
net.connect(generator,plant)

net.connect(plant,diff)             # compute difference
net.connect(model,diff,weight=-1)

net.add_to_nengo()
