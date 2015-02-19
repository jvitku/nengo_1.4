# implementation of nodes with purpose of evaluating the performance of individuals
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

# computes mean squared error over the entire simlation run
# here could be used tick, but this should work too
class mse(nef.SimpleNode):
    def init(self):
        self.samples=1
        self.mse=0
    def termination_error(self,x,dimensions=1,pstc=0.01):
        self.samples = self.samples+1
        self.mse = self.mse + x[0]*x[0]
    def origin_output(self):
        return [self.mse/self.samples]


    def getMse(self):
        return [self.mse/self.samples]
        
