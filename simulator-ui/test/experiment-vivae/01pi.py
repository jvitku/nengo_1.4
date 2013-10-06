"""
go for it

by Jaroslav Vitku
"""

import nef
import time
import random
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl.vivae import VivaeNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils
from nengoros.modules.impl.vivae.impl import SimulationControls as Controls
import simplemodule
import util
from vivaeLauncher import oneAgent

RosUtils.setAutorun(False)    

# controller mixes sinusoid with actual closed-loop signal
class Controller(simplemodule.SimpleModule):
    def init(self,inputdims,outputdims,numpars):

        self.inputdims=inputdims;    # no of agents sensors (2x distance [used] 2xfriction [unused] for simplicity)
        self.outputdims=outputdims;  # actuators - wheel speeds (left and right)
        self.numpars=numpars;
        self.output = range(self.outputdims)
        
        self.k = 0.12;               # regulator parameters
        self.p = 0.25;
        self.pi = 0.04;
        
        self.prevL = 0;
        self.prevR = 0;
        
    # data from agent are: [[distance sensors from left to right], [friction sensors from left to right], [speed]]
    def termination_inputs(self,values):
        self.t = self.t+1;
        self.prevL = self.prevL/self.t + values[1];         # "integrate"
        self.prevR = self.prevR/self.t + values[2];
        
        I = [self.pi*self.prevL, self.pi*self.prevR];
        P = [self.p*values[1], self.p*values[2]];       
        
        self.output = [P[0]+I[0]+self.k, P[1]+I[1]+self.k]; # add forward speed 
        
        #print 'speed '+repr(values[8])
        
    def origin_outputs(self):
        return self.output
        
class SpeedSaver(nef.SimpleNode):
    def init(self):
        self.t = 0;
        self.speeds = 0;
    def termination_data(self,values,dimensions=9):
        self.speeds = self.speeds+values[8];
        self.t = self.t+1;
    def getAvgSpeed(self):
        print 'average speed of agent was: '+repr(self.speeds/self.t)
        return self.speeds/self.t;

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

#################################################################################
simName = util.randomString(20);
net=nef.Network('Vivae - PI controller for agent')
net.add_to_nengo()  
t=1;
dt=0.001;

numsensors=4                                # number of agents sensors (if changed, need to read correct values in the Controller)

simulator = oneAgent(net,agentName='a',numSensors=numsensors,maxDistance=120, frictionDistance=0,nameSpace=simName,visible=False);
vivae = simulator.getControls();

controller = net.add(Controller('Agent controller',2*numsensors+1,2,0)); # build controller

net.connect(simulator.getAgent('a').getOrigin(),controller.getTermination('inputs'))    # connect agent with controller
net.connect(controller.getOrigin('outputs'), simulator.getAgent('a').getTermination())  # connect controller to the agent


#saver = net.add(DataSaver('saver')) # save data ??
saver = net.add(SpeedSaver('avg'))
net.connect(simulator.getAgent('a').getOrigin(),saver.getTermination('data'));

print "OK, configuration done. Simulating network for "+repr(t)+" seconds"
net.run(t,dt)

print 'avg speed was: '+repr(saver.getAvgSpeed()*dt);
vivae.setVisible(True)
print "Simulation done, will reset and show the interactive simulation window."
net.view()

