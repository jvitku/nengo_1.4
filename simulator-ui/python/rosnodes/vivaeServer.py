# This is python interface for the project vivae/vivaesimulator SimulatorServer.java
#
# This is helper for starting the vivae simulation server from the Nengoros.
#
#
# @author Jaroslav Vitku

import nef
import time
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.vivae import VivaeNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
from ctu.nengoros.modules.vivae import SimulationControls as Controls
from vivae.ros.simulator.server import Sim
import simplemodule

# java classes
modem  = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";   
server = "vivae.ros.simulator.server.SimulatorServer"        		# start the simulator server in own thread


"""
Build the vivae simulation with loaded map and return it ready for adding agents and starting.
Usage is the following:

import vivaeServer as vivae

simulator = vivae.init(net,"data/scenarios/arena1.svg", True)
#net.add(simulator)
v = simulator.getControls()
v.addAgent('a',2*numsensors,    120          ,0)
v.callStartSimulation()

"""
def init(net, mapName=Sim.Maps.DEFAULT, visible=False):
	
    # create group of nodes
    g = NodeGroup("vivae", True);               	# create default group of nodes
    g.addNode(server, "SimulatorServer", "java");   # run the simulator..
    g.addNode(modem,"modem","modem")              	# add default modem..
    g.startGroup()                              	# start group normally

    simulator = NeuralModule('VivaeSimulator',g)  	# create NeuralModule which is able to add/remove agents

    vivae = simulator.getControls();     			# this starts the control services..
    vivae.callSetVisibility(visible);              	# make simulation window visible..
    result=net.add(simulator)                 		# add it to the Nengo network

    vivae.callLoadMap(mapName)  					# loads the map into the simulator and waits for start or agents
    return simulator;
    
