# These modules are used to convert spikes to float values (some integration probably)
#
#  by Jaroslav Vitku
# 

import util
import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model.impl import NetworkImpl, NoiseFactory, FunctionInput, NetworkArrayImpl
import inFactory

# Converts incomming spikes to float value by use of controlled integrator
# Integrator is implemented by means of NEF-designed ANN and integration can be
# tunned by inputVal
#
# to use it:
# 
#   CI = spiketofloat.controlledIntegrator(model);
#   net.connect(output,CI,transform=[1,0],pstc=0.1)
#   model.connect(CI,outputt.getTermination('in'))
def controlledIntegrator(net,num,inputVal = -1):
    N=225;          # number of neurons in the net
    rad = 2.5;
    
    control=net.make_input('Tunning_%d'%num,[inputVal])
    # Make a population with N neurons, 2 dimensions, and a 
    #  larger radius to accommodate large simulataneous inputs
    A=net.make('SpikeToFloatInt_%d'%num, N, 2, radius=rad, mode='direct', quick=True)
    # Connect all the relevant objects with the relevant 1x2
    #  mappings, postsynaptic time constant is 10 ms
    #net.connect(input,A,transform=[0.1,0],pstc=0.1)
    net.connect(control,A,transform=[0,2],pstc=0.1)

    def feedback(x):
        return x[0]*x[1]
    # Create the recurrent connection mapping the 1D function 'feedback'
    #  into the 2D population using the 1x2 transform
    net.connect(A,A,transform=[1,0],func=feedback,pstc=0.1)
    return A;


def convertor(net,mr,no):
    CI = controlledIntegrator(net,no);             # build integrator
    output = inFactory.makeLifNeuron(mr, 'spike', net, name='SpikeToFloat_%d'%no,intercept=0); # build n.
    net.connect(output,CI,transform=[1,0]);     # connect them
    return output
    
    
    
    