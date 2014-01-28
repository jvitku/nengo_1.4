# Create the NeuralModule which implements discrete source of motivation.
#
# the source of motivaiton has:
#   -n inputs, these are summed, if the value is equal or more  than threshold (1 now), the reinforcement is received, state variable is set back to limbo
#   -2 outputs defining: {reward just received, size of the current motivation}
#
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from ctu.nengoros.modules.impl import DefaultNeuralModule as NeuralModule
from ctu.nengoros.comm.nodeFactory import NodeGroup as NodeGroup
from ctu.nengoros.comm.rosutils import RosUtils as RosUtils
from org.hanns.physiology.statespace.ros import BasicMotivation as Motivation
import motivation

net=nef.Network('Motivation source demo')
net.add_to_nengo()  

# params: name,net,noInputs,decay per step (from 1 to 0), log period, rewardValue, rewardThreshold
source = motivation.basicConfigured("BasicMotivation", net, 1, Motivation.DEF_DECAY, 1)   

#Create a white noise input function with params: baseFreq, maxFreq [rad/s], RMS, seed
# generate random values, if val>1, the reward is received and the motivaiton source goes to the limbo area
reward=FunctionInput('RewardGenerator', [FourierFunction(.1, 10,-0.5, 12)],Units.UNK)

#net.add(source)
net.add(reward)

# data
net.connect(reward,	source.getTermination(Motivation.topicDataIn))
#net.connect(reward,	source.newTerminationFor(Motivation.topicDataIn,[1]))

print 'Configuration complete.'
print ''
print 'One motiavtion source is connected to its configuration (defining decay speed)'
print 'and source of motivation, if the motivation is above the threshold, the '
print 'physiological variable returns to the limbo area, where no motivation is produced.'
print 'With thime, the motivaiton increases again.'

