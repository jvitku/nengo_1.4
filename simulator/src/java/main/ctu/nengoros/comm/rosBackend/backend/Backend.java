package ctu.nengoros.comm.rosBackend.backend;

import org.ros.internal.message.Message;

import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.MyEventListenerInterface;
import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSourceInt;


/**
 * 
 * <p>This serves as a backend for direct communication with ROS nodes. 
 * It can be used either for publishing messages OR for receiving messages.</p>
 * 
 * <p>For communication between nodes, the Nengo uses either RealOutput 
 * or SpikeOutput data types. The Real output is implemented as vector of float values.
 * Therefore the Backend accepts and returns vectors of floats. Note that SpikeOutput
 * is not implemented so far.</p> 
 * 
 * <ul>
 * 	<li>Publishing: just call method publish()</li>
 * 	<li>Receiving: just extend this: MyEventListenerInterface and subscribe to events
 * fired by this class. Event informs that new ROS message is available, 
 * then call decodeMessage(Message m) to obtain the float[] data.</li>
 * <ul> 
 * 
 * @see ca.nengo.model.SpikeOutput
 * @see ca.nengo.model.RealOutput
 *  
 * @author Jaroslav Vitku
 *
 */
public interface Backend extends OnNewRosMessageSourceInt{

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
	 * 
	 * @see ctu.nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSource
	 */
	@Override
	public void addEventListener(MyEventListenerInterface listener);

	/**
	 * New message from ROS received.
	 * 
	 * @see ctu.nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSource
	 */
	@Override
	public void fireOnNewMessage(Message mess);


}
