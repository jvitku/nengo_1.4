package ctu.nengoros.comm.rosutils.utilNode.time.impl;

import ctu.nengoros.comm.rosutils.utilNode.time.RosTimeUtil;

public class DefaultTimeSlave implements RosTimeUtil{

	public static final String name = "NengoRosTimeSlave";
	private final String me ="["+name+"] ";
	
	@Override
	public float[] handleTime(float startTime, float stopTime) {
		float[] out  =new float[]{startTime,stopTime};
		
		//TODO handle time here 
		
		return out;
	}

}
