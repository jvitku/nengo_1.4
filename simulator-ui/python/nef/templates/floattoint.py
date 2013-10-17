# Simple (Drag and drop template for) NeuralModule that has some inputs/outputs 
# by Jaroslav Vitku
#
# for more information how to make such template, see: http://nengo.ca/docs/html/advanced/dragndrop.html or notes/add_node_to_gui.md
#

import nef
import math
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
#from nengoros.neurons.impl.test import SecondOne as NeuralModule
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils

# node utils..
title='FloatToInt'
label='FloatToInt'
icon='F2I.png'

# parameters for initializing the node
params=[
('name','Select name for NeuralModule F2I (stands for FloatToInt)',str),
('independent','Can be group pndependent? (pushed into namespace?) select true',bool)
]

# try to instantiate node with given parameters (e.g. check name..)
def test_params(net,p):
    try:
       net.network.getNode(p['name'])
       return 'That name is already taken'
    except:
        pass


def make(net,name='NeuralModule which selects min and max from inputs and publishes as integers', 
independent=True, useQuick=True):
    

    finder = "resender.mpt.F2IPubSub";
    modemClass = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";

    # create group with a name
    g = NodeGroup(name, independent);    	# create independent group called..
    g.addNC(finder, "Finder", "java");      # start java node and name it finder
    g.addNC(modemClass,"Modem","modem")     # add modem to the group
    g.startGroup()

    modem = g.getModem()
    neuron = NeuralModule('MinMaxIntFinder_'+name, modem) # construct the smart neuron 
    neuron.createEncoder("ann2rosFloatArr", "float",4)  # termination = input of neuron (4xfloat)
    neuron.createDecoder("ros2annFloatArr", "int",2)    # origin = output of neuron (min and max)

    many=net.add(neuron)                    # add it into the network


