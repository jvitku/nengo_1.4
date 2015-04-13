package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;

/**
 * Compared to the NengoROS, the Term here only serves as encoder and sender of information.
 * The rest is incorporated in the weighted connections
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Term extends Resettable {

	/**
	 * By default, sums all values sent here (e.g. from multiple origins).
	 * 
	 * @param value value to be added
	 * @param index index of value in the Term's array
	 */
	public void sendValue(float value, int index);
	
	public int getSize();
	
	/**
	 * Encode received values and send as the ROS message
	 *  
	 * @param startTime
	 * @param endTime
	 */
	public void run(float startTime, float endTime);
}
