package ctu.nengoros.testsuit.demo.nodeTesters;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import std_msgs.Float32MultiArray;

import ctu.nengoros.nodes.CommunicationAwareNode;
import ctu.nengoros.nodes.topicParticipant.ConnectedParticipantPublisher;

/**
 * This demo shows how to test DemoSubscriber node. This means that we should create
 * a node which will publish data for the DemoSubscriber (a bit over-simplified example).
 *
 * @author Jaroslav Vitku
 *
 */
public class SubscriberTestNode extends CommunicationAwareNode {
	
	// data taken from DemoSubscriber node
	protected final java.lang.String topicIn = "hanns/demonodes/A";
	private final int dataLength = 7;
	private final Random r = new Random();
	
	Log log;
	
	private final int ms = 50;	// how often to send data
	
	/**
	 * Note that the method onStart is called by roscore asynchronously, 
	 * so launching the node and calling its methods is insufficient. First, the
	 * program has to wait for node registration.
	 * 
	 * True when the node is ready (registered, connected..)
	 */
	public boolean isReady(){
		if(!super.communicationReady()){
			this.awaitCommunicationReady();
		}
		return true;
	}
	
	@Override
	public void onStart(final ConnectedNode connectedNode) {

		log = connectedNode.getLog();
		
		final Publisher<std_msgs.Float32MultiArray> publisher = 
				connectedNode.newPublisher(topicIn, std_msgs.Float32MultiArray._TYPE);
		
		// this thing ensures that at least one subscriber is registered
		super.participants.registerParticipant(
				new ConnectedParticipantPublisher<Float32MultiArray>(publisher));
		
		// in case of checking only whether the participant is registered by the ROS core, use this instead:
		//super.participants.registerParticipant(
		//		new ParticipantPublisher<Float32MultiArray>(publisher));
		
		super.nodeIsPrepared();
		super.awaitCommunicationReady();	// wait for all connections before sending any data
		
		// periodically publish random data of dimension 7
		connectedNode.executeCancellableLoop(new CancellableLoop() {

			@Override
			protected void loop() throws InterruptedException {
				Float32MultiArray mess = publisher.newMessage();
				mess.setData(generateData());
				publisher.publish(mess);
				
				Thread.sleep(ms);
			}
		});
	}

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("ExampleTest"); }
	
	private float[] generateData(){
		float[] out = new float[this.dataLength];
		
		for(int i=0;i<out.length; i++)
			out[i] = r.nextInt();
		
		return out;
	}
	
}
