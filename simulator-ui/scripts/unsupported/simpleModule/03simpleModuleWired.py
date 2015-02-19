# Shows SimpleModule wired into the network, where the accuracy of approximation is measured
#
# Here, the SimpleModule is exactly the same as in previous demo, only IO dimensions are different
# Parameters of the model are set by connecting to constant.
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

import simplemodule

# definition of the model
class Model(simplemodule.SimpleModule):
    def init(self,inputdims,outputdims,numpars):
        self.inputdims=inputdims
        self.outputdims=outputdims;
        self.numpars=numpars
        self.output = range(self.outputdims)
        self.parameters = range(self.numpars)
        
    def termination_inputs(self,values):
        for i in range(0, self.outputdims):
            self.output[i]=values[i%self.inputdims] + self.parameters[i%self.numpars]*random.random()*20;
            
    def termination_params(self,parameters):
        self.parameters=parameters;
        
    def origin_outputs(self):
        return self.output

# Builds enire experiment
def buildExperiment(w):
    # PARENT NETWORK
    net=nef.Network('SimpleModule tries to approximate the Plant')  
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
    model=net.add(Model('model',2,1,3))             # system with no. of inputs,outputs,pars.
    const=net.make_input('Constant',[0.2,0.4,0.6])  # set the parameters of the model
    net.connect(const,model.getTermination('params'));

    # ERROR estimation
    err = net.make('error',1,1,mode='direct');
    mmse = net.add(modules.mse('MSE'));

    # WIRING it together
    net.connect(generator,model.getTermination('inputs'))   # generator to model
    net.connect(generator,plant.getTermination('inputs'))   # generator to plant

    net.connect(model.getOrigin('outputs'), err, weight=-1) # model to error (negative value)
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

