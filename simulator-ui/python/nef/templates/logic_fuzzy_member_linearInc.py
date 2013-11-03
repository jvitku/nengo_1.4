# Drag and drop template for the Nengo simulator which represents ROS node implementing particular fuzzy membership function.
#
# by Jaroslav Vitku
#
# for more information how to make such template, see: http://nengo.ca/docs/html/advanced/dragndrop.html or notes/add_node_to_gui.md
#

import nef
import math
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
#from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.modules.impl import DefaultAsynNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils

# node utils..
title='FuzzyMemLinInc'
label='FuzzyMemLinInc'
icon='logic_fuzzy_member_linearInc.png'

# parameters for initializing the node
params=[
('name','Select name for NeuralModule Fuzzy Membership function linear increasing',str),
('independent','Can be group pndependent? (pushed into namespace?) select true',bool)
]

# try to instantiate node with given parameters (e.g. check name..)
def test_params(net,p):
    try:
       net.network.getNode(p['name'])
       return 'That name is already taken'
    except:
        pass


def make(net,name='NeuralModule which implements FuzzyMembership function - Increasing Linear', 
independent=True, useQuick=True):

    finder = "org.hanns.logic.fuzzy.membership.impl.IncreasingLinear";

    # create group with a name
    g = NodeGroup(name, independent);   
    g.addNode(finder, "FuzzyMemLinInc", "java");     

    neuron = NeuralModule(name+"_FuzzyMemLinInc", g) 
    neuron.createEncoder("logic/gates/ina", "float",1)   	# termination = data input x 
    neuron.createEncoder("logic/gates/confa", "float",1)	# termination - config input alpha
    neuron.createEncoder("logic/gates/confb", "float",1)	# termination - config input beta
    neuron.createDecoder("logic/gates/outa", "float",1)  	# origin = output of neuron = data output y


    many=net.add(neuron)                    # add it into the network

