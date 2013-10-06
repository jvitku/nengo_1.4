package nengoros.comm.rosBackend.backend.newMessageEvent;


import org.ros.internal.message.Message;

/**
 * This have to implement all decoders. 
 * This event is fired if new message from ROS is received.
 * 
 * @author Jaroslav Vitku
 *
 */
public interface MyEventListenerInterface {
	
    public void onNewRosMessage(Message rosMessage);
    
}
