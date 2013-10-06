"""
Test how we can run more independent simulations which use ROS. eg. util.randomString -> namespace

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



# this generates signal which turns the agent left
class TurnGenerator(simplemodule.SimpleModule):
    #  .......... 9         2          1    [all sensors, two speeds, turning duration]
    def init(self,inputdims,outputdims,numpars):
        self.inputdims = inputdims;
        self.outputdims = outputdims;
        self.numpars = numpars;
        
        self.l = 0.1;
        self.r = 0.38;
        self.duration = 0;
        self.maxdur = 30;
        self.started = False;
        
        self.straight = [0.47, 0.47];
        self.time = 0;
        
    def termination_inputs(self,values):
        if(not self.started):
            if(values[1]==0 and self.time>1):
                self.duration = 0;
                self.started = True;
        
    def origin_outputs(self):
        if(self.started):
            # stopping criteria fulfilled?
            if(self.duration>self.maxdur):
                self.started = False;
                return self.straight;
            else:
                self.duration=self.duration+1;
                return [self.l, self.r];
        return self.straight;
        
    def termination_params(self,values):
        if(not self.started):
            self.duration = values[0];
        self.l = values[1];
        self.r = values[2];
        return;
        
    def tick(self):
        #print 'tick'
        self.time = self.time+1;
        

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
        print 'lw and rw are: '+repr(self.output[0])+ ' '+repr(self.output[1]);
        
        #print 'speed '+repr(values[8])
        
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
        
#################################################################################
simName = util.randomString(20);
net=nef.Network('Vivae - Turning controller for agent')
net.add_to_nengo()  

numsensors=4                                # number of agents sensors (if changed, need to read correct values in the Controller)

simulator = oneAgent(net,agentName='a',numSensors=numsensors,maxDistance=120, frictionDistance=0,nameSpace=simName);
vivae = simulator.getControls();

#controller = net.add(Controller('Agent controller',2*numsensors+1,2,0)); # build controller
controller = net.add(TurnGenerator('Agent controller',2*numsensors+1,2,3)); # build explicit controller
input=net.make_input('input',[20, 0.0, 0.33])
net.connect(input,controller.getTermination('params'))
net.connect(simulator.getAgent('a').getOrigin(),controller.getTermination('inputs'))    # connect agent with controller
net.connect(controller.getOrigin('outputs'), simulator.getAgent('a').getTermination())  # connect controller to the agent


#saver = net.add(DataSaver('saver')) # save data ??

t=2;
dt=0.001;
print "OK, configuration done. Simulating network for "+repr(t)+" seconds"
#net.run(t,dt)

print "Simulation done, will reset and show the interactive simulation window."
net.view()
