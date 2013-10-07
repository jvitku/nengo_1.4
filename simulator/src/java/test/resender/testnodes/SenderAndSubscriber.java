package resender.testnodes;

import org.apache.commons.logging.Log;
import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * this node simply asynchronously resends received information with some delay
 * this is for purposes of testing of modems
 * 
 * @author j
 * 
 */
public class SenderAndSubscriber extends AbstractNodeMain {

	private final String topic = "top";

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("resender");
	}

	@Override
	public void onStart(final ConnectedNode connectedNode) {

		/**
		 * subscriber part (just adds message listener)
		 */
		final Log log = connectedNode.getLog();
		// subscribe to given topic
		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(
				topic, std_msgs.String._TYPE);
		subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
			// print messages to console
			@Override
			public void onNewMessage(std_msgs.String message) {
				log.info("I heard message: \"" + message.getData()
						+ "\" published on topic: '" + topic + "'");
			}
		});

		/**
		 * publisher part
		 */
		final Publisher<std_msgs.String> publisher = connectedNode
				.newPublisher("chatter", std_msgs.String._TYPE);
		// This CancellableLoop will be canceled automatically when the node
		// shuts
		// down.
		connectedNode.executeCancellableLoop(new CancellableLoop() {
			private int sequenceNumber;

			@Override
			protected void setup() {
				sequenceNumber = 0;
			}

			@Override
			protected void loop() throws InterruptedException {
				std_msgs.String str = publisher.newMessage();
				str.setData("Hello world! " + sequenceNumber);
				publisher.publish(str);
				sequenceNumber++;
				Thread.sleep(1000);
			}
		});
	}
}
