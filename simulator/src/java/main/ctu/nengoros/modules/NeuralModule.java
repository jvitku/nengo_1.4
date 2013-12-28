package ctu.nengoros.modules;

import ca.nengo.model.Node;
import ca.nengo.model.Probeable;

/**
 * 
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
 * 
 * @author Jaroslav Vitku
 *
 */
public interface NeuralModule extends PeripheralsRegisteringNode, Node, Probeable{

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
