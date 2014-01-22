package ctu.nengoros.testsuit.demo.nodeTesters;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import ctu.nengoros.network.node.testsuit.CommunicationAwareNode;
import ctu.nengoros.network.node.testsuit.topicParticipant.ConnectedParticipantSubscriber;
import ctu.nengoros.network.node.testsuit.topicParticipant.RegisteredTopicParticipant;
import std_msgs.Float32MultiArray;


/**
 * This demo shows how to test DemoPublisher node. This means that we should create
 * a node which will receive data for the DemoPublisher and check connected status.
 *
 * @author Jaroslav Vitku
 *
 */
public class PublisherTestNode extends CommunicationAwareNode {

	protected final java.lang.String topicIn = "hanns/demonodes/A";
	private final int dataLength = 7;

	Log log;

	private int receivedMessages = 0;
	
	public boolean somethingReceived(){
		return receivedMessages>0;
	}

	@Override
	public void onStart(final ConnectedNode connectedNode) {

		log = connectedNode.getLog();

		// subscribe to given topic
		Subscriber<std_msgs.Float32MultiArray> subscriber = 
				connectedNode.newSubscriber(topicIn, std_msgs.Float32MultiArray._TYPE);
		// create listener
		subscriber.addMessageListener(new MessageListener<std_msgs.Float32MultiArray>() {
			@Override
			public void onNewMessage(std_msgs.Float32MultiArray message) {
				float[] data = message.getData();
				if(data.length != dataLength){
					log.error("Received message has unexpected length of"+data.length+"!");
					fail("Received message has unexpected length of"+data.length+"!");
				}	
				else{
					receivedMessages++;
					log.info("Received these data: "+toAr(data));
				}
			}
		});

		// this thing ensures that at least one subscriber is registered
		super.participants.registerParticipant(
				(RegisteredTopicParticipant)new ConnectedParticipantSubscriber<Float32MultiArray>(subscriber));

		// in case of checking only whether the participant is registered by the ROS core, use this instead:
		//super.participants.registerParticipant(
		//		new ParticipantSubscriber<Float32MultiArray>(subscriber));
	
		// wait for preconditions: registered to master and some subscriber connected
		super.nodeIsPrepared();
	}

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("PubisherTestNode"); }

	private String toAr(float[] f){
		String out = "";
		for(int i=0;i<f.length; i++)
			out = out+"  "+f[i];
		return out;		
	}

}
