# Demo of fuzzy membership functions (ROS nodes).
#
#
# To launch this script: open the Nengo gui (probably nengo/simulator-ui/nengo) and weite into the command line:
# 	run nr-demo/gates/logic_crisp_gates.py
# Note that this cript has to be either symlinked or copied into the location: nengo/simulator-ui/nengo/nr-demo/gates/
#
#
# by Jaroslav Vitku [vitkujar@fel.cvut.cz]

import nef
from ca.nengo.math.impl import FourierFunction
from ca.nengo.model.impl import FunctionInput
from ca.nengo.model import Units
from rosnodes import logic_gates

# creates nef network and adds it to nengo (this must be first in the script) 
net=nef.Network('Demo script which shows network of fuzzy membership functions - Trapezoid and Triangular')
net.add_to_nengo()  # here: delete old (toplevel) network and replace it with the newly CREATED one

# Create function generators, white noise with params: baseFreq, maxFreq [rad/s], RMS, seed
gen1=FunctionInput('Randomized input 1', [FourierFunction(.1, 10,3, 12)],Units.UNK)
net.add(gen1)

net.make_input('alpha1',[-0.5]) 
net.make_input('beta1',	[0]) 
net.make_input('gamma1',[0.5]) 

net.make_input('alpha2',[-1]) 
net.make_input('beta2',	[-0.5]) 
net.make_input('gamma2',[0.5]) 
net.make_input('delta2',[1]) 


# Add nodes
triangle = logic_gates.fuzzyMemTriangle("Triangle")
net.add(triangle)

trapezoid = logic_gates.fuzzyMemTrapezoid("Trapezoid")
net.add(trapezoid)

# Wire inputs
net.connect(gen1, triangle.getTermination('logic/gates/ina'))
net.connect(gen1, trapezoid.getTermination('logic/gates/ina'))

net.connect('alpha1', triangle.getTermination('logic/gates/confa'))
net.connect('beta1', triangle.getTermination('logic/gates/confb'))
net.connect('gamma1', triangle.getTermination('logic/gates/confc'))

net.connect('alpha2', trapezoid.getTermination('logic/gates/confa'))
net.connect('beta2', trapezoid.getTermination('logic/gates/confb'))
net.connect('gamma2', trapezoid.getTermination('logic/gates/confc'))
net.connect('delta2', trapezoid.getTermination('logic/gates/confd'))

print 'Configuration complete.'

