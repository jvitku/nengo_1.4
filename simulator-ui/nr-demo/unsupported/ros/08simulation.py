# This demo shows how to wire symple Hybrid neural system featuring Vivae simulator, MinMax node and Nural ensemble.
# by Jaroslav Vitku
import nef
import time
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl.vivae import VivaeNeuralModule as VivaeNeuron
from nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils
from nengoros.modules.impl.vivae.impl import SimulationControls as Controls

net=nef.Network('Vivae tests')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

modemCl  = "nengoros.comm.nodeFactory.modem.impl.DefaultModem"; # commands to launch default modem, vivae simulator, minmax
vv = ["./sb/../../../../simulators/vivae/build/install/vivae/bin/vivae","vivae.ros.simulatorControlsServer.ControlsServer"]
minmax = "resender.mpt.F2FPubSub";

# run vivae
g = NodeGroup("vivae", True);           # create default independent group of nodes
g.addNC(vv, "vivaeSimulator", "native");# add vivae ControlsServer (node which handles the simulation)
g.addNC(modemCl,"modem","modem")        # add a default modem..
g.startGroup()                          # start group normally

time.sleep(2)                           # give it some time to init services
modem = g.getModem()                    
simulator = VivaeNeuron('VivaeTest',modem)  # create NeuralModule which is able to add/remove agents

Controls = simulator.getControls();     # this starts the control services..
many=net.add(simulator)                 # add it to the Nengo network

Controls.loadMap('data/scenarios/arena2.svg') 
Controls.addAgent('a',4)
Controls.start()

# run minMax node
g = NodeGroup('minMaxFinder', True);# create independent group called..
g.addNC(minmax, "MinMax","java");       # start java node and name it finder
g.addNC(modemCl,"Modem", "modem")       # add modem to the group
g.startGroup()

modem = g.getModem()
neuron = NeuralModule('MinMaxFinder',    modem)      # construct the smart neuron 
neuron.createEncoder("ann2rosFloatArr", "float",4)  # termination = input of neuron (4xfloat)
neuron.createDecoder("ros2annFloatArr", "float",2)  # origin = output of neuron (min and max)
minmaxN=net.add(neuron)                    # add it into the network

# make neural ensemble 
A=net.make('Controls',neurons=50,dimensions=2,radius=2)

net.connect(simulator.getAgent('a').getOrigin(), minmaxN.getTermination('ann2rosFloatArr'))
net.connect(minmaxN.getOrigin('ros2annFloatArr'),A)
net.connect(A,simulator.getAgent('a').getTermination())

print "OK, configuration done.  -- Open the network in GUI to see hot the controls are wired."
print "-one agent created inside the vivae simulator. Agent holds his origins/terminations"
print "-agent publishes vector of 4 variables (each for one sensor) and subscribes to two values for motor velocities"
print "-smart neuron called MinMax finds the min and max value from sensory data, these values sends to neural ensemble" 


