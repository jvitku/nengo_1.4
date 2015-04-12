package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;

/**
 * Headless version of the Nengo's Termination. It has vector of weights
 * (defining the connection weights between output of connected node and 
 * this input). 
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Terminaiton extends Resettable{
	
	public void setWeights(float[][] weights);
	
	/**
	 * These are values that are sent from the connected Origin, the 
	 * weights are applied and the resulting values are saved (e.g. encoded 
	 * and sent to ROS node).
	 *  
	 * @param values vector of values published by the connected Origin
	 */
	public void setValues(float[] values);

	public int getSize();
	

	/**
	 * Called each step, values are read, encoded and sent to the ROS node(s) here.
	 * @param startTime
	 * @param endTime only this time step is used
	 */
	public void run(float startTime, float endTime);
	
}
