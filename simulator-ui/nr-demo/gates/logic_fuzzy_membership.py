# Create the NeuralModule which receives 4 float values, finds min and max, converts them to int and passes to the output.
#
# starts: 
#   -ROS-java node (class extending the org.ros.Node) which does exactly the thing described above
#   -NeuralModule with modem that communicates with the ROS node 
#
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from rosnodes import logic_gates

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Demo script which shows network of fuzzy membership functions')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

# Create function generators, white noise with params: baseFreq, maxFreq [rad/s], RMS, seed
gen1=FunctionInput('Randomized input 1', [FourierFunction(.1, 10,3, 12)],Units.UNK)
net.add(gen1)

net.make_input('alpha1',[-0.5]) #Create a controllable input function 
net.make_input('beta1',	[0.5]) 

net.make_input('alpha2',[-0.5]) 
net.make_input('beta2',	[0]) 
net.make_input('gamma2',[0.5]) 

net.make_input('alpha3',[-1]) 
net.make_input('beta3',	[-0.5]) 
net.make_input('gamma3',[0.5]) 
net.make_input('delta3',[1]) 


# Add nodes
incl = logic_gates.fuzzyMemIncLin("IncLin")
net.add(incl)

declin = logic_gates.fuzzyMemDecLin("DecLin")
net.add(declin)

triangle = logic_gates.fuzzyMemTriangle("Triangle")
net.add(triangle)

trapezoid = logic_gates.fuzzyMemTrapezoid("Trapezoid")
net.add(trapezoid)

# Wire inputs
net.connect(gen1, incl.getTermination('logic/gates/ina'))
net.connect(gen1, declin.getTermination('logic/gates/ina'))
net.connect(gen1, triangle.getTermination('logic/gates/ina'))
net.connect(gen1, trapezoid.getTermination('logic/gates/ina'))

net.connect('alpha1', incl.getTermination('logic/gates/confa'))
net.connect('beta1', incl.getTermination('logic/gates/confb'))

net.connect('alpha1', declin.getTermination('logic/gates/confa'))
net.connect('beta1', declin.getTermination('logic/gates/confb'))

net.connect('alpha2', triangle.getTermination('logic/gates/confa'))
net.connect('beta2', triangle.getTermination('logic/gates/confb'))
net.connect('gamma2', triangle.getTermination('logic/gates/confc'))

net.connect('alpha3', trapezoid.getTermination('logic/gates/confa'))
net.connect('beta3', trapezoid.getTermination('logic/gates/confb'))
net.connect('gamma3', trapezoid.getTermination('logic/gates/confc'))
net.connect('delta3', trapezoid.getTermination('logic/gates/confd'))


print 'Configuration complete.'


