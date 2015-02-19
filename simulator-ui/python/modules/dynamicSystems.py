# module which serves as library of dynamic systems

import nef

# Define some dynamic system
class exampleDynamicSystem(nef.SimpleNode):    
    def init(self):
        self.x=0;
        self.prev_x=0;
        self.alpha=0;
    def termination_inputs(self,inp,dimensions=2):
        # differential equation goes here:
        self.x = 2*inp[0] + 3*inp[1] + self.alpha * self.prev_x;
    def origin_output(self):
        return [self.x]
    # input which sets parameter alpha online
    def termination_par_alpha(self,parameter_alpha,dimensions=1):
        self.alpha = parameter_alpha[0];
