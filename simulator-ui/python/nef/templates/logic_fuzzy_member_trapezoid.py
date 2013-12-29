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
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils

# node utils..
title='FuzzyMemTrapezoid'
label='FuzzyMemTrapezoid'
icon='logic_fuzzy_member_trapezoid.png'

# parameters for initializing the node
params=[
('name','Select name for NeuralModule Fuzzy Membership function trapezoid',str),
('independent','Can be group pndependent? (pushed into namespace?) select true',bool)
]

# try to instantiate node with given parameters (e.g. check name..)
def test_params(net,p):
    try:
       net.network.getNode(p['name'])
       return 'That name is already taken'
    except:
        pass


def make(net,name='NeuralModule which implements FuzzyMembership function - Trapezoid', 
independent=True, useQuick=True):

    finder = "org.hanns.logic.fuzzy.membership.impl.Trapezoid";

    # create group with a name
    g = NodeGroup(name, independent);   
    g.addNode(finder, "FuzzyMemTrapezoid", "java");     

    neuron = NeuralModule(name+"_FuzzyMemTrapezoid", g) 
    neuron.createEncoder("logic/gates/ina", "float",1)   	# termination = data input x 
    neuron.createEncoder("logic/gates/confa", "float",1)	# termination - config input alpha
    neuron.createEncoder("logic/gates/confb", "float",1)	# termination - config input betaa
    neuron.createEncoder("logic/gates/confc", "float",1)	# termination - config input gamma
    neuron.createEncoder("logic/gates/confd", "float",1)	# termination - config input delta
    neuron.createDecoder("logic/gates/outa", "float",1)  	# origin = output of neuron = data output y


    many=net.add(neuron)                    # add it into the network

