package ctu.nengoros.comm.rosutils.utilNode.time.impl;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;

import ctu.nengoros.comm.rosutils.utilNode.time.RosTimeUtil;

/**
 * In case of completely ignoring time for other ROS nodes, this does nothing.
 *  
 * @author Jaroslav Vitku
 *
 */
public class IgnoreTime extends AbstractNodeMain implements RosTimeUtil{

	public static final String name = "DefaultTimeIgnoreNode";
	
	@Override
	public float[] handleTime(float startTime, float endTime) {
		return new float[]{startTime, endTime};
	}

	@Override
	public void simulationStopped() { }

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(name); }
	
}
