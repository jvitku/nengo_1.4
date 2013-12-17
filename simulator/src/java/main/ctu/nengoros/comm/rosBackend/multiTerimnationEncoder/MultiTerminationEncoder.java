package ctu.nengoros.comm.rosBackend.multiTerimnationEncoder;

import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;

/**
 * Thing that may hold multiple Terminations, values of its 
 * terminations are summed together to provide the final value 
 * that is be encoded and sent over the ROS network.
 *  
 * 
 * @author Jaroslav Vitku
 *
 */
public interface MultiTerminationEncoder extends Resettable/*, Terminatoin*/{

	
	/**
	 * First, run all my terminations (compute their data)
	 * 
	 * @param startTime start time of the simulation step
	 * @param endTime end time of the simulaiton step
	 * @throws SimulationException
	 */
	public void runAllTerminations(float startTime, float endTime) throws SimulationException;
	
	/**
	 * Second, collect data on all my terminations and do the same as
	 * the Encoder would do.
	 *  
	 * @param startTime
	 * @param endTime
	 * @throws SimulationException
	 */
	public void runCollectDataOnTerminations(float startTime, float endTime) throws SimulationException;

}
