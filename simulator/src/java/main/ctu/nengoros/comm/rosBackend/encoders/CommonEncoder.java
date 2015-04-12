package ctu.nengoros.comm.rosBackend.encoders;

import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;
import ctu.nengoros.modules.NeuralModule;

/**
 * @see the ctu.nengoros.comm.rosBackend.encoders.Encoder
 * 
 * @author Jaroslav Vitku
 *
 */
public interface CommonEncoder extends Resettable{


	/**
	 * Get the parent of Encoder
	 * @return my parent to whom I register my Terminations 
	 */
	NeuralModule getParent();

	/**
	 * Return the name of Encoder, this name corresponds
	 * to ROS Topic on which the messages are published and 
	 * is used to generate names for own Terminations.
	 * 
	 * @return returns the name of Encoder
	 */
	String getName();

	/**
	 * Get dimensions of this encoder.
	 * @return number of dimensions of my encoded input 
	 */
	public int getDimensions();
	
	/**
	 * Run encoder for a given time, that is
	 * <ul>
	 * <li>Combine values on all my Terminations</li>
	 * <li>Encode the result into the ROS message</li>
	 * <li>Publish the ROS message on a topic which corresponds 
	 * to the name of Encoder</li>
	 * </ul>
	 * 
	 *  Note that parent NeuralModule is responsible for running all
	 *  Terminations before running Encoders, so that Terminations
	 *  contain new values while calling this method. 
	 * 
	 * @param startTime
	 * @param endTime
	 */
	public void run(float startTime, float endTime) throws SimulationException;

}
