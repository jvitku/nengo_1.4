# How does the simulator even work? :0
#
# testing here ..how IO things are called (in time with a given resolution) to pass data
#
# turns out that it works as follows:
#   -information about time is sent mainly for computing inner dynamics in networks (simpleNodes do not use it)
#   -simpleNode just:
#       -receives, processes and sets origin ONE VALUE at one time-step (no time representation)
#       -these new values will be available (will be red on connected terms.) on the origin at the NEXT time step
#
# @author Jaroslav Vitku


import nef
import random
import simplemodule
from ca.nengo.model import Units
from ca.nengo.model.impl import NetworkImpl, NoiseFactory, FunctionInput, NetworkArrayImpl

class Model(simplemodule.SimpleModule):
    def init(self,inputdims,outputdims,numpars):
        self.inputdims=inputdims
        self.outputdims=outputdims;
        self.numpars=numpars
        self.output = range(self.outputdims)
        self.parameters = range(self.numpars)
        print 'A-inited here'
    def termination_params(self,parameters):
        self.parameters=parameters;
    def termination_inputs(self,values):
        print 'A-termination: '+repr(values);
        self.output=values;
    def origin_outputs(self):
        print 'A-origiiiiiin: '+repr(self.output)
        return self.output
    def tick(self):
        print 'A-tick'

class Modell(simplemodule.SimpleModule):
    def init(self,inputdims,outputdims,numpars):
        self.inputdims=inputdims
        self.outputdims=outputdims;
        self.numpars=numpars
        self.output = range(self.outputdims)
        self.parameters = range(self.numpars)
        print 'B-inited here'
    def termination_params(self,parameters):
        self.parameters=parameters;
    def termination_inputs(self,values):
        print 'B-termination: '+repr(values);
        self.output=values;
    def origin_outputs(self):
        print 'B-origiiiiiin: '+repr(self.output)
        return self.output
    def tick(self):
        print 'B-tick'# + repr(self.dt)


net=nef.Network('SimpleModule demo')
#net.make_input('input',range(5))            # set inputs to the model
generator=FunctionInput('input',[FourierFunction(.1, 5,.3, 12),
    FourierFunction(.5, 7, .7, 112)],Units.UNK)
net.add(generator);
net.make_input('parameters',[-0.2, 0.73])   # set parameters of the model
model=net.add(Model('model',2,2,2))   # build system with inputs,outputs,pars.

net.connect('input',     model.getTermination('inputs'))   # wire network
net.connect('parameters',model.getTermination('params'))

modell=net.add(Modell('modell',2,2,2))   # build system with inputs,outputs,pars.
net.connect(model.getOrigin('outputs'),modell.getTermination('inputs'))

net.add_to_nengo()
