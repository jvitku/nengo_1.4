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

# print each weight
def myprint(w):
    print "dimensions are: [%i,%i]" % (len(w),len(w[1])) 
    
    for i in range(len(w)):
        print "-----i is here: %i range i is: %i\n" % (i,len(w))
        for j in range(len(w[i])):
            print "w[%i][%i] je tady: \t%f" % (i,j,w[i][j])
    return w

# set values
diag=0.4
other=-0.08
def diagonals(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            if i==j:
                w[i][j]=diag
            else:
                w[i][j]=other
    myprint(w)
    return w

# set constant values
c=0.3
def constant(w):
    for i in range(len(w)):
        for j in range(len(w[i])):
            w[i][j]=c
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
A=net.make('A',neurons=10,dimensions=1)
B=net.make('B',neurons=10,dimensions=1)
net.connect(wave.getOrigin('wave'),A)
net.connect(A,B,weight_func=diagonals)
#net.connect(A,B,weight_func=sparsify)
#net.connect(A,B,weight_func=constant)

print 'Configuraton done'
print 'One generator of sine wave connected to neural ensemble'
print 'One recurrent conection (called AXON) = full connection from output to input'
