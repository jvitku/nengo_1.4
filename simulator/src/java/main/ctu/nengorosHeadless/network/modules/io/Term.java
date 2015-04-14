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
	 * @return name which corresponds to the owned ROS topic
	 */
	public String getName();
	
	/**
	 * By default, sums all values sent here (e.g. from multiple origins).
	 * 
	 * @param value value to be added
	 * @param index index of value in the Term's array
	 */
	public void sendValue(float value, int index);
	
	public int getSize();
	
	/**
	 * @return vector of float values collected on inputs
	 */
	public float[] getValues();
}
