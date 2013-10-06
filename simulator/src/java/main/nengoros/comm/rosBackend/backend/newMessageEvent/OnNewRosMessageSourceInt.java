package nengoros.comm.rosBackend.backend.newMessageEvent;

/**
 * Source of new message from an external ROS node.
 */
import org.ros.internal.message.Message;

public interface OnNewRosMessageSourceInt {
	
	public void addEventListener(MyEventListenerInterface listener);

	public void removeEventListener(MyEventListenerInterface listener);
	
	public void fireOnNewMessage(Message mess);
}
