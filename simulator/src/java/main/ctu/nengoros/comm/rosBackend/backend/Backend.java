package ctu.nengoros.comm.rosBackend.backend;

import org.ros.internal.message.Message;

import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.MyEventListenerInterface;
import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSourceInt;


/**
 * 
 * This serves as a backend for direct communication with ROS nodes. 
 * It can be used either for publishing messages OR for receiving messages.
 * 
 * Nengo uses only vectors of floats (in case of real values are sent), 
 * so the Backend accepts and returns only this kind of data for now. 
 * 
 * Publishing: just call method publish()
 * Receiving: just extend this: MyEventListenerInterface and subscribe to events
 * fired by this class. Event informs that new ROS message is 
 * available, then call decodeMessage(Message m) to obtain the float[] data. 
 * 
 *  
 * @author Jaroslav Vitku
 *
 */
public interface Backend extends OnNewRosMessageSourceInt{
	
	/**
	 * Here is what information should be passed to the constructor.
	 */
	/*
	public Backend(String topicName, String dataType, 
			int[] dimensionSizes, ConnectedNode myRosNode) {}
	*/
	
	/**
	 * This transforms given array of floats (from Nengo) 
	 * to given data type and publishes message with data 
	 * the to a given ROS topic.
	 * @param data
	 */
	void publish(float[] data);
	
	
	/**
	 * This method decodes the last available message from ROS 
	 * into Nengo format (that is an array of floats)
	 * @return
	 */
	float[] decodeMessage(Message mess);
	
	/**
	 * return number of floats that are connected to Nengo side. 
	 * 
	 * @return dimension of data for Nengo
	 */
	int gedNumOfDimensions();
	
	/** 
	 * Adds the listener which waits for new message from ROS events.
	 * @See OnNewRosMessageSource
	 */
	@Override
	public void addEventListener(MyEventListenerInterface listener);

	/**
	 * New message from ROS received.
	 * @See OnNewRosMessageSource
	 */
	@Override
	public void fireOnNewMessage(Message mess);
	

}
