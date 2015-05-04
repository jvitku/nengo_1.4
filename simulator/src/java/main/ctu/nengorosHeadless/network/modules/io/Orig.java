package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;

public interface Orig extends Resettable, IO{
	
	/**
	 * Values that are sent through the network.
	 * 
	 * @return array of values that is sent through the Connection
	 */
	public float[] getValues();

	/**
	 * For the ROS stuff, this does nothing. For the custom nodes (as inputs) this implements its function
	 * . 
	 * @param startTime
	 * @param endTime
	 */
	public void run(float startTime, float endTime)  throws SimulationException;
}
