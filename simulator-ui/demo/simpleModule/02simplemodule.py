# Demo which shows how to use SimpleModule
#
# SimpleModule is similar to SimpleNode except it can have:
# 
#   -only ONE termination called inputs 
#       -dimension is set by the constructor
# 
#   -only ONE termination called params 
#       -can be used for online tunning of model parameters
#       -dimension is set by the constructor
# 
#   -arbitrary numner of origins
#       -but recommended is one origin called "outputs"
#       -dimensionality of origin is determined by calling it in time 0,0
#       -dimensionality is set by the constructor,BUT can be changed during simulation
#       -you shoudl check the size of array returned by this method yourself
#
# @author Jaroslav Vitku


import nef
import random
import simplemodule

class Model(simplemodule.SimpleModule):
    
    # This "constructor" is called before each simulation
    def init(self,inputdims,outputdims,numpars):

        self.inputdims=inputdims
        self.outputdims=outputdims;
        self.numpars=numpars
        
        # init all variables!  (e.g. zeros [0*self.numpars] == initial contitions) 
        self.output = range(self.outputdims)
        self.parameters = range(self.numpars)
        
    # termination has to be set a priori    
    def termination_inputs(self,values):
        # just generate some pseudo-random garbage based on model inputs and parameters
        for i in range(0, self.outputdims):
            self.output[i]=values[i%self.inputdims] + self.parameters[i%self.numpars]*random.random()*20;
            
    # define input for online adjusting of parameters
    def termination_params(self,parameters):
        self.parameters=parameters;

    #"The function *func* will be called once by create_origin to determine the dimensionality it returns."
    def origin_outputs(self):
        return self.output


net=nef.Network('SimpleModule demo')
net.make_input('input',range(5))            # set inputs to the model
net.make_input('parameters',[-0.2, 0.73])   # set parameters of the model

model=net.add(Model('model',5,7,2))   # build system with inputs,outputs,pars.

net.connect('input',     model.getTermination('inputs'))   # wire network
net.connect('parameters',model.getTermination('params'))

net.add_to_nengo()

print 'This is simple example how to use SimpleModule'
print 'SimpleModule has:'
print ' -one input for values of arbitrary dimensionality'
print ' -oen input for onine configuration of model parameters'
print ' -one or mode outputs'
print ''
