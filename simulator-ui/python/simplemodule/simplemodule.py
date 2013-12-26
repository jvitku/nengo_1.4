"""
SimpleModule is similar to SimpleNode, with the following differences:
#
#   -only ONE termination called inputs 
#       -dimension is set by the constructor
# 
#   -only ONE termination called params 
#       -can be used for online tunning of model parameters
#       -dimension is set by the constructor
# 
#   -arbitrary numner of origins
#       -but recommended is one origin called "outputs"
#       -dimensionality of origin is determined by calling it in time 0,0
#       -dimensionality is set by the constructor,BUT can be changed during simulation
#       -you should check the size of array returned by this method yourself
#
#   -acces to exact information about time (eg. for modeling of dynamics):
#       -you can define termination (inputs or params) or origin with acces to (start,end) time as follows:

            def termination_t_inputs(self,values,start,end):
                print 'A-termination: received this: '+repr(values)+' with these times: '+repr(start)+' '+repr(end);
                self.output=values;
                
        -compared to this:
        
            def termination_inputs(self,values):
                print 'A-termination: '+repr(values);
                self.output=values;
                
    @author Jaroslav Vitku [based on SimpleNode class]
"""

from ca.nengo.model import Termination, Origin, Probeable, Node, SimulationMode, Units
from ca.nengo.model.impl import BasicOrigin, RealOutputImpl
from ca.nengo.util import VisiblyMutableUtils
import java
import inspect
import warnings
import math

class SimpleTermination(Termination):
    def __init__(self,name,node,func,tau=0,dimensions=1,sendtime=False):
        self._name=name
        self._node=node
        self._tau=tau
        self._modulatory=False
        self._func=func
        self._sendtime=sendtime     # whether to pass start and end times to function
        self.setDimensions(dimensions)

    def getName(self):
        return self._name
    def setDimensions(self,dimensions):
        self._values=[0]*dimensions
        self._filtered_values=[0]*dimensions
        self._dimensions=dimensions
    def getDimensions(self):
        return self._dimensions
    def getNode(self):
        return self._node
    def getTau(self):
        return self._tau
    def setTau(self,tau):
        self._tau=tau
    def getModulatory(self):
        return self._modulatory
    def setModulatory(self,modulatory):
        self._modulatory=modulatory

    def setValues(self,values):
        self._values=values.values
    def getOutput(self):
        return self._values

    def reset(self):
        self._values = [0] * self._dimensions
        self._filtered_values = [0] * self._dimensions

    def run(self,start,end):
        dt=end-start
        v=self.getOutput()
        if self.tau<dt or dt==0 or self.tau<=0:
            self._filtered_values=v
        else:    
            decay=math.exp(-dt/self.tau)
            for i in range(self._dimensions):
                x=self._filtered_values[i]
                self._filtered_values[i]=x*decay+v[i]*(1-decay)
        if(self._sendtime):
            self._func(self._filtered_values,start,end)                               
        else:
            self._func(self._filtered_values)                               

class SimpleOrigin(BasicOrigin):
    def __init__(self,name,node,func,sendtime=False):
        if(sendtime):
            BasicOrigin.__init__(self,node,name,len(func(0,0)),Units.UNK)
        else:
            BasicOrigin.__init__(self,node,name,len(func()),Units.UNK)
        self._sendtime=sendtime
        self.func=func
    def run(self,start,end):
        if(self._sendtime):
            self.values=RealOutputImpl(self.func(start,end),Units.UNK,end)
        else:
            self.values=RealOutputImpl(self.func(),Units.UNK,end)

class SimpleModule(Node,Probeable):
    pstc=0
    def __init__(self,name,inputdims,outputdims,numpars):

        """
        :param string name: the name of the created node
        """
        self._name=name
        self.listeners=[]
        self._origins={}
        self._terminations={}
        self._states=java.util.Properties()
        self.setMode(SimulationMode.DEFAULT)
        self.t_start=0
        self.t_end=0
        self.t=0
        
        self.inputdims=inputdims
        self.outputdims=outputdims
        self.numpars=numpars


        
        termsfound=0
        partermsfound=0
        for name,method in inspect.getmembers(self,inspect.ismethod):
            if name.startswith('termination_inputs'):
                if(termsfound>0):
                    print 'ERROR: this simplemodule supports only one termination called values!'
                else:
                    self.create_termination_inputs(name[12:],method,inputdims)
                    termsfound=termsfound+1
            # pass also time information to the input?
            if name.startswith('termination_t_inputs'):
                if(termsfound>0):
                    print 'ERROR: this simplemodule supports only one termination called values!'
                else:
                    self.create_termination_inputs(name[14:],method,inputdims,sendtime=True)
                    termsfound=termsfound+1
            if name.startswith('termination_params'):
                if(partermsfound>0):
                    print 'ERROR: this simplemodule supports only one termination called params!'
                else:
                    self.create_termination_params(name[12:],method,numpars)
                    partermsfound=partermsfound+1
            # send parameters with time too (may not be even necessary)
            if name.startswith('termination_t_params'):
                if(partermsfound>0):
                    print 'ERROR: this simplemodule supports only one termination called params!'
                else:
                    self.create_termination_params(name[14:],method,numpars,sendtime=True)
                    partermsfound=partermsfound+1
        self.init(inputdims,outputdims,numpars)
        
        for name,method in inspect.getmembers(self,inspect.ismethod):
            if name.startswith('origin_t_'):
                self.create_origin(name[9:],method,sendtime=True)
            elif name.startswith('origin_'):
                self.create_origin(name[7:],method)

    def notifyAboutDeletion(self):
        """
        ///my @author Jaroslav Vitku
        Modification of Node interface by Jaroslav Vitku, each node is notified
        before will be deleted. Note that this method has to be here, if not, the following 
        error is thrown: RuntimeException: Maximum recurtion depth exceeded."""


    def create_origin(self,name,func,sendtime=False):
        self.addOrigin(SimpleOrigin(name,self,func,sendtime=sendtime))
  

    def create_termination_inputs(self,name,func,dimensions,sendtime=False):
        """
        ///my 
        This builds input termination of given dimensions
        """
        pstc=self.pstc
        a,va,k,d=inspect.getargspec(func)
        if 'pstc' in a:
            index=a.index('pstc')-len(a)
            pstc=d[index]

        t=SimpleTermination(name,self,func,tau=pstc,dimensions=dimensions,sendtime=sendtime)
        self.addTermination(t)
        
    def create_termination_params(self,name,func,dimensions,sendtime=False):
        """
        ///my 
        This builds termination for online setting parameters,
        dimensionality (num of parameters) is defined in constructor of this class
        """
        pstc=self.pstc
        a,va,k,d=inspect.getargspec(func)
        if 'pstc' in a:
            index=a.index('pstc')-len(a)
            pstc=d[index]

        t=SimpleTermination(name,self,func,tau=pstc,dimensions=dimensions,sendtime=sendtime)
        self.addTermination(t)
        

    def tick(self):
        """An extra utility function that is called every time step.
        
        Override this to create custom behaviour that isn't necessarily tied
        to a particular input or output.  Often used to write spike data to a file
        or produce some other sort of custom effect.
        """
        pass

    def setTau(self,name,tau):
        """Change the post-synaptic time constant for a termination.
        
        :param string name: the name of the termination to change
        :param float tau: the desired post-synaptic time constant
        """
        self._terminations[name].setTau(tau)

    def init(self):
        """Initialize the node.
        
        Override this to initialize any internal variables.  This will
        also be called whenever the simulation is reset::
        
          class DoNothingNode(nef.SimpleNode):
              def init(self):
                  self.value=0
              def termination_input(self,x,pstc=0.01):
                  self.value=x[0]
              def origin_output(self):
                  return [self.value]
        """        
        pass


    # the following functions implement the basic interface needed to be a Node        
    def getName(self):
        return self._name
    def setName(self,name):
        VisiblyMutableUtils.nameChanged(self, self.getName(), name, self.listeners)
        self._name=name

    def reset(self,randomize=False):
        for termination in self.getTerminations():
            termination.reset()
        self.init(self.inputdims,self.outputdims,self.numpars)    


    def addChangeListener(self,listener):
        self.listeners.append(listener)
    def removeChangeListener(self,listener):
        self.listeners.remove(listener)

    def run(self,start,end):
        if start<self.t_start: self.reset()
        self.t_start=start
        self.t_end=end
        self.t=self.t_start
        for t in self.getTerminations():
            t.run(start,end)
        self.tick()    
        for o in self.getOrigins():
            o.run(start,end)

    def getOrigins(self):
        return self._origins.values()
    def getOrigin(self,name):
        return self._origins[name]
    def addOrigin(self,origin):
        self._origins[origin.name]=origin
        self._states.setProperty(origin.name,"data")
    def removeOrigin(self, name):
        del self._origins[name]
        self._states.remove(name)

    def getTerminations(self):
        return self._terminations.values()
    def getTermination(self,name):
        return self._terminations[name]
    def addTermination(self,termination):
        self._terminations[termination.name]=termination
    def removeTermination(self, name):
        del self._terminations[name]

    def getDocumentation(self):
        return ""

    def clone(self):
        raise java.lang.CloneNotSupportedException()

    def setMode(self,mode):
        self._mode=mode
    def getMode(self):
        return self._mode

    def listStates(self):
        return self._states
        
    def getChildren(self):
        return None    




