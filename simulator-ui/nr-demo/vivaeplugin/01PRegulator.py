# Agent which is controlled by simple proportional controller and navigates through the maze.
# -only distance sensors are used here (lines)
# -position of friciton sensors is under the agent (dots; friction of road 0 < friction of grass)
# 
# by Jaroslav Vitku

"""
Notes about agents: (see screenshot to this script)

Friction sensor [dots]
    -measures [binary] presence of ANY OBJECT (including grass)
    -road=0, everything else=1

Distance sensor [lines]
    -measures [continuous] distance to the nearest OBSTACLE (not a grass, not a road)

Note: value 0 to sensor properties should add 0 sensors now
"""

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

class Controller(simplemodule.SimpleModule):

    # This "constructor" is called before each simulation
    def init(self,inputdims,outputdims,numpars):

        self.inputdims=inputdims;    # no of agents sensors (2x distance [used] 2xfriction [unused] for simplicity)
        self.outputdims=outputdims;  # actuators - wheel speeds (left and right)
        self.numpars=numpars;
        self.output = range(self.outputdims)
        
        self.k = 0.12;               # regulator parameters
        self.p = 0.25;
        
    # data from agent are: [[distance sensors from left to right], [friction sensors from left to right], [speed]]
    def termination_inputs(self,values):
        self.output = [self.k+self.p*values[1], self.k+self.p*values[2]]; # more close to the wall => smaller speed on the opposite wheel
    def origin_outputs(self):
        return self.output

# stores data into csv files each time step
class DataSaver(nef.SimpleNode):
    def tick(self):

        i=file('data/sensoryData.csv','a+')  # data from robot sensors
        o=file('data/actuatorData.csv','a+') # data comming from regulator to robot actuators

        sensorydata = simulator.getAgent('a').getOrigin().getValues().getValues(); # columns: time, [distance sensors from the left, friction sensors from the left]
        actuatordata = controller.getOrigin('outputs').getValues().getValues();    # columns: time, left_wheel, right_wheel

        i.write('%1.3f,%s\n'%(self.t,list(sensorydata)))
        o.write('%1.3f,%s\n'%(self.t,list(actuatordata)))
        i.close()
        o.close()

################################################################################# here is the script
net=nef.Network('Vivae - hardwired control for agent')
net.add_to_nengo()  

numsensors=4                                # number of agents sensors (if changed, need to read correct values in the Controller)

simulator = vivae.init(net,"data/scenarios/test/walls.svg", True)
v = simulator.getControls()
v.addAgent('a',2*numsensors,    120          ,0)
v.callStartSimulation()


controller = net.add(Controller('Agent controller',2*numsensors+1, 2, 0 )); # build controller

net.connect(simulator.getAgent('a').getOrigin(),controller.getTermination('inputs'))    # connect agent with controller
net.connect(controller.getOrigin('outputs'), simulator.getAgent('a').getTermination())

saver = net.add(DataSaver('saver'))         # save data??

t=0.5;
dt=0.001;
print "OK, configuration done. Simulating network for "+repr(t)+" seconds and saving data to files"
net.run(t,dt)

print "Simulation done, will reset and show the interactive simulation window."
net.view()

print "All done."
