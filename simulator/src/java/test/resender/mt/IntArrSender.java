package resender.mt;

import org.apache.commons.logging.Log;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * this thing is for testing of communication by means of int[] messages
 * 
 * @author j
 *
 */
public class IntArrSender extends AbstractNodeMain {



	private final java.lang.String ann2ros = "ann2ros";
	protected final java.lang.String ros2ann = "ros2ann";
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("IntArrSender");
	}
	  @Override
	  public void onStart(final ConnectedNode connectedNode) {
	    final Publisher<std_msgs.Int32MultiArray> publisher =
	        connectedNode.newPublisher(ros2ann, std_msgs.Int32MultiArray._TYPE);
	    
	    // This CancellableLoop will be canceled automatically when the node shuts
	    // down.
	    connectedNode.executeCancellableLoop(new CancellableLoop() {
	      private int sequenceNumber;

	      @Override
	      protected void setup() {
	        sequenceNumber = 0;
	      }

	      @Override
	      protected void loop() throws InterruptedException {
	    	  
	        std_msgs.Int32MultiArray ints = publisher.newMessage();
	        
	        int[] q = new int[10];
	        for(int i=0;i<10; i++)
	        	q[i] = i+sequenceNumber;
	        
	        ints.setData(q);
	        
	        publisher.publish(ints);
	        System.out.println("XX sending the array starting from this num to 10: "+sequenceNumber);
	        
	        sequenceNumber++;
	        Thread.sleep(1000);
	      }
	    });
	  
		  
	    System.out.println("heeeell.. o");
			
	  }
}
