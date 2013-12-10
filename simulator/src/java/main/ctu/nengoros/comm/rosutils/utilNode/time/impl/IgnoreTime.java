package ctu.nengoros.comm.rosutils.utilNode.time.impl;

import ctu.nengoros.comm.rosutils.utilNode.time.RosTimeUtil;

/**
 * In case of completely ignoring time for other ROS nodes, this does nothing.
 *  
 * @author Jaroslav Vitku
 *
 */
public class IgnoreTime implements RosTimeUtil{

	@Override
	public float[] handleTime(float startTime, float endTime) {
		return new float[]{startTime, endTime};
	}
	
}
