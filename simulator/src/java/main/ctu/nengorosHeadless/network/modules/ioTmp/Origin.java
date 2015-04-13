package ctu.nengorosHeadless.network.modules.ioTmp;

import ca.nengo.model.Resettable;

public interface Origin extends Resettable{

	/**
	 * Values decoded from the ROS message. These are sent to other nodes.
	 * 
	 * @return vector of values that is received from the ROS node.
	 */
	public float[] getValues();
	
	public int getSize();

	/**
	 * Called each step, values are decoded (from the ROS node(s)), and set to outputs 
	 * @param startTime
	 * @param endTime only this time step is used
	 */
	public void run(float startTime, float endTime);
	
}
