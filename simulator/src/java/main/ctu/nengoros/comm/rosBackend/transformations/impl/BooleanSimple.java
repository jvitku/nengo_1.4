package ctu.nengoros.comm.rosBackend.transformations.impl;

import ctu.nengoros.comm.rosBackend.transformations.BooleanTransform;

public class BooleanSimple implements BooleanTransform {

	@Override
	public boolean float2bool(float data) {
		if(data >= 0.5)
			return true;
		return false;
	}

	@Override
	public float bool2float(boolean data) {
		if(data)
			return 1;
		return 0;
	}

	
	
}
