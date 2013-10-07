package resender.mt;

import java.util.Random;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;

/**
 * this thing is for testing of communication by means of int[] messages
 * 
 * @author j
 */
public class IntSender extends AbstractNodeMain {

	private final String me = "IntSender";

	protected final java.lang.String ros2ann = "ros2ann";

	int id;

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("IntArrSender");
	}
	@Override
	public void onStart(final ConnectedNode connectedNode) {

		id=  new Random().nextInt();

		final Publisher<std_msgs.Int32> publisher =
				connectedNode.newPublisher(ros2ann, std_msgs.Int32._TYPE);
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
				std_msgs.Int32 intt = publisher.newMessage();
				intt.setData(sequenceNumber);

				publisher.publish(intt);
				System.out.println("["+id+" "+me+"] sending this number: "+sequenceNumber);

				sequenceNumber++;
				Thread.sleep(1000);
			}
		});
		System.out.println("["+id+" "+me+"] Hello");
	}
	
	@Override
	public void onShutdown(Node node) {
		System.out.println("["+id+" "+me+"] shutting down!");
	}

	@Override
	public void onShutdownComplete(Node node) {
		System.out.println("["+id+" "+me+"] shutdown complete!");
	}

	
}
