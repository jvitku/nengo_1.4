# signal handling (split 2D signal, convert float to spikes etc..)
# TODO get rid of this
#
# @author Jaroslav Vitku
# 
import nef
import nef.templates.learned_termination as learning
import nef.templates.gate as gating
import random

from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl import DefaultNeuralModule as SmartNeuron
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils

from design.ea.matrix.ann import WMatrixEA as EA
from design.ea.matrix.ann.impl import Ind as Ind

dd = 2;

#class router(nef.SimpleNode):
#    def init(self,numdims=2):
#        self.dims = numdims;self.vals = [0] * self.dims;
#    def termination_in(self,x,dimensions=numdims):
#        self.vals = x
#    def origin_out(self):
#        return self.vals;

# just send the signal further..
class threeDimsRouter(nef.SimpleNode):
    def init(self):
	self.vals = [0,0,0];
    def termination_in(self,x,dimensions=3):
        self.vals = x
    def origin_out(self):
        return self.vals;

# just send the signal further..
class twoDimsRouter(nef.SimpleNode):
    def init(self):
	self.vals = [0,0];
    def termination_in(self,x,dimensions=2):
        self.vals = x
    def origin_out(self):
        return self.vals;

# just send the signal further..
class oneDimRouter(nef.SimpleNode):
    def init(self):
	self.vals = [0];
    def termination_in(self,x,dimensions=1):
        self.vals = x
    def origin_out(self):
        return self.vals;

class getFirstDim(nef.SimpleNode):
    def init(self):
	self.vals = [0];
    def termination_in(self,x,dimensions=2):
        self.vals = x[0]
    def origin_out(self):
        return [self.vals];

class mergeTwoFirstDims(nef.SimpleNode):
    def init(self):
	self.vals = [0, 0];
    def termination_inA(self,x,dimensions=2):
        self.vals[0] = x[0]
    def termination_inB(self,x,dimensions=2):
        self.vals[1] = x[0]
    def origin_out(self):
        return self.vals;

class getSecondDim(nef.SimpleNode):
    def init(self):
	self.vals = [0];
    def termination_in(self,x,dimensions=2):
        self.vals = x[1]
    def origin_out(self):
        return [self.vals];

class sumatorFive(nef.SimpleNode):
    def init(self):
        self.summed = 0; self.zero = 0; self.one = 0; self.two = 0; self.three = 0; self.four=0;
        # self.in_0=0; self.in_1=0; self.in_2=0; self.in_3=0;self.in_4=0;
    def swt_in_0(self,val):
        self.in_0=val;
    def swt_in_1(self,val):
        self.in_1=val;
    def swt_in_2(self,val):
        self.in_2=val;
    def swt_in_3(self,val):
        self.in_3=val;
    def swt_in_4(self,val):
        self.in_4=val;
    def termination_in_0(self,x,dimensions=2):
        self.zero = self.in_0*x[0];
    def termination_in_1(self,x,dimensions=2):
        self.one = self.in_1*x[0];
    def termination_in_2(self,x,dimensions=2):
        self.two = self.in_2*x[0];
    def termination_in_3(self,x,dimensions=2):
        self.three = self.in_3*x[0];
    def termination_in_4(self,x,dimensions=1):
        self.four = self.in_4*x[0];
    def origin_out(self):
        self.summed = self.zero+self.one+self.two+self.three+self.four;
        return [self.summed];

class sumatorFour(nef.SimpleNode):
    def init(self):
        self.summed = 0; self.zero = 0; self.one = 0; self.two = 0; self.three = 0;
    def termination_in_0(self,x,dimensions=2):
        self.zero = x[0];
    def termination_in_1(self,x,dimensions=2):
        self.one = x[0];
    def termination_in_2(self,x,dimensions=2):
        self.two = x[0];
    def termination_in_3(self,x,dimensions=2):
        self.three = x[0];
    def origin_out(self):
        self.summed = self.zero+self.one+self.two+self.three;
        return [self.summed];
        
# build controlled integrator by means of NEF
# this controlled integrator converts single spikes to float value 
def buildcontrolledIntegrator(net):
    input=net.make_input('input',[0])       
    control=net.make_input('control',[1])  #Create a controllable input function
    
    N=225;
    # Make a population with N neurons, 2 dimensions, and a 
    #  larger radius to accommodate large simulataneous inputs
    A=net.make('A',N,2,radius=1.5,quick=True)
    # Connect all the relevant objects with the relevant 1x2
    #  mappings, postsynaptic time constant is 10ms
    net.connect(input,A,transform=[0.1,0],pstc=0.1)
    net.connect(control,A,transform=[0,1],pstc=0.1)

    def feedback(x):
        return x[0]*x[1]
    # Create the recurrent connection mapping the 1D function 'feedback'
    #  into the 2D population using the 1x2 transform
    net.connect(A,A,transform=[1,0],func=feedback,pstc=0.1)
    return net;

