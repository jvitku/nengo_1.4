import nef
import random

# constants / parameter setup etc
N = 50  # number of neurons
D = 1   # number of dimensions

RosUtils.prefferJroscore(False) # Turlte prefers roscore before jroscore (don't know why..) 



def make_model(name, neurons, dimensions, intercept=[0]):
    def mult_neg_one(x):
        return x[0] * -1 

    abs_val = nef.Network(name)

    abs_val.make('input', neurons=1, dimensions=dimensions, mode='direct')  # create input relay
    abs_val.make('output', neurons=1, dimensions=dimensions, mode='direct') # create output relay
    
    abs_val.connect('input', 'output')
    
    return abs_val.network

net = nef.Network('network')

# Create absolute value subnetwork and add it to net
net.add(make_model(name='model', dimensions=D, neurons=N))

# Create function input
net.make_input('input', values=[random.random() for d in range(D)])
net.add_to_nengo()


# Connect things up
net.connect('input', 'abs_val.input')

