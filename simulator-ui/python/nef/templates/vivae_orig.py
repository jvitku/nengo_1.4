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


def make(net,name='Vivae Simulator', mapName='arena1.svg', numSensors=4,maxdist=30,frictdist=50,independent=True, useQuick=True):
    
    numSensors = numSensors*2
    mn = 'data/scenarios/'+mapName

    # note that simulator is started externally, since SVG loaded in vivae hanged otherwise from unknown reason..
    modem  = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";   # custom modem here
    server = "vivae.ros.simulatorControlsServer.ControlsServer"         # call Vivae as a thread in Java from this process
    vv  = ["./sb/../../../../simulators/vivae/build/install/vivae/bin/vivae","vivae.ros.simulatorControlsServer.ControlsServer"]
    vvj = ["vivae.ros.simulatorControlsServer.ControlsServer"]
    
    # create group of nodes
    g = NodeGroup("vivae", True);           # if nameSpace not defined, create independent group
    #g.addNode(server,"vivaeSimulator", "java");   # run the simulator..
    #g.addNode(vv, "vivaeSimulator", "native");  # run the simulator..
    g.addNode(vvj, "vivaeSimulator", "java");  # run the simulator..
    g.addNode(modem,"modem","modem")              # add default modem..
    g.startGroup()                              # start group normally

    #modem = g.getModem()
    #time.sleep(3)    # if the process is native, it takes longer time to init the services !!      
    
    simulator = NeuralModule('VivaeSimulator',g)  # create NeuralModule which is able to add/remove agents
    
    vivae = simulator.getControls();     # this starts the control services..
    vivae.setVisible(True);              # make simulation window visible..
    many=net.add(simulator)                 # add it to the Nengo network
    """
    vivae.loadMap('data/scenarios/test/walls.svg')  

    #addAgent(name,numSensors, maxDistance, frictionSensor) 
    vivae.addAgent('a',2*numsensors,    120          ,0)
    vivae.start()
    """
    print 'loaigin'
    vivae.loadMap(mn)  
    agentNames = ['a','b','c','d','e','f','g','h','i','j','k','l','m']
    # run as many agents as map can hold (up to a-m)  
    for i in range(0, len(agentNames)):
        vivae.addAgent(agentNames[i], numSensors, maxdist, frictdist)
    print 'starting'
    vivae.start()
    
    """
    vivae.loadMap(mn)  
    agentNames = ['a','b','c','d','e','f','g','h','i','j','k','l','m']
    # run as many agents as map can hold (up to a-m)  
    for i in range(0, len(agentNames)):
        Controls.addAgent(agentNames[i], numSensors, maxdist, frictdist)    
    """
    #    vivae.start()
    print 'Vivae is ready.'


