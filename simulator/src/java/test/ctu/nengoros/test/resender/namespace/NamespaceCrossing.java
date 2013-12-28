package ctu.nengoros.test.resender.namespace;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;


/**
 * Test how we can communicate.
 * 
 * @author Jaroslav Vitku
 *
 */
public class NamespaceCrossing extends AbstractNodeMain {

	private final java.lang.String ann2ros = "ann2rosFloatArr";
	protected final java.lang.String ros2ann = "namespaace/ros2annFloatArr";
	protected final java.lang.String other ="cool/ros2annFloatArr";

	private float min;
	private float max;
	
	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("cool/namespace/F2IPubSub"); }

	@Override
	public void onStart(final ConnectedNode connectedNode) {

		final Log log = connectedNode.getLog();

		// define the publisher
		final Publisher<std_msgs.Int32MultiArray> publisher = 
				connectedNode.newPublisher(ros2ann, std_msgs.Int32MultiArray._TYPE);

		final Publisher<std_msgs.Int32MultiArray> publisherr = 
				connectedNode.newPublisher(other, std_msgs.Int32MultiArray._TYPE);

		
		// subscribe to given topic
		Subscriber<std_msgs.Float32MultiArray> subscriber = 
				connectedNode.newSubscriber(ann2ros, std_msgs.Float32MultiArray._TYPE);
		
		subscriber.addMessageListener(new MessageListener<std_msgs.Float32MultiArray>() {
			// print messages to console
			@Override
			public void onNewMessage(std_msgs.Float32MultiArray message) {
				float[] data = message.getData();
				//log.info("received these data: "+toAr(data));
				min = min(data);
				max = max(data);
				//log.info("publishing this: min: "+(double)min+"   max: "+(double)max);
				
				// publish
				std_msgs.Int32MultiArray fl = publisher.newMessage();	
				fl.setData(new int[]{Math.round(min),Math.round(max)});								
				publisher.publish(fl);	
				
				
				// publish to other place something other
				std_msgs.Int32MultiArray fll = publisherr.newMessage();	
				fll.setData(new int[]{Math.round(-min),Math.round(-max)});								
				publisherr.publish(fll);	
				
				
			}
		});

		log.info("HEY! Node is ready now!");
	}

	protected String toAr(float[] f){
		String out = "";
		for(int i=0;i<f.length; i++)
			out = out+"  "+f[i];
		return out;		
	}
	
	private float max(float[] vals){
		float mx = Float.MIN_VALUE;
		for(int i=0;i<vals.length; i++)
			if(mx<vals[i])
				mx=vals[i];
		return mx;
	}
		
	private float min(float[] vals){
		float min = Float.MAX_VALUE;
		for(int i=0;i<vals.length; i++)
			if(min>vals[i])
				min=vals[i];
		return min;	
	}
	
}
