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
	 * For the ROS stuff, this does nothing. For the custom nodes (as inputs) this implements its function
	 * . 
	 * @param startTime
	 * @param endTime
	 */
	public void run(float startTime, float endTime)  throws SimulationException;
}
