package nengoros.modules;

/**
 * The same as NeuralModule, but it adds support for synchronous communication.
 * 
 * @author Jaroslav Vitku
 *
 */
public interface NeuralModule extends AsynNeuralModule{

	/**
	 * The same, but boolean synchronous tells whether to wait for incoming message after some
	 * message was sent by the neural module (most likely after each step). If the parameter is not
	 * specified, the default value of synchronous is used.   
	 * @param topicName
	 * @param dataType
	 * @param dimensionSizes
	 * @param synchronous - wait for incoming message after corresponding sent message?
	 */
	public void createDecoder(String topicName, String dataType, int[] dimensionSizes, boolean synchronous);
	public void createDecoder(String topicName, String dataType, int dimensionSize, boolean synchronous);
	public void createDecoder(String topicName, String dataType, boolean synchronous);
	
	/**
	 * Turn synchronous communication on/off
	 *  
	 * @param synchronous
	 */
	public void setSynchronous(boolean synchronous);
}
