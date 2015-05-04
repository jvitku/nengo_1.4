package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;

/**
 * Compared to the NengoROS, the Term here only serves as encoder and sender of information.
 * The rest is incorporated in the weighted connections
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Term extends Resettable, IO {

	/**
	 * By default, sums all values sent here (e.g. from multiple origins).
	 * 
	 * @param value value to be added
	 * @param index index of value in the Term's array
	 */
	public void sendValue(float value, int index);
	
	/**
	 * Manually set values of the termination (in the simulator).
	 * 
	 * @param values array of new values, ignored if the array has incorrect length.
	 * @throws SimulationException 
	 */
	public void setValues(float[] values) throws SimulationException;
	
	
	/**
	 * @return vector of float values collected on inputs
	 */
	public float[] getValues();
}
