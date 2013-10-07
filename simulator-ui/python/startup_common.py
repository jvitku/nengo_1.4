"""A series of Python commands to execute when starting up Nengo."""

import ca.nengo
from ca.nengo.math.impl import *
import sys
sys.path.append('.')
sys.path.append('python')
sys.path.append('python/hanns') # TODO: this solves 'could not get source code', tomething from nef_core..
sys.path.append('python/simplemodule') # TODO: this solves 'could not get source code', tomething from nef_core..
sys.path.append('python/simulators') # TODO: this solves 'could not get source code', tomething from nef_core..
sys.path.append('python/util') # TODO: this solves 'could not get source code', tomething from nef_core..

# testing of simplemodule(s)
#sys.path.append('test/simplemodule/mysimpletests') 

from numeric import *
from pydoc import help
