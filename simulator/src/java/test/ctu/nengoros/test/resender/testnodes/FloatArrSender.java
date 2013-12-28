package ctu.nengoros.test.resender.testnodes;


import org.apache.commons.logging.Log;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * 
 * this node publishes does:
 * 
 * 	-receives 4 floats 
 * 	-finds the smallest and the biggest one
 * 	-the biggest and the smallest value publishes
 * 
 * @author j
 * 
 */
public class FloatArrSender extends AbstractNodeMain {

//	private final java.lang.String ann2ros = "ann2ros";
	protected final java.lang.String ros2ann = "ros2annFloatArr";

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("FloatArrSender"); }

	@Override
	public void onStart(final ConnectedNode connectedNode) {

		final Log log = connectedNode.getLog();
		
		// define the publisher
		final Publisher<std_msgs.Float32MultiArray> publisher = 
				connectedNode.newPublisher(ros2ann, std_msgs.Float32MultiArray._TYPE);

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
		        std_msgs.Float32MultiArray fl = publisher.newMessage();	// init message
		        
		        float[] data = new float[]{(float)Math.sin(poc),(float)(1.7*Math.cos(2*poc))};
		        
		        fl.setData(data);								// set message data
		        publisher.publish(fl);							// send message
		        log.info("Time: "+poc+" and sending these vals "+data[0]+" "+data[1]);
		        poc=poc+step;
		        Thread.sleep(100);
		      }
		    });
	    
	   log.info("HEY! FloatSender ready now!");
	}

}
