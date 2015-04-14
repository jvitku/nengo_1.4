package ctu.nengorosHeadless.rosBackend.decoders;

import ctu.nengoros.comm.rosBackend.decoders.CommonDecoder;

/**
 * this is decoder of ROS messages, it does:
 * 
 * 	-in constructor should do this:
 * 		-subscribe to a topic which is the same of name of this decoder
 * 		-subscribe to event onNewMessage
 * 
 *  -decoder should decode each message into Nengo format, that is:
 *  	-RealValues
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Decoder extends CommonDecoder{

	/**
	 * get name of the termination 
	 * this name corresponds to a ROS topic subscribed
	 * @return
	 */
	String getName();
	
	float getEndTime();
}
