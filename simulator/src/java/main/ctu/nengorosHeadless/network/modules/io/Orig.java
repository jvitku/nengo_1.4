package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;

public interface Orig extends Resettable{

	/**
	 * Should correspond to the owned ROS topic.
	 * @return name of this origin
	 */
	public String getName();
	
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
	 * The method will return control after the ROS message is received (in the synchronous mode).
	 * 
	 * @param startTime
	 * @param endTime
	 */
	//public void run(float startTime, float endTime)  throws SimulationException;
}
