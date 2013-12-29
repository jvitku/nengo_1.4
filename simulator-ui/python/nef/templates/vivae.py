# Drag and drop template for Vivae simulator
# Externally starts ROS Vivae simulator server which is able to spawn agents
# spawns as many agents as possible and registers their actuators and sensors into the Nengo network  

# by Jaroslav Vitku
import nef
import time
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.vivae import VivaeNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
from ctu.nengoros.modules.vivae import SimulationControls as Controls
import simplemodule
import vivaeServer as vivae
#from vivae.ros.simulator.server import Sim


# node utils..
title='vivae'
label='vivae'
icon='vivaeOne.png'

# parameters for initializing the node
params=[
('name','Select name for the simulation node',str),
('independent','Can be group independent? (pushed into namespace?) select true',bool),
('mapName','Specify name of the map under the data/scenarios/ folder',str),
('numSensors','number of sensors?',int),
('maxdist','maximum distance of agents sensors',int),
('frictdist','distance of agents friction sensors?',int)
]

# try to instantiate node with given parameters (e.g. check name..)
def test_params(net,p):
    try:
        net.network.getNode(p['name'])
        return 'That name is already taken'
    except:
        pass


def make(net,name='Vivae Simulator', mapName="data/scenarios/arena1.svg", numSensors=4, maxdist=30,frictdist=50,independent=True, useQuick=True):
    numSensors = numSensors*2

    simulator = vivae.init(net, mapName, True)
    v = simulator.getControls()    
    v.callSetVisibility(True)

    agentNames = ['a','b','c','d','e','f','g','h','i','j','k','l','m']

    # run as many agents as map can hold (up to a-m)  
    for i in range(0, len(agentNames)):
        print 'adding this guy '+agentNames[i]
        v.tryToAddAgent(agentNames[i], numSensors, maxdist, frictdist)

    print 'starting the vivae simulation'

    v.callStartSimulation()
    print 'Vivae is started.'


