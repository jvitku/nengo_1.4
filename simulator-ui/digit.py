presentation_time=0.1 # how long to show each digit
N=3                   # number of spiking neurons per RBM node for layers 1-3
N2=10                 # number of spiking neurons per RBM node for layer 4 
pstc=0.006            # post-synaptic time constant      
seed=2                # random number seed for neuron creation

import nef
import random
import numeric
import hrr

# utility function for reading data from a csv file
def read(fn):
    data=[]
    for line in file(fn).readlines():
        row=[float(x) for x in line.strip().split(',')]
        data.append(row)
    return data

m_inputs=read('mat_test_x.csv')   # the visual stimuli
m_encode=read('mat_encode.csv')   # the exact RBM output value for that stimuli
m_inputsy=read('mat_test_y.csv')  # the category of the stimuli (0-9)

# present the digits in a random order
order=list(range(len(m_inputs)))
random.shuffle(order)


# compute the semantic pointers for each digit by averaging the encoded values for each category
vocab=hrr.Vocabulary(50)
for i,label in enumerate(['ZERO','ONE','TWO','THREE','FOUR','FIVE','SIX','SEVEN','EIGHT','NINE']):
    v=numeric.array([0]*50,typecode='f')
    count=0
    for j,yy in enumerate(m_inputsy):
        if yy[0]==i:
            v+=m_encode[j]
            count+=1
    sp=hrr.HRR(data=v/count)
    sp.normalize()
    vocab.add(label,sp)
    print label,sp.v


class Input(nef.SimpleNode):
    def origin_image(self):                             # present the visual input to the model
        index=int(self.t/presentation_time)%len(order)
        return m_inputs[order[index]]
    def origin_exact(self):                             # the exact correct value (as computed by
        index=int(self.t/presentation_time)%len(order)  #  the non-spiking RBM model).  Used for
        return m_encode[order[index]]                   #  comparison purposes only
        

net=nef.Network('RBM Digit Recognition',seed=seed)
input=net.add(Input('input'))

# the sigmoid function used by the RBM model
def transform(x):
    return 1.0/(1+math.exp(-x[0]))

w1=read('mat_1_w.csv')   # weights for layer 1 (computed using standard Matlab learning model)
b1=read('mat_1_b.csv')   # bias for layer 1 (computed using standard Matlab learning model)

layer1=net.make_array('layer1',N,len(w1[0]),encoders=[[1]],intercept=(0,0.8),seed=seed)
bias1=net.make_input('bias1',b1[0])
net.connect(bias1,layer1)
net.connect(input.getOrigin('image'),layer1,transform=numeric.array(w1).T,pstc=pstc)


w2=read('mat_2_w.csv')   # weights for layer 2 (computed using standard Matlab learning model)
b2=read('mat_2_b.csv')   # bias for layer 2 (computed using standard Matlab learning model)

layer2=net.make_array('layer2',N,len(w2[0]),encoders=[[1]],intercept=(0,0.8),seed=seed)
bias2=net.make_input('bias2',b2[0])
net.connect(bias2,layer2)
net.connect(layer1,layer2,func=transform,transform=numeric.array(w2).T,pstc=pstc)

w3=read('mat_3_w.csv')   # weights for layer 3 (computed using standard Matlab learning model)
b3=read('mat_3_b.csv')   # bias for layer 3 (computed using standard Matlab learning model)

layer3=net.make_array('layer3',N,len(w3[0]),encoders=[[1]],intercept=(0,0.8),seed=seed)
bias3=net.make_input('bias3',b3[0])
net.connect(bias3,layer3)
net.connect(layer2,layer3,func=transform,transform=numeric.array(w3).T,pstc=pstc)

w4=read('mat_4_w.csv')   # weights for layer 4 (computed using standard Matlab learning model)
b4=read('mat_4_b.csv')   # bias for layer 4 (computed using standard Matlab learning model)

layer4=net.make_array('layer4',N2,len(w4[0]))
bias4=net.make_input('bias4',b4[0])
net.connect(bias4,layer4)
net.connect(layer3,layer4,func=transform,transform=numeric.array(w4).T,pstc=pstc)


net.add_to_nengo()
net.set_layout({'state': 0, 'height': 700, 'width': 950, 'x': 0, 'y': 28},
 [(u'input', None, {'label': False, 'height': 33, 'width': 60, 'x': 75, 'y': 8}),
  (u'input', u'value (grid)|image', {'label': True, 'height': 200, 'width': 200, 'x': 7, 'y': 40}),
  (u'layer1', None, {'label': False, 'height': 33, 'width': 69, 'x': 267, 'y': 12}),
  (u'layer2', None, {'label': False, 'height': 33, 'width': 69, 'x': 402, 'y': 10}),
  (u'layer3', None, {'label': False, 'height': 33, 'width': 69, 'x': 563, 'y': 12}),
  (u'layer4', None, {'label': False, 'height': 33, 'width': 69, 'x': 780, 'y': 15}),
  (u'layer1', 'spike raster', {'label': True, 'sample': 10, 'height': 519, 'width': 148, 'x': 214, 'y': 49}),
  (u'layer2', 'spike raster', {'label': True, 'sample': 5, 'height': 519, 'width': 147, 'x': 371, 'y': 49}),
  (u'layer3', 'spike raster', {'label': True, 'sample': 5, 'height': 511, 'width': 147, 'x': 525, 'y': 50}),
  (u'layer4', 'semantic pointer', {'label': True, 'normalize': True, 'sel_dim': [0, 1, 2, 3, 4, 5, 6, 7, 8, 9], 'last_maxy': 5.0000000000000036, 'height': 272, 'width': 267, 'x': 677, 'smooth_normalize': False, 'fixed_y': (-0.5, 1), 'show_pairs': False, 'autozoom': False, 'y': 274}),
  (u'layer4', 'firing rate', {'label': True, 'rows': None, 'height': 200, 'width': 200, 'x': 715, 'y': 51, 'cols': None})],
 {'dt': 0, 'show_time': 0.1, 'sim_spd': 4, 'filter': 0.01, 'rcd_time': 4.0})
net.view()
        
        

