package ctu.nengoros.test.resender.testnodes;


import org.apache.commons.logging.Log;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * this node sends one float in a ROS message
 * 
 * function sent is sin(t) e.g..
 * 
 * @author j
 * 
 */
public class FloatSender extends AbstractNodeMain {

//	private final java.lang.String ann2ros = "ann2ros";
	protected final java.lang.String ros2ann = "ros2annFloat";

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("FloatSender"); }

	@Override
	public void onStart(final ConnectedNode connectedNode) {

		final Log log = connectedNode.getLog();
		
		// define the publisher
		final Publisher<std_msgs.Float32> publisher = 
				connectedNode.newPublisher(ros2ann, std_msgs.Float32._TYPE);

		// ROS uses these cancellable loops
		connectedNode.executeCancellableLoop(new CancellableLoop() {
		      private float poc, step;

		      @Override
		      protected void setup() {
		        poc = 0;
		        step = (float)0.1;
		      }

		      @Override
		      protected void loop() throws InterruptedException {
		        std_msgs.Float32 fl = publisher.newMessage();	// init message
		        fl.setData((float)Math.sin(poc));								// set message data
		        publisher.publish(fl);							// send message
		        log.info("Time: "+poc+" and sending this val "+(float)Math.sin(poc));
		        poc=poc+step;
		        Thread.sleep(100);
		      }
		    });
	    
	   log.info("HEY! FloatSender ready now!");
	}

}
