package resender.onoff;

import java.util.Random;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

/**
 * this thing is for testing of communication by means of int[] messages
 * 
 * @author j
 *
 */
public class Talker extends AbstractNodeMain {


	private int id;

	private final java.lang.String ann2ros = "ann2ros";
	protected final java.lang.String ros2ann = "ros2ann";
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("IntArrSender");
	}
	  @Override
	  public void onStart(final ConnectedNode connectedNode) {
	    final Publisher<std_msgs.Int32> publisher =
	        connectedNode.newPublisher(ros2ann, std_msgs.Int32._TYPE);
	    // This CancellableLoop will be canceled automatically when the node shuts
	    // down.
	    
	    System.out.println("MYYYYYYYY name is: "+connectedNode.getName() );

		Random r = new Random();
		id = r.nextInt();	//generate unique ID
		
	    connectedNode.executeCancellableLoop(new CancellableLoop() {
	      private int sequenceNumber;

	      @Override
	      protected void setup() {
	        sequenceNumber = 0;
	      }

	      @Override
	      protected void loop() throws InterruptedException {
	        std_msgs.Int32 intt = publisher.newMessage();
	        intt.setData(sequenceNumber);
	        
	        publisher.publish(intt);
	        System.out.println("ID:"+id+" sending this kamo: "+sequenceNumber);
	        
	        sequenceNumber++;
	        Thread.sleep(1000);
	      }
	    });
	  
		  
	    System.out.println("heeeell.. o");
			
	  }
}
