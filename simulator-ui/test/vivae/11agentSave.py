"""
This is feedforward HNN which connection weights are specified by genome of length 29 (interval <-1,1>)

EA modifies these weights in order to produce the highest averate speed

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

from design.ea.matrix.hnn.simple import HNNWMatrixEA as EA
from design.ea.matrix.hnn.simple import HNNInd as Ind
import os

from ca.nengo.model import Termination, Origin, Probeable, Node, SimulationMode, Units
from ca.nengo.model.impl import BasicOrigin, RealOutputImpl
from ca.nengo.util import VisiblyMutableUtils
import java
import inspect
import warnings
import math
import csv

RosUtils.setAutorun(False)    

# this generates signal which turns the agent left
class EventGenerator(simplemodule.SimpleModule):
    #  .......... 9         2          1    [all sensors, two speeds, turning duration]
    def init(self,inputdims,outputdims,numpars):
        self.inputdims = inputdims;
        self.outputdims = outputdims;
        self.numpars = numpars;
        self.lo = 0.1;       # original one
        self.ro = 0.38;      # original one
        self.duration = 0;
        self.maxdur = 30;    # original one
        self.started = False;
        self.threshold = 0; # if input value is under this threshold, launch the event!
        #self.straight = [0.47, 0.47];
        self.time = 0;
        self.il=0
        self.ir=0
            
    def termination_inputs(self,values):
        if(not self.started):
            self.il=values[1];
            self.ir=values[2];
            inputval = wa[0]*self.il + wa[4]*self.ir + wa[8]*1
            if(inputval <= self.threshold and self.time>1):
                self.duration = 0;
                self.started = True;
        
    def origin_outputs(self):
        if(self.started):
            # stopping criteria fulfilled?
            if(self.duration > self.maxdur):
                self.started = False;
                return [0, 0];
            else:
                self.duration=self.duration+1;
                return [self.lo, self.ro];
        return [0, 0];
        
    # parames are: threshold <-1,1>, leftWheel <-1,1> & rightWheel <-1,1> during an event
    def termination_params(self,values):
        if(not self.started):
            self.threshold = self.il*wa[1]+self.ir*wa[5]+wa[9]*values[0]; 
        self.lo = self.il*wa[2] + self.ir*wa[6] + wa[10]*values[0];
        self.ro = self.il*wa[3] + self.ir*wa[7] + wa[11]*values[0];
        return;
        
    def tick(self):
        self.time = self.time+1;


class SpeedSaver(nef.SimpleNode):
    def init(self):
        self.t = 0;
        self.speeds = 0;
    def termination_data(self,values,dimensions=9):
        self.speeds = self.speeds+values[8];
        self.t = self.t+1;
    def getAvgSpeed(self):
        return self.speeds/self.t;


class Summer(nef.SimpleNode):
    def init(self):
        self.output = [0,0]
        self.il = 0;
        self.ir = 0;
        self.ib = 0;
        self.bias = 0; # this is added to both ouput signals
    def termination_inputData(self,values,dimensions=2):
        self.il = values[0];
        self.ir = values[1];
    def termination_inputBias(self,values,dimensions=1):
        self.ib = values[0];
    def origin_outputs(self):
        self.bias =      wb[2]*self.il + wb[5]*self.ir+wb[8]*self.ib;   # bias added to signals
        output=[0,0]    # tmp
        output[0] = wb[0]*self.il + wb[3]*self.ir+wb[6]*self.ib +self.bias; # outputs
        output[1] = wb[1]*self.il + wb[4]*self.ir+wb[7]*self.ib +self.bias;
        
        # here is also the representation of output weights..
        self.output[0] = wc[0]*output[0]+wc[1]*output[1];
        self.output[1] = wc[2]*output[0]+wc[3]*output[1];
        return self.output;

def saveAgent(generation,fitness,genome,experimentName):
    i = open(experimentName, 'a+');
    #i=file('data/sensoryData.csv','a+')  # data from robot sensors
    #ii=file(experimentName,'a+')  # data from robot sensors
    i.write('%1.3f,%1.3f,%s\n'%(generation,fitness,list(genome)))
    i.close()
    
def another(generation, fitness, genome, experimentName):
	file = open(experimentName, 'a+');
	out = [0]*(2+len(genome));
	print repr(out)
	out[0] = generation
	out[1] = fitness
	
	for i in range(len(genome)):
		out[i+2]=genome[i]
		
	def format(value):
	    return "%1.3f" % value

	formatted = [format(v) for v in out]
	file.write(str(formatted))
	file.close()
	
def anotherr(experimentName):
	file = open(experimentName, 'r');
	reader = csv.reader(file)
	for row in reader:
		print(row)
	"""
	a = file.read()
	for i in range(len(a)):
		print '.. '+repr(a[i])
	"""
	print repr(a)
		
def csvwriter(generation, fitness, genome, experimentName):
	f =  open(experimentName, 'a+');
	writer = csv.writer(f)
	writer.writerows(genome)
		
def writee(generation,fitness,genome,experimentName):
	outfile= open(experimentName,'w')
	out = csv.writer(outfile)
	#out.writerow([generation + fitness])
	out.writerow(['generation', 'fasdf'])



################################################################################# higher level..


def buildExperiment(net,wa,wb,wc,visibility=False):

    numsensors=4                                # number of agents sensors (if changed, need to read correct values in the Controller)

    simulator = oneAgent(net,agentName='a',numSensors=numsensors,maxDistance=120, frictionDistance=0,nameSpace=simName,visible=visibility,mapName='data/scenarios/test/wallsIII.svg');
    vivae = simulator.getControls();

    # Input->LI
    controller = net.add(EventGenerator('Agent controller',numsensors*2+1,2,1));            # event generator
    input = net.make_input('bias1',[1])                                                     # should result in 0, 0, .33
    net.connect(input, controller.getTermination('params'))
    net.connect(simulator.getAgent('a').getOrigin(),controller.getTermination('inputs'))    # connect agent with controller

    # LII->LIII->Output
    input2 = net.make_input('bias2',[1])
    summer = net.add(Summer('Summator'));
    net.connect(input2,summer.getTermination('inputBias'));
    net.connect(controller.getOrigin('outputs'),summer.getTermination('inputData'))
    net.connect(summer.getOrigin('outputs'),simulator.getAgent('a').getTermination())

    saver = net.add(SpeedSaver('avg'))
    net.connect(simulator.getAgent('a').getOrigin(),saver.getTermination('data'));
    #saver = net.add(DataSaver('saver')) # save data ??
    return simulator


def runExperiment(net):
    print "Simulating network for "+repr(t)+" seconds"
    net.reset()
    net.run(t,dt)
    avg = net.get('avg').getAvgSpeed()*dt;
    print 'AVG speed was: '+repr(avg);
    return avg;
    
wa = [1,0,0,0, 0,0,0,0, 0,0,-0.44,-0.11, 0,0,0,0]
wb = [1,0,0, 0,1,0, 0,0,0.44]
wc = [1,0, 0,1]

############################################################ EA here

def evalInd(net,simulator,ind):
    print 'vector: '+repr(ind.m.getVector())
    print 'wa: '+repr(wa)
    print 'wb: '+repr(wb)
    print 'wc: '+repr(wc)

    net.reset()
    vivae = simulator.getControls();
    avg = runExperiment(net)
    return avg

minw = -1;
maxw = 1;
genomeLen = 29;
# which setup to use?
config=1
dt = 0.001
if config == 1: # this works pretty well (approximates sum)
    t=0.31
    pMut = 0.1
    pCross = 0.9;
    popsize = 3;
    maxgen = 1;
if config == 2:
    t=2
    pMut = 0.3
    pCross = 0.9;
    popsize = 50;
    maxgen = 100;

simName = util.randomString(20);
net=nef.Network('Vivae - Turning controller for agent')
net.add_to_nengo()  
ea = EA(maxgen,popsize,minw,maxw,genomeLen);
ea.setProbabilities(pMut,pCross);
ea.initPop();
simulator = buildExperiment(net,wa,wb,wc,visibility=True);

print 'starting build'
expNo = round(1000000*random.random(),0);   # generate some number for text file data
print expNo
f = open('data/tmp/ea_%d.txt'%expNo, 'w');


# evolution insert here
while ea.wantsEval():
    print 'Gen: '+repr(ea.generation())+'/'+repr(maxgen)+' actual ind is ' +repr(ea.actualOne())+'/'+repr(popsize)+' best so far: '+repr(ea.getBestFitness());
    
    ind = ea.getInd();

    # get wa,b,c from ind genome
    wa = ind.m.getRange(0,16);
    wb = ind.m.getRange(16,25);
    wc = ind.m.getRange(25,29);
    #simName = util.randomString(20);
    ind.getFitness().set(2.545);
    break;		

    fitness = evalInd(net,simulator,ind);
    ind.getFitness().set(fitness);

    print 'Ind: '+repr(ea.actualOne())+' AVG Speed is: '+repr(fitness) +' fitness is: '+repr(ind.getFitness().get());

    # evaluated the last individual in the generatio? write stats
    if (ea.actualOne() == (popsize-1)):
        print 'check: '+repr(ea.generation())
        fit = ea.getBestInd().getFitness().get();
        er = ea.getBestInd().getFitness().getError();
        print '%d %.5f %.5f\n' % (ea.generation(),fit,er)
        f.write('%d %.8f %.8f\n' % (ea.generation(),fit,er))
        f.flush()
        os.fsync(f.fileno()) # just write it to disk
    # poc++ and check end of ea
    ea.nextIndividual();

f.close()

# load the best one found
ind = ea.getIndNo(ea.getBest());
#net = buildExperiment(ind);
print 'best fitness is:'
print ind.getFitness().get();


file = open('data/tmp/ea_%d_architecture.txt'%expNo, 'w');
m = ind.m.getVector();
file.write(str(m));
file.close();

print 'architecture weights written, openning architecture'

# get wa,b,c from ind genome
wa = ind.m.getRange(0,16);
wb = ind.m.getRange(16,25);
wc = ind.m.getRange(25,29);

print 'save...'

g = 1;
f = 9;

saveAgent(g,f,ind.m.getVector(),'data/tmp/ea_%d_agents.csv'%expNo)
#writee(g,f,ind.m.getVector(),'data/tmp/ea_%d_agents_.csv'%expNo)
#another(g,f,ind.m.getVector(),'data/tmp/ea_%d_agents_.csv'%expNo)
#another(g+1,f+3,ind.m.getVector(),'data/tmp/ea_%d_agents_.csv'%expNo)
#another(g+2,f+5,ind.m.getVector(),'data/tmp/ea_%d_agents_.csv'%expNo)
#anotherr('data/tmp/ea_%d_agents_.csv'%expNo)

csvwriter(g,f,ind.m.getVector(),'data/tmp/ea_%d_agents_.csv'%expNo)



#########################


#vivae.setVisible(True)
#print "Simulation done, will reset and show the interactive simulation window."
#net.view()
