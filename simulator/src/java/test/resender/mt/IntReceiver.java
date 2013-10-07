package resender.mt;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Subscriber;


public class IntReceiver extends AbstractNodeMain {

	private final String me = "IntReceiver";
	protected final java.lang.String ros2ann = "ros2ann";


	int id;

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("IntReceiver"); }

	@Override
	public void onStart(ConnectedNode connectedNode) {

		id = new Random().nextInt();
		System.out.println(id+" "+me+" on start callded");

		final Log log = connectedNode.getLog();
		Subscriber<std_msgs.Int32> subscriber = connectedNode.newSubscriber(ros2ann, std_msgs.Int32._TYPE);

		MessageListener<std_msgs.Int32> ml = new MessageListener<std_msgs.Int32>() {
			@Override
			public void onNewMessage(std_msgs.Int32 message) {
				log.info("\t\t\t["+id+" "+me+"] OK I heard: \"" + message.getData() + "\"");
				System.out.println("\t\t\t["+id+" "+me+"] OK I heard: \"" + message.getData() + "\"");

			}
		};
		subscriber.addMessageListener(ml);
		System.out.println("\t\t\t["+id+" "+me+"] Ready now!");
	}

	@Override
	public void onShutdown(Node node) {
		System.out.println("\t\t\t["+id+" "+me+"] shutting down!");
	}

	@Override
	public void onShutdownComplete(Node node) {
		System.out.println("\t\t\t["+id+" "+me+"] shutdown complete!");
	}

}
