"""
Net has two nodes: 
    -summer which sums control signal with some bias
    -temp signal changer which is triggered by event (zero on left sensor for now)         
        and generates signal on its outputs for given period of time
        
This is demonstration of hybrid network, where nodes does not have to be neurons, but can implement
top-down designed algorithms, or "events" in this case

# by Jaroslav Vitku


Notes about agents: (see screenshot to this script)

Friction sensor [dots]
    -measures [binary] presence of ANY OBJECT (including grass)
    -black and blue lines gere
    -road=0, everything other=1

Distance sensor [lines]
    -measures [continuous] distance to the nearest OBSTACLE (not grass, not road)

Note: value 0 to sensor properties should add 0 sendors now
"""

import nef
import time
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from nengoros.modules.impl.vivae import VivaeNeuralModule as NeuralModule
from nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from nengoros.comm.rosutils import RosUtils as RosUtils
from nengoros.modules.impl.vivae.impl import SimulationControls as Controls
import simplemodule
from vivaeLauncher import oneAgent
#RosUtils.setAutorun(False)     # Do we want to autorun roscore and rxgraph? (true by default)

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
        self.maxdur = 20;
        self.started = False;
        self.straight = [0, 0];
        self.time = 0;
        
    def termination_inputs(self,values):
        if(not self.started):
            if(values[1]==0 and self.time>1):
                #print 'left sensor is zero!! => TURN'
                self.duration = 0;
                self.started = True;
        
    def origin_outputs(self):
        if(self.started):
            # stopping criteria fulfilled?
            if(self.duration>self.maxdur):
                #print 'stopping the turn!'
                self.started = False;
                return self.straight;
            else:
                self.duration=self.duration+1;
                return [self.l, self.r];
        return self.straight;
    # there are 3 params: how long to turn, speeds of left and right wheel
    def termination_params(self,values):
        if(not self.started):
            self.duration = values[0]*100;
        self.l = values[1];
        self.r = values[2];
        return;
    def tick(self):
        self.time = self.time+1;



# jsut adds bias to input vector [x y] = [xx+b yy+b]
class Summer(nef.SimpleNode):
    def init(self):
        self.output = [0,0]
        self.ia = [0,0];
        self.ib = [0,0];
    def termination_inputData(self,values,dimensions=2):
        self.ia = values;
    def termination_inputBias(self,values,dimensions=1):
        self.ib = values;
    def origin_outputs(self):
        self.output[0] = self.ia[0]+self.ib[0];
        self.output[1] = self.ia[1]+self.ib[0];
        return self.output;



# stores data into csv files each time step
class DataSaver(nef.SimpleNode):
    def tick(self):
        i=file('data/sensoryData.csv','a+')  # data from robot sensors
        o=file('data/actuatorData.csv','a+') # data comming from regulator to robot actuators
        #
        sensorydata = simulator.getAgent('a').getOrigin().getValues().getValues(); # columns: time, [distance sensors from the left, friction sensors from the left]
        actuatordata = controller.getOrigin('outputs').getValues().getValues();    # columns: time, left_wheel, right_wheel
        #
        i.write('%1.3f,%s\n'%(self.t,list(sensorydata)))
        o.write('%1.3f,%s\n'%(self.t,list(actuatordata)))
        i.close()
        o.close()


#################################################################################
net=nef.Network('Vivae - Hybrid network controlled agent')
net.add_to_nengo()  

numsensors=4    # number of agent's sensors (if changed, need to read correct values in the Controller)
simulator = oneAgent(net,agentName='a',numSensors=numsensors,maxDistance=120, frictionDistance=0);
vivae = simulator.getControls();

#controller = net.add(Controller('Agent controller',2*numsensors+1,2,0)); # build controller
controller = net.add(TurnGenerator('Temp. signal changer',2*numsensors+1,2,3)); # build explicit controller
summer = net.add(Summer('sum')); # build explicit controller
c = 0.44
straight = net.make_input('bias0',[c])

net.connect(straight,summer.getTermination('inputBias'))
net.connect(controller.getOrigin('outputs'),summer.getTermination('inputData'))
input=net.make_input('biasO',[0.2, 0.0-c, 0.33-c])

net.connect(input,controller.getTermination('params'))
net.connect(simulator.getAgent('a').getOrigin(),controller.getTermination('inputs'))    # connect agent with controller
#net.connect(controller.getOrigin('outputs'), simulator.getAgent('a').getTermination())  # connect controller to the agent
net.connect(summer.getOrigin('outputs'), simulator.getAgent('a').getTermination())  # connect controller to the agent

#saver = net.add(DataSaver('saver')) # save data ??

t=6;
dt=0.001;
print "OK, configuration done. Simulating network for "+repr(t)+" seconds"
#net.run(t,dt)

print "Simulation done, will reset and show the interactive simulation window."
net.view()
