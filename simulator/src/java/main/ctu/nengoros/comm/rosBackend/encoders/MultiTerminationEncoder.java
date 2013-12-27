package ctu.nengoros.comm.rosBackend.encoders;

import ca.nengo.model.Node;
import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;

/**
 * <p>Compared to BasicEncoder, this is not a Termination, but can have multiple
 * Terminations, whose values are combined into single value published on a single
 * ROS topic.</p>
 * 
 * <p>One MultiTerminationEncoder is initialized with one own Termination. If this 
 * Termination is already used, the new one, free one, should be simple added.</p>
 * 
 * TODO: only one dimensional Terminations are supported so far.
 * TODO: use methods for Termination to setup parameters all own Terminations at once (e.g. setTau..) 
 * 
 * @author Jaroslav Vitku
 *
 */
public interface MultiTerminationEncoder extends Resettable{

	// Methods similar to the Termination here:
	String getName();
	public int getDimensions();
	public Node getNode();
	
	/**
	 * Runs own MultiTermination, after that, reads its value, encodes it
	 * according to its setup and sends over the ROS network.
	 *   
	 * @param startTime
	 * @param endTime
	 */
	public void run(float startTime, float endTime) throws SimulationException;

	/**
	 * Support for multi-dimensional Termination is TODO.
	 *  
	 * @param weights
	 * @return
	 *
	public String addTermination(float[][] weights);

	/**
	 * Adds one new Termination to own MultiTermination, values on this 
	 * Termination will be added to the resulting value.
	 *  
	 * @param weight connection weight of this Termination
	 * @return name of registered Termination
	 *
	public String addTermination(float weight);

	/**
	 * Adds one new Termination with the default weight, probably 1.
	 * 
	 * @return name of the registered Termination
	 *
	public String addTermination();

	public HashMap<String, Termination> getMyTerminations();
	*/

}
