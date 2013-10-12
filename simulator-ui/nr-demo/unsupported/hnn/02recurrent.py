import nef
import random

# sparsification of connection weights
p=0.2
def sparsify(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            if random.random()<p:
                w[i][j]/=p
            else:
                w[i][j]=0.0
    return w

def print_weights(w):
    print w
    return w

# print each weight
def myprint(w):
    print "dimensions are: [%i,%i]" % (len(w),len(w[1])) 
    
    for i in range(len(w)):
        print "-----i is here: %i range i is: %i\n" % (i,len(w))
        for j in range(len(w[i])):
            print "w[%i][%i] je tady: \t%f" % (i,j,w[i][j])
    return w

# set values
d=1
def set_diagonals(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            if i==j:
                w[i][j]=d
            else:
                w[i][j]=0.019 #random.uniform(-0.001,0.001)
                #w[i][j]+=random.gauss(0,0.001)
    myprint(w)
    return w

# define the SimpleNode
class SineWave(nef.SimpleNode):
    def origin_wave(self):
        # print "here :%f" % (self.t*10)
        return [math.sin(self.t*10)]


net=nef.Network('Small recurrent fully connected ANN')
net.add_to_nengo()

wave=net.add(SineWave('Wave generator'))
A=net.make('Neural Ensemble',neurons=10,dimensions=1)
net.connect(wave.getOrigin('wave'),A)
#net.connect(A,A,weight_func=myprint)
net.connect(A,A,weight_func=set_diagonals)

print 'Configuraton done'
print 'One generator of sine wave connected to neural ensemble'
print 'One recurrent conection (called AXON) = full connection from output to input'
