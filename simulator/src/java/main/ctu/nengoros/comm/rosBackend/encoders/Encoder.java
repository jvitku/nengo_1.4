package ctu.nengoros.comm.rosBackend.encoders;


import ctu.nengoros.comm.rosBackend.encoders.multiTermination.MultiTermination;
import ctu.nengoros.modules.NeuralModule;
import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;

/**
 * <p>Each Encoder is allowed to register one or more own Nengo Terminations.
 * Encoder registers own Termination to own parent (NeuralModule). This means
 * that NeuralModule is responsible for correct order of running the components:
 * first run all the registered Terminations, then run all Encoders.</p>
 * 
 * <p>Encoder should:
 * <ul>
 * 	<li>Register new ROS message publisher in the constructor. Here, the ROS 
 * topic on which are messages published equals to the name of Encoder.</li>.
 * 
 * 	<li>In each call of method run() collects input values on all own Terminations,
 * encodes the result into the ROS message and publishes on the ROS topic.</li>
 * </ul>
 * </p>
 *  
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Encoder extends Resettable{

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
	 * TODO: only one-dimensional Terminations are supported currently  
	 * @return number of dimensions of my encoded input 
	 */
	public int getDimensions();

	/**
	 * Adds new Termination to the encoder (that is, builds it and registers it
	 * to the parent NeuralModule). 
	 * 
	 * @return new Termination with auto-generated name  
	 */
	public Termination addTermination() throws StructuralException;
	
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

	
	/**
	 * Return own MultiTermination, this allows user to add new Terminations
	 * to it.
	 * @return MultiTermination if found
	 */
	public MultiTermination getMultiTermination() throws StructuralException;
}
