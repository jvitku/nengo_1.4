# by Jaroslav Vitku

import nef
import time
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl.vivae import VivaeNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils
from nengoros.modules.impl.vivae.impl import SimulationControls as Controls

Controls = simulator.getControls();     # this starts the control services..

Controls.stop()

Controls.destroy()


#Controls.loadMap('data/scenarios/arena2.svg') 
Controls.loadMap('data/scenarios/ushape.svg')
#Controls.loadMap('data/scenarios/arena1.svg') 
#Controls.loadMap('data/scenarios/manyAgents.svg') 
Controls.init()
"""
Controls.addAgent('a',4)
Controls.addAgent('b',18)
Controls.addAgent('c',18,30,50)

Controls.addAgent('d')
Controls.addAgent('e',8)
Controls.addAgent('f',20,10,50)
Controls.addAgent('g')
Controls.addAgent('h',39)
Controls.addAgent('i',22,30,1)
Controls.addAgent('j')
"""
#time.sleep(1);      # concurent modification exception
Controls.start()

print "what now?"