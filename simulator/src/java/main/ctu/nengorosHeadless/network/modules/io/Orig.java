package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;

public interface Orig extends Resettable{

	/**
	 * Values that are sent through the network.
	 * 
	 * @return array of values that is sent through the Connection
	 */
	public float[] getValues();

	public int getSize();
	
	/**
	 * Wait for all ROS messages (in the Synchronous case), decode them and 
	 * put the values to my output.
	 * 
	 * @param startTime
	 * @param endTime
	 */
	public void run(float startTime, float endTime);
}
