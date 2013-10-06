# creating one neural module - for weight matrixes
# test how weights are plotted, and used:
#
# result: the destination neural enemble must have uniform neurons
# in order to weights be printed OK, uniform must be:
#   -intercepts
#   -maximum firing rate
#
# this could be due to involving bias/threshold into weights wiever
#
# by Jaroslav Vitku

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.neurons.impl import DefaultSmartNeuron as SmartNeuron
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils


# print each weight
def myprint(w):
    print "dimensions are: [%i,%i]" % (len(w),len(w[1])) 
    
    for i in range(len(w)):
        print "-----i is here: %i range i is: %i\n" % (i,len(w))
        for j in range(len(w[i])):
            print "w[%i][%i] je tady: \t%f" % (i,j,w[i][j])
    return w

di=0.1
def const(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            w[i][j]=di
    myprint(w)
    return w

 
# set values
d=1
nd = -0.1;
def set_diagonals(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            if i==j:
                w[i][j]=d
            else:
                w[i][j]=nd #random.uniform(-0.001,0.001)
                #w[i][j]+=random.gauss(0,0.001)
    #w[1][2] = 10;
    myprint(w)
    return w

#########################################################################
# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('WM test')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

#Create a white noise input function with params: baseFreq, maxFreq [rad/s], RMS, seed
input=FunctionInput('Randomized input', [
    FourierFunction(.5, 11,1.6, 17)],Units.UNK)

net.add(input) # Add to the network and connect to neuron

# outpuc
A=net.make('A',neurons=10,dimensions=1,
#    encoders = [[1], [1], [1], [1], [1], [1], [1], [1], [1], [1]],
    intercept= [0, 0, 0, 0, 0 ,0 ,0 ,0 ,0 ,0],
    max_rate = [100, 100, 100, 100, 100, 100, 100, 100, 100, 100])

net.connect(input,A)
net.connect(A,A,weight_func=const)
