package ctu.nengoros.modules;

import ca.nengo.model.Node;
import ca.nengo.model.Probeable;
/**
 * This defines how a NeuralModule should look like. 
 * Basically NeuralModule is a Nengo Node, methods 
 * createEncoder and createDecoder and add ROS (publisher/listener)
 * and connect it to the Nengo Node terminations/origins.
 * In the constructor of NeuralModule there should be instantiation of 
 * Modem, which corresponds to ROS java node, this ROS node is interested in all
 * messages with topics specified in methods createDecoder and createEncoder.
 * 
 * @author Jaroslav Vitku
 *
 */
public interface AsynNeuralModule extends Node, Probeable, PeripheralsRegisteringNode{

	/**
	 * Add decoder to the neural module, that is: subscribe to given ROS topic 
	 * with expected type of data and expected dimensionality. Add origin to the neural
	 * module Node in Nengo, with corresponding dimensionality, which produces 
	 * float values obtained from ROS node. 
	 * Note: name of ROS node is specified by the Modem instance.
	 * 
	 * @param topicName name of the ROS topic
	 * @param dataType type of data ( @See rosjava std_msgs package for more info) 
	 * @param dimensionSizes list of dimension sizes (best support only for 1D vectors)
	 */
	public void createDecoder(String topicName, String dataType, int[] dimensionSizes);
	public void createDecoder(String topicName, String dataType, int dimensionSize);
	public void createDecoder(String topicName, String dataType);

	/**
	 * Add encoder to the neural module, that is: publish to given ROS topic 
	 * with given type of data and given dimensionality. Add termination to the neural
	 * module Node in Nengo, data of float type received on this termination are 
	 * then published in form of ROS messages.
	 * 
	 * Note: name of ROS node is specified by the Modem instance.
	 * 
	 * @param topicName name of the ROS topic
	 * @param dataType type of data ( @See rosjava std_msgs package for more info) 
	 * @param dimensionSizes list of dimension sizes (best support only for 1D vectors)
	 */
	public void createEncoder(String topicName, String dataType, int[] dimensionSizes);
	public void createEncoder(String topicName, String dataType, int dimensionSize);
	public void createEncoder(String topicName, String dataType);

}
