# Demo showing all fuzzy memmership functions
# Unfortunately, increasing and decreasing linear do not work in the GUI (despite the fact that data are sent OK). TODO solve this.
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
inclinn = logic_gates.fuzzyMemIncLin("IncLin")
net.add(inclinn)

declinn = logic_gates.fuzzyMemDecLin("DecLin")
net.add(declinn)

triangle = logic_gates.fuzzyMemTriangle("Triangle")
net.add(triangle)

trapezoid = logic_gates.fuzzyMemTrapezoid("Trapezoid")
net.add(trapezoid)

# Wire inputs
net.connect(gen1, inclinn.getTermination('logic/gates/ina'))
net.connect(gen1, declinn.getTermination('logic/gates/ina'))
net.connect(gen1, triangle.getTermination('logic/gates/ina'))
net.connect(gen1, trapezoid.getTermination('logic/gates/ina'))

net.connect('alpha1', inclinn.getTermination('logic/gates/confa'))
net.connect('beta1', inclinn.getTermination('logic/gates/confb'))

net.connect('alpha1', declinn.getTermination('logic/gates/confa'))
net.connect('beta1', declinn.getTermination('logic/gates/confb'))

net.connect('alpha2', triangle.getTermination('logic/gates/confa'))
net.connect('beta2', triangle.getTermination('logic/gates/confb'))
net.connect('gamma2', triangle.getTermination('logic/gates/confc'))

net.connect('alpha3', trapezoid.getTermination('logic/gates/confa'))
net.connect('beta3', trapezoid.getTermination('logic/gates/confb'))
net.connect('gamma3', trapezoid.getTermination('logic/gates/confc'))
net.connect('delta3', trapezoid.getTermination('logic/gates/confd'))

print 'Configuration complete.'

