# implementation of very basic fuzzy logic modules
# by Jaroslav Vitku
import nef
import nef.templates.learned_termination as learning
import nef.templates.gate as gating
import random

from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.impl import DefaultNeuralModule as SmartNeuron
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils

from design.ea.matrix.ann import WMatrixEA as EA
from design.ea.matrix.ann.impl import Ind as Ind


# define Fuzzy OR
class OR(nef.SimpleNode):    
    def init(self):
        self.orval=0
    def termination_inputs(self,x,dimensions=2):
        self.orval=max(x)
        if self.orval<0:
            self.orval=0
        if self.orval>1:
            #print 'OR: do not send me membership values greater than 1, ok?'
            self.orval=1
    def origin_output(self):
        return [self.orval]

# define Fuzzy OR, with multiple inputs which are summed together
class mmisoOR(nef.SimpleNode):    
    def init(self):
        self.orval=0
        self.firstDim=[0,0,0,0,0];    
        self.secondDim=[0,0,0,0,0];   #each dimension is something like dendritic tree
      #  self.in_0=[10,10];
       # print 'seeeeeeetting here ok, vals are: %d %d'%(self.in_0[0],self.in_0[1]);
        #self.in_10=0;
        #self.in_11=0;
        #self.in_20=0;
        #self.in_21=0;
        #self.in_30=0;
        #self.in_31=0;
    # set weights
    def swt_inputs_0(self,vals):    # give me two values (2d input)
        self.in_0=vals;
    def swt_inputs_1(self,vals):    # give me two values (2d input)
        self.in_1=vals;
        #print 'ok, vals are: %d %d'%(self.in_0[0],self.in_0[1]);
    def swt_inputs_10(self,val):
        self.in_10=val;
    def swt_inputs_11(self,val):
        self.in_11=val;
    def swt_inputs_20(self,val):
        self.in_20=val;
    def swt_inputs_21(self,val):
        self.in_21=val;
    def swt_inputs_30(self,val):
        self.in_30=val;
    def swt_inputs_31(self,val):
        self.in_31=val;
        
    def sigmoid(self,x):
        if(x<0):
            return 0;
        return x;
            
    def termination_inputs0(self,x,dimensions=2):
        self.firstDim[0] = self.sigmoid(self.in_0[0]*x[0]);
        self.secondDim[0] = self.sigmoid(self.in_0[1]*x[1]);
    def termination_inputs1(self,x,dimensions=2):
        self.firstDim[4] = self.sigmoid(self.in_1[0]*x[0]);
        self.secondDim[4] = self.sigmoid(self.in_1[1]*x[1]);
        #print 'ok, vals are: %d %d'%(self.in_0[0],self.in_0[1]);
        #print 'received: %d %d by:%d %d %d'%(self.firstDim[0],self.secondDim[0],self.in_0[0],x[0],x[1])
    def termination_inputs_10(self,x,dimensions=1): # first input to first dimension.. :(
        self.firstDim[1] = self.sigmoid(self.in_10*x[0]);
    def termination_inputs_11(self,x,dimensions=1): # second to first dim
        self.secondDim[1] =self.sigmoid(self.in_11* x[0]);
    def termination_inputs_20(self,x,dimensions=2):# this thing just gets second dimension (connected to integrator)
        self.firstDim[2] = self.sigmoid(self.in_20*x[1]);
    def termination_inputs_21(self,x,dimensions=2):
        self.secondDim[2] = self.sigmoid(self.in_21*x[1]);
    def termination_inputs_30(self,x,dimensions=2):
        self.firstDim[3] = self.sigmoid(self.in_30*x[1]);
    def termination_inputs_31(self,x,dimensions=2):
        self.secondDim[3] = self.sigmoid(self.in_31*x[1]);
        
    def origin_output(self):
#        print 'daata hare: %f %f'%(x[0],x[1])
        first = sum(self.firstDim);
        second = sum(self.secondDim);
        self.orval=max(first,second)
        if self.orval>1:
            self.orval=1;
 #       if self.orval<0:
  #          self.orval=0;
        return [self.orval]

# define Fuzzy OR, with multiple inputs which are summed together
class misoOR(nef.SimpleNode):    
    def init(self):
        self.orval=0
        self.firstDim=[0,0,0,0];    
        self.secondDim=[0,0,0,0];   #each dimension is something like dendritic tree
    def termination_inputs_0(self,x,dimensions=2):
        self.firstDim[0] = x[0];
        self.secondDim[0] = x[1];
    def termination_inputs_1(self,x,dimensions=2):
        self.firstDim[1] = x[0];
        self.secondDim[1] = x[1];
    def termination_inputs_2(self,x,dimensions=2):
        self.firstDim[2] = x[0];
        self.secondDim[2] = x[1];
    def termination_inputs_3(self,x,dimensions=2):
        self.firstDim[3] = x[0];
        self.secondDim[3] = x[1];
    def origin_output(self):
        first = sum(self.firstDim);
        second = sum(self.secondDim);
        self.orval=max(first,second)
        if self.orval>1:
            self.orval=1
        return [self.orval]


# define Fuzzy AND
class AND(nef.SimpleNode):    
    def init(self):
        self.andval=0
    def termination_inputs(self,x,dimensions=2):
        self.andval=min(x)
        if self.andval<0:
            self.andval=0
        if self.andval>1:
            #print 'AND: do not send me membership values greater than 1, ok?'
            self.andval=1
    def origin_output(self):
        return [self.andval]
