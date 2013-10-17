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
title='LogicNOT'
label='LogicNOT'
icon='not.png'

# parameters for initializing the node
params=[
('name','Select name for NeuralModule NOT',str),
('independent','Can be group pndependent? (pushed into namespace?) select true',bool)
]

# try to instantiate node with given parameters (e.g. check name..)
def test_params(net,p):
    try:
       net.network.getNode(p['name'])
       return 'That name is already taken'
    except:
        pass


def make(net,name='NeuralModule which implements logical NOT operation', 
independent=True, useQuick=True):

    finder = "ctu.hanns.logic.gates.impl.NOT";
    modemClass = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";

    # create group with a name
    g = NodeGroup(name, independent);    	# create independent group called..
    g.addNC(finder, "LogicNOT", "java");      # start java node and name it finder
    g.addNC(modemClass,"Modem","modem")     # add modem to the group
    g.startGroup()

    modem = g.getModem()
    neuron = NeuralModule('NOT_'+name, modem) # construct the neural module 
    neuron.createEncoder("logic/gates/ina", "bool",1)   # termination = input of neuron (4xfloat)
    neuron.createDecoder("logic/gates/outa", "bool",1)  # origin = output of neuron (min and max)


    many=net.add(neuron)                    # add it into the network

