package resender.testnodes;

import java.util.Random;
import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import std_msgs.String;

/**
 * this node simply asynchronously resends received information with some delay
 * this is for purposes of testing of modems
 * 
 * @author j
 * 
 */
public class Resender extends AbstractNodeMain {

	private final java.lang.String ann2ros = "ann2ros";
	protected final java.lang.String ros2ann = "ros2ann";

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("Resender"); }

	@Override
	public void onStart(final ConnectedNode connectedNode) {

		final Log log = connectedNode.getLog();
		
		// define the publisher
		final Publisher<std_msgs.String> publisher = 
				connectedNode.newPublisher(ros2ann, std_msgs.String._TYPE);
		
		// subscribe to given topic
	    Subscriber<std_msgs.String> subscriber = 
	    		connectedNode.newSubscriber(ann2ros, std_msgs.String._TYPE);

	    //listen and respond
	    subscriber.addMessageListener(this.buildML(publisher));
	    
	   log.info("HEY! RESENDER ready now!");
	}
	
	/**
	 * lets build my own message listener
	 * @return
	 */
	private MessageListener<std_msgs.String> buildML(final Publisher<std_msgs.String> p){
		MessageListener<std_msgs.String> ml = new MessageListener<String>() {
			
			
			@Override
			public void onNewMessage(std_msgs.String message) {
				Random r = new Random();
				try {
					Thread.sleep(r.nextInt(14)*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				std_msgs.String str = p.newMessage();
				System.out.println("RESENDING: the message: "+message);
				str.setData("RESPONSE: "+message.getData());
				p.publish(str);				
			}
		};
		return ml;
	}
}
