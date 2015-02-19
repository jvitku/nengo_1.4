# These functions help building IO connections for biologically plausible ANNs
#
#  by Jaroslav Vitku
# 

import util


# Build ensemble with one neuron implementing identity (x to rate)
# give: parent network, max rate for neuron, number of ensemble (input), mode
#       bio (T/F) specifies whether to make two neurons (for positive and negative values, or one neuron)
def makeLinearNeuron(maxrate, modee, parent, name='ensemble', num=None,intercept=-1):
    fc = util.makeLinIdentity(maxrate,intercept);
    if num is None:
        A = parent.make(name, neurons=1, dimensions=1, encoders=[[1]], node_factory=fc,
            intercept=[0], mode=modee, max_rate=(maxrate,maxrate))
    else:
        A = parent.make('in_neuron_%d'%num, neurons=1, dimensions=1, encoders=[[1]], node_factory=fc,
            intercept=[0], mode=modee, max_rate=(maxrate,maxrate))
    return A;

# Build ensemble with two neurons implementing biologically plausible response (+x to rate_1 and -x to rate_2)
# give: parent network, max rate for neuron, number of ensemble (input), mode
def makeBioLinearNeuron(maxrate, modee, parent, name='ensemble', num=None):
    fcos = util.makeLinOneSided(maxrate);
    if num is None:
        A = parent.make(name, neurons=2, dimensions=1, encoders=[[1], [-1]], node_factory=fcos,
            intercept=[0], mode=modee, max_rate=(maxrate,maxrate))
    else:
        A = parent.make('in_neuron_%d'%num, neurons=2, dimensions=1, encoders=[[1], [-1]], node_factory=fcos,
            intercept=[0], mode=modee, max_rate=(maxrate,maxrate))
    return A;

def makeSigmoidNeuron(maxrate, modee, parent, name='ensemble', num=None,inflection=0):
    fcos = util.makeSigmoidOneSided(maxrate,inflection);
    if num is None:
        A = parent.make(name, neurons=1, dimensions=1, encoders=[[1]], node_factory=fcos,
            intercept=[0], mode=modee, max_rate=(maxrate,maxrate))
    else:
        A = parent.make('in_neuron_%d'%num, neurons=1, dimensions=1, encoders=[[1]], node_factory=fcos,
            intercept=[0], mode=modee, max_rate=(maxrate,maxrate))
    return A;

def makeBioSigmoidNeuron(maxrate, modee, parent, name='ensemble', num=None,inflection=0):
    fcos = util.makeSigmoidOneSided(maxrate,inflection);
    if num is None:
        A = parent.make('in_neuron_%d'%num, neurons=2, dimensions=1, encoders=[[1], [-1]], node_factory=fcos,
            intercept=[0], mode=modee, max_rate=(maxrate,maxrate))
    else:
        A = parent.make(name, neurons=2, dimensions=1, encoders=[[1], [-1]], node_factory=fcos,
            intercept=[0], mode=modee, max_rate=(maxrate,maxrate))
    
    return A;

def makeLifNeuron(maxrate, modee, parent, name='ensemble', num=None,intercept=0):
    #fcos = util.makeSigmoidOneSided(maxrate);
    inter = [intercept];
    if num is None:
        A = parent.make(name, neurons=1, dimensions=1, encoders=[[1]], intercept=inter, mode=modee, max_rate=(maxrate,maxrate))
    else:
        A = parent.make('in_neuron_%d'%num, neurons=1, dimensions=1, encoders=[[1]], intercept=inter, mode=modee, max_rate=(maxrate,maxrate))
    return A;


