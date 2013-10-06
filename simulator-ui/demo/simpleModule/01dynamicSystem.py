# Example of evaluating the accuraci of approxamation of behaviour of plant by a dynamic system
# Both plant and model are implemented in module "modules"
# Actual difference between model and plant signals is computed by ANN, then is sent to node
#   which computes Means Squared Error across the entire simulation
#
# by Jaroslav Vitku
import nef
import random
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units

import modules
from modules import mathNodes
from modules import dynamicSystems


# Builds enire experiment
def buildExperiment(w):
    # PARENT NETWORK
    net=nef.Network('Simple Model tries to approximate the Plant')  
    net.add_to_nengo();
    
    # SIGNAL GENERATOR
    # function .1 base freq, max freq 10 rad/s, and RMS of .5; 12 is a seed
    generator=FunctionInput('generator',[FourierFunction(.1, 5,.3, 12),
        FourierFunction(.5, 7, .7, 112)],Units.UNK)
    net.add(generator);

    # PLANT
    # can be implemented by anything (external process, signal generator, dataset..)
    plant = net.add(modules.OR('Plant - Fuzzy OR'));
    
    # MODEL
    model = net.add(modules.exampleDynamicSystem('Model - dynamic system'));
    # TODO: setup of parameter should work as weighted constant, as follows:
    #const=net.make_input('Constant',[10])  # Create a constant function
    #net.connect(const,model.getTermination('par_alpha'),weight=w);
    const=net.make_input('Constant',[w*10])  # ..This is temporary solution
    net.connect(const,model.getTermination('par_alpha'));

    # ERROR estimation
    err = net.make('error',1,1,mode='direct');
    mmse = net.add(modules.mse('MSE'));

    # WIRING it together
    net.connect(generator,model.getTermination('inputs'))   # generator to model
    net.connect(generator,plant.getTermination('inputs'))   # generator to plant

    net.connect(model.getOrigin('output'), err, weight=-1)  # model to error (negative value)
    net.connect(plant.getOrigin('output'), err, weight=+1)  # plant to error 

    net.connect(err,mmse.getTermination('error'))           # actual error to MSE

    return net;

# runs the experiment and returns value of MSE
def runExperiment(net,t,dt):
    # see: http://nengo.ca/docs/html/nef.Network.html
    net.reset()
    net.run(t,dt)
    mse = net.get('MSE')    # get MSE measuring node
    return mse.getMse()     # read MSE

# experiment setup - constants
t = 3;          # simulate for 3 seconds
dt = 0.001;     # time resolution
weight = 10;    # weight which sets value of parameter of dynamic system (@see TODO)

net = buildExperiment(weight);
error = runExperiment(net,t,dt);    # can be commented out, if you want just to build model and run it from GUI

print 'done, error is: ' + repr(error[0]) + '\n\n\n'

