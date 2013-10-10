# How does the simulator even work? :0
#
# turns out that it works as follows:
#   -information about time is sent mainly for computing inner dynamics in networks (simpleNodes do not use it)
#   -simpleNode just:
#       -receives, processes and sets origin ONE VALUE at one time-step (no time representation)
#       -these new values will be available (will be red on connected terms.) on the origin at the NEXT time step
#
#   -this demo introduces direct acces to time, node called model1 has no acces to time, but the model2 has
#   -this can be useful for example for modelling of dynamical systems
#   -time information "start" and "end" define when the time-slot (space between time samples) starts and ends
#   -simulation length and sampling frequency can be modified in simulation interactive GUI (bottom) or in script by:
#       net.reset()
#       net.run(t,dt)  # how long to run and sampling period
#
# @author Jaroslav Vitku


import nef
import random
import simplemodule
from ca.nengo.model import Units
from ca.nengo.model.impl import NetworkImpl, NoiseFactory, FunctionInput, NetworkArrayImpl

class ModelB(simplemodule.SimpleModule):
    def init(self,inputdims,outputdims,numpars):
        self.inputdims=inputdims
        self.outputdims=outputdims;
        self.numpars=numpars
        self.output = range(self.outputdims)
        self.parameters = range(self.numpars)
        print 'B-inited here'
    def termination_t_params(self,parameters,start,end):
        self.parameters=parameters;
    def termination_t_inputs(self,values,start,end):
        print 'B-termination: received this: '+repr(values)+' with these times: '+repr(start)+' '+repr(end);
        self.output=values;
    def origin_t_outputs(self,start,end):
        print 'B-origiiiiiin: '+repr(self.output)+' with these times: '+repr(start)+' '+repr(end);
        return self.output
    def tick(self):
        print 'B-tick'

class ModelA(simplemodule.SimpleModule):
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
        print 'A-termination: received this: '+repr(values)
        self.output=values;
    def origin_outputs(self):
        print 'A-origiiiiiin: '+repr(self.output)
        return self.output
    def tick(self):
        print 'A-tick'

net=nef.Network('SimpleModule demo')
#net.make_input('input',range(5))            # set inputs to the model
generator=FunctionInput('input',[FourierFunction(.1, 5,.3, 12),
    FourierFunction(.5, 7, .7, 112)],Units.UNK)
net.add(generator);
net.make_input('parameters',[-0.2, 0.73])   # set parameters of the model
modelA=net.add(ModelA('modelA',2,2,2))   # build system with inputs,outputs,pars.

modelB=net.add(ModelB('modelB',2,2,2))   # build system which has information about time 
net.connect('input',     modelA.getTermination('inputs'))   # wire inputs to modelA
net.connect('parameters',modelA.getTermination('params'))   # set parameters
net.connect(modelA.getOrigin('outputs'),modelB.getTermination('inputs'))   # wire modelA to modelB

net.add_to_nengo()
