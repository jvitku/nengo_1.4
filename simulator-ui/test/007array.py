import nef
import nef.templates.learned_termination as learning
import nef.templates.gate as gating
import random

from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.neurons.impl.test import SecondOne as SmartNeuron
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils



net=nef.Network('Squaring Array')
input=net.make_input('input',[0,0,0,0,0])
A=net.make_array('A',neurons=100,length=5)
B=net.make('B',neurons=100,dimensions=1)
net.connect(input,A)
def square(x):
  return x[0]*x[0]
net.connect(A,B,transform=[1,1,1,1,1],func=square)

net.add_to_nengo();