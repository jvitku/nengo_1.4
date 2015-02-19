# These modules are used to convert float values to spikes (probably neuron with some desired response)
#
#  by Jaroslav Vitku
# 

import inFactory
import util


# Builds NEGF ensemble with one LIF neuron which converts positive values of float to spikes
#
# to use it, for example:
#   number=1;
#   floattospike(net,number);
#   model.connect(inputs.getOrigin('out'), 'FoatToSpike_%d'%d, index_pre=d);
#   model.connect('FoatToSpike_%d'%d, 'ANN',weight_func=setInW);
def lif(net,maxrate=25,num=0,intercept=0,modee='spike'):
    name = 'FloatToSpike_%d';
    inter = [intercept];
    A = net.make(name % num, neurons=1, dimensions=1, encoders=[[1]], intercept=inter, mode=modee, max_rate=(maxrate,maxrate))
    return A;
    