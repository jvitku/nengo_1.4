# These functions help building IO connections for biologically plausible ANNs
#
#  by Jaroslav Vitku
# 

import nef
# factories
from ca.nengo.model.neuron.impl.PoissonSpikeGenerator import LinearNeuronFactory
from ca.nengo.model.neuron.impl.PoissonSpikeGenerator import SigmoidNeuronFactory
from ca.nengo.model.neuron.impl import LIFNeuronFactory
from ca.nengo.math.impl import IndicatorPDF


# see: public LinearNeuronFactory(PDF maxRate, PDF intercept, boolean rectified)
# implements identity - transforms input signal (x \in <-1,1>) to rate (spike/s)
# this factory is then passed to nef.Network.make method
def makeLinIdentity(maxrate,inter=-1):
    radius = 1; # x range
    mr = IndicatorPDF(maxrate-0.01,maxrate);
    intercept = IndicatorPDF(inter,inter+0.01);
    factory = LinearNeuronFactory(mr,intercept,True); # linear neuron builder
    return factory;

# Builds factory which generates neurons with response to only positive or negative input
# encoder=1 => positive representation, encoder=-1 => negative
# this factory is then passed to nef.Network.make method
def makeLinOneSided(maxrate):
    radius = 1; # x range
    mr = IndicatorPDF(maxrate-0.01,maxrate);
    intercept = IndicatorPDF(0,0.01);
    factory = LinearNeuronFactory(mr,intercept,True); # linear neuron builder
    return factory;

def makeSigmoidOneSided(maxrate,inflection):
    radius = 1;     # x range
    slope = 1;      # how sharp is sigmoid
    sl = IndicatorPDF(slope-0.01,slope)
    inflection = IndicatorPDF(inflection,inflection+0.01);
    mr = IndicatorPDF(maxrate-0.01,maxrate);
    factory = SigmoidNeuronFactory(sl,inflection,mr); # build sigmoid neuron
    return factory;


def ident(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            if i==j:
                w[i][j]=1
            else:
                w[i][j]=0
    return w


# NOTE: this one could be used for loading inputs, but 'self' is one argument which is added automatically
# this causes problems in nef_core script, so it is not used now
# pass me an individual, I will get his weight matrix and provide as weight_func
class Reader:
    def __init__(self, ind):
        self.ind = ind;
        self.inRead = 0;
        self.outRead = 0;
    def nextIn(self):
        self.inRead = self.inRead+1;
    def nextOut(self):
        self.outRead = self.outRead+1;

    def setInputWeights(self,w):
        w = ind.getMatrix().get2DInMatrixNo(self.inRead);
        return w;

    def setInputWeights(self,w):
        w = ind.getMatrix().get2DOutMatrixNo(self.outRead);
        return w;
