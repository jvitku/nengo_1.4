package ctu.nengoros.comm.rosBackend.decoders;

import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.MyEventListenerInterface;
import ctu.nengoros.util.sync.SyncedUnitInterface;
import ca.nengo.model.Origin;

/**
 * this is decoder of ROS messages, it does:
 * 	-in constructor should do this:
 * 		-subscribe to a topic which is the same of name of this decoder
 * 		-subscribe to event onNewMessage
 * 
 *  -decoder should decode each message into Nengo format, that is:
 *  	-RealValues
 *  	-or Spikes
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Decoder extends SyncedUnitInterface, Origin, MyEventListenerInterface{

	/**
	 * get name of the termination 
	 * this name corresponds to a ROS topic subscribed
	 * @return
	 */
	String getName();
	
	/**
	 * each decoder should be able to subscribe to a topic which corresponds to its name
	 * @param dec this decoder
	 * @return messageListener for given type of (ROS) data (that is std_msgs.?)
	 */
	//MessageListener<?> buildMessageListener(final Decoder dec);
	
	
	/**
	 * TODO: problem how to sync Nengo and ROS time references
	 * @return
	 */
	float getStartTime();
	float getEndTime();
	
}
