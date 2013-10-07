"""Commands to run when starting nengo-cl (the command-line version)"""
# ///my modifications all the way, also in the nengo-cl script @author Jaroslav Vitku
# based on oriignal nengo script
execfile('python/startup_common.py')

__nengo_cl__ = True

import sys
sys.argv=sys.argv[1:]   # strip out the "python/startup_cl.py" from the list
if len(sys.argv)>0:
    print 'Nengo here: starting script named: '+repr(sys.argv)
    # run the script indicated on the command line
    execfile(sys.argv[0])

    # start an interactive console
    sys.argv=['-i']
    import org.python.util
    interp=org.python.util.JLineConsole(locals())
    print '-------------------------------------'
    interp.interact()
else:
    print """
Welcome to Nengo!  <http://nengo.ca>
    
Nengo is an open-source neural simulator for large-scale systems, developed at
the University of Waterloo by the Centre for Theoretical Neuroscience.

This is the command-line interface, useful for quickly running scripts and
for embedding Nengo within other research tools.  There is also a graphical
user interface available by running 'nengo' (rather than 'nengo-cl', which
is this program).

The scripting language is Python.  You can run .py scripts as follows:
    execfile('directory/script.py')
You can also create models manually and even run the interactive plots viewer:
    import nef
    net=nef.Network('My Network')
    input=net.make_input('input',[0])
    A=net.make('A',neurons=50,dimensions=1)
    net.connect(input,A)
    net.view()
"""
    # start an interactive console
    sys.argv=['-i']
    import org.python.util
    interp=org.python.util.JLineConsole(locals())
    interp.interact()
    
