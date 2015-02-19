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


# sums two absolute values of inputs
class SUMABS(nef.SimpleNode):    
    def init(self):
        self.sum=0
    def termination_inputs(self,x,dimensions=2,pstc=0.01):
        self.sum=0
        if x[0] > 0:
            self.sum = x[0];
        if x[1] > 0:
            self.sum = self.sum+x[1]
    def origin_output(self):
        return [self.sum]

# absolute value of sums two inputs
class ABSSUM(nef.SimpleNode):    
    def init(self):
        self.sum=0
    def termination_inputs(self,x,dimensions=2,pstc=0.01):
        self.sum=x[0]+x[1]
        if self.sum < 0:
            self.sum=0;
    def origin_output(self):
        return [self.sum]


# sums two inputs
class SUM(nef.SimpleNode):    
    def init(self):
        self.sum=0
    def termination_inputs(self,x,dimensions=2,pstc=0.01):
        self.sum=x[0]+x[1]
    def origin_output(self):
        return [self.sum]


# substracts second from the first
class SUBSTRACT(nef.SimpleNode):    
    def init(self):
        self.sub=0
    def termination_inputs(self,x,dimensions=2,pstc=0.01):
        self.sub=x[0]-x[1]
    def origin_output(self):
        return [self.sub]
