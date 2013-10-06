# req.:
#  -generic num of terminations:
#
#  -generic num of origins:
#
#  num of IOs and their dimensions is defined here
#
#
import nef
import random
import simplemodule

class Largest(simplemodule.SimpleModule):
    
    # init all variables here!
    def init(self):
        self.largest=0
        
    # termination has to be set a priori    
    def termination_values(self,x,dimensions=5,pstc=0.01):
        self.largest=max(x)
    
    # dimension of origin is determined by calling the method in time 0,0
    def origin_largest(self):
        return [self.largest]

net=nef.Network('largest')
net.make_input('input',[0]*5)
largest=net.add(Largest('largest'))
net.connect('input',largest.getTermination('values'))
net.add_to_nengo()
print 'done'
