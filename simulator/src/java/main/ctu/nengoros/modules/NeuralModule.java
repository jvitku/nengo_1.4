package ctu.nengoros.modules;

//import ctu.nengoros.model.multiTermination.MultiTermination;
import ctu.nengoros.model.transformMultiTermination.MultiTermination;
import ca.nengo.model.Node;
import ca.nengo.model.Probeable;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;

/**
 * <p>NeuralModule is similar to SimpleNode, but adds the support for ROS communication.</p>
 * 
 * <p>Each Module has own ROS modem. The Modem is a ROS node which provides access to 
 * the ROS Publisher/Subscriber to the Nengo simulator.</p>  
 * 
 * <p>Each Module has own set of Encoders for encoding received data (on Termination(s))
 * into ROS messages and set of Decoders for decoding data from received ROS messages
 * and setting them on the corresponding Origin.</p>
 * 
 * <p>Encoder has own ROS Publisher which publishes messages to given ROS topic.
 * Decoder has own ROS Subscriber which is subscribed to (receives) selected ROS messages.</p>
 * 
 * @author Jaroslav Vitku
 *
 */
public interface NeuralModule extends PeripheralsRegisteringNode, Node, Probeable{

	/**
	 * <p>This NeuralModule supports connecting multiple "inputs" to one Encoder.
	 * It is accomplished by the fact that each Encoder has MultiTermination, where
	 * the MultiTermination can have several own Terminations, whose values are
	 * combined into one value. </p> 
	 * 
	 * <p>The user is allowed to create new Terminations for Encoder. In order to 
	 * do this, the Encoders MultiTermination has to be available. The MultiTermination
	 * is generated with identical name as the Encoder. This method provides access
	 * to the Encoders MultiTermination by its name.</p>
	 * 
	 * @param name name of the Encoder==MultiTermination
	 * @return MultiTermination if found
	 * @throws StructuralException if no Encoder of such name found
	 */
	public MultiTermination getMultiTermination(String name) throws StructuralException;
	
	/**
	 * <p>In order to simplify connecting new inputs to the Encoder (MultiTermination in general), 
	 * this method can be use to create new Terminations. It receives name of the Encoded 
	 * ROS topic (name of MultiTermination), crates new Termination with default weight (1), 
	 * registers it to own MultiTermination and this NauralModule.</p>
	 * 
	 * <p>The MultiTermination in DefaultNeuralModule computes weighted sum of all its
	 * Terminations. The resulting value is then encoded and sent as a ROS message on the
	 * topic which corresponds to the Encoders name.</p>
	 * 
	 * @param name name of MultiTermination to connect newly created Termination (this equals
	 * to the name of ROS topic used by Encoder)
	 * @return newly created Termination
	 * @throws StructuralException if the Termination could not be created (e.g. Encoder not found,
	 * name is used etc.)
	 * 
	 * {@link ctu.nengoros.comm.rosBackend.encoders.Encoder#getMultiTermination()}
	 * {@link ctu.nengoros.model.multiTermination.MultiTermination#addTermination()}
	 */
	public Termination newTerminationFor(String name) throws StructuralException;
	
	/**
	 * The same as the {@link ctu.nengoros.modules.NeuralModule#newTerminationFor(String)}, 
	 * but here the weight can be specified. 
	 * 
	 * @param name name of the ROS topic == name of the Encoder == name of the parent MultiTermination 
	 * @param weight terminations input is weighted
	 * @return newly created Termination
	 * @throws StructuralException if the Termination could not be created
	 * 
	 * @see #newTerminationFor(String)
	 * {@link ctu.nengoros.model.multiTermination.MultiTermination#addTermination(float)}
	 */
	public Termination newTerminationFor(String name, float weight) throws StructuralException;
	
	/**
	 * The same as the {@link ctu.nengoros.modules.NeuralModule#newTerminationFor(String)}, 
	 * but here the weight for each dimension can be specified.
	 * 
	 * @param name name of the Encoder
	 * @param weights array of weights, for each dimension one weight
	 * @return newly created Termination 
	 * @throws StructuralException exception if the Termination could not be created (e.g. dimension
	 * of weight array provided is incorrect)
	 * 
	 *  @see #newTerminationFor(String)
	 *  {@link ctu.nengoros.model.multiTermination.MultiTermination#addTermination(Float[])}
	 */
	//public Termination newTerminationFor(String name, Float[] weights) throws StructuralException;
	
	/**
	 * The same as {@link ctu.nengoros.modules.NeuralModule#newTerminationFor(String,float)}, but here
	 * the new termination implements transformation between some multi-dimensional Origin and the 
	 * corresponding, potentially multi-dimensional Encoder.
	 * 
	 * @param name name of termination (encoder) to which the new Termination add
	 * @param weights matrix of weights which represents the transformation, first dimension defines 
	 * dimensionality of an Origin that can be connected, the second dimension has to correspond
	 * to the dimension of this Encoder.   
	 * @return newly created Termination
	 * @throws StructuralException exception if the Termination could not be created (e.g. dimensions inconsistent)
	 */
	public Termination newTerminationFor(String name, float[][] weights) throws StructuralException;
	
	
	/**
	 * <p>Turn synchronous communication on/off. Node is synchronous by default.
	 * If the synchronous communication is turned on, the Nengo simulator waits for
	 * messages from all synchronous Decoders of the Module each simulation step.</p>
	 * 
	 *  <p>If the Module is set to asynchronous mode: the simulation does not wait
	 *  for any of its childs. In the synchronous mode, only synchronous Decoders can 
	 *  block the simulation</p>. 
	 *  
	 * @param synchronous
	 */
	public void setSynchronous(boolean synchronous);

	/**
	 * Add the Decoder to the Module, that is:
	 * <ul>
	 * 	<li>Subscribe to given ROS topic with expected type of data and expected dimensionality.</li> 
	 * 	<li>Add Decoder to Node in the Nengo with corresponding dimensionality. The Decoder 
	 * converts ROS messages to array of float values expected by the Nengo RealOutput.</li>
	 * 	<li>The decoder is allowed to add own Origin to this Node. This Origin then produces
	 * data received by the ROS subscriber</li>
	 * </ul>
	 * 
	 * Note: the name of ROS node is specified by the Modem instance.
	 * 
	 * @param topicName name of the ROS topic
	 * @param dataType type of data 
	 * @param dimensionSizes list of dimension sizes (best support only for 1D vectors)
	 * 
	 * @see <a href="http://docs.rosjava.googlecode.com/hg/rosjava_core/html/index.html">rosjava_core doc</a>
	 */
	public void createDecoder(String topicName, String dataType, int[] dimensionSizes);
	public void createDecoder(String topicName, String dataType, int dimensionSize);
	public void createDecoder(String topicName, String dataType);

	/**
	 * Add the Encoder to the neural module, that is:
	 * <ul>
	 * 	<li>Encoder is allowed to register own (potentially multiple) Termination(s) to the Node</li>
	 * 	<li>Encoder encodes data received on own Termination(s) into the ROS 
	 * message according to the specified dataType and dimensionality</li>
	 * 	<li>Encoder then publishes ROS message to given topicName each 
	 * simulation step.</li>
	 * </ul>
	 * 
	 * Note: name of ROS node is specified by the Modem instance.
	 * 
	 * @param topicName name of the ROS topic
	 * @param dataType type of data 
	 * @param dimensionSizes list of dimension sizes (best support only for 1D vectors)
	 * 
	 * @see <a href="http://docs.rosjava.googlecode.com/hg/rosjava_core/html/index.html">rosjava_core doc</a>
	 */
	public void createEncoder(String topicName, String dataType, int[] dimensionSizes);
	public void createEncoder(String topicName, String dataType, int dimensionSize);
	public void createEncoder(String topicName, String dataType);

	/**
	 * <p>The same as createDecoder, but the parameter synchronous tells whether 
	 * the created Decoder will be synchronous. Nengo simulator will wait for all
	 * SynchedUnits each simulation step until they are ready.</p>
	 * 
	 * <p>In other words: synchronous Decoders block the Nengo simulation each time step
	 * until they receive ROS message on own ROS topic.</p>
	 *    
	 * @param topicName name of the ROS topic
	 * @param dataType type of data 
	 * @param dimensionSizes list of dimension sizes (best support only for 1D vectors)
	 * @param synchronous wait for incoming message after corresponding sent message?
	 * 
	 * @see <a href="http://docs.rosjava.googlecode.com/hg/rosjava_core/html/index.html">rosjava_core doc</a>
	 */
	public void createDecoder(String topicName, String dataType, int[] dimensionSizes, boolean synchronous);
	public void createDecoder(String topicName, String dataType, int dimensionSize, boolean synchronous);
	public void createDecoder(String topicName, String dataType, boolean synchronous);

}
