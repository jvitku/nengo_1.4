import nef
from ca.nengo.math.impl import ConstantFunction, FourierFunction, PostfixFunction
import math


# Network model Start
net_model = nef.Network('model')

# model - Nodes
net_model.make('ANN', 5, 2, tau_rc=0.020, tau_ref=0.002, max_rate=(100.0, 100.0), intercept=(0.0, 0.0), radius=1.00)
net_model.make('input', 2, 2, tau_rc=0.020, tau_ref=0.002, max_rate=(100.0, 100.0), intercept=(0.0, 0.0), radius=1.00)
net_model.make('output', 1, 1, tau_rc=0.020, tau_ref=0.002, max_rate=(100.0, 100.0), intercept=(0.0, 0.0), radius=1.00)

# model - Templates

# model - Projections
transform = [[1.0, 0.0]]
net_model.connect('input', 'in_neuron_0', transform=transform)

transform = [[0.0, 1.0]]
net_model.connect('input', 'in_neuron_1', transform=transform)


# Network model End

net_model.add_to_nengo()
