package ctu.nengoros.test.resender.turtle;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;
import turtlesim.Pose;


/**
 * Should be able to read position of turtle in turtlesim: http://www.ros.org/wiki/turtlesim
 * 
 * Receives turtlesim/Pose data in the following format:
 * float32 x
 * float32 y
 * float32 theta
 * 
 * float32 linear_velocity
 * float32 angular_velocity"
 * 
 * 
 * on the topic /turtle1/command_velocity
 * 
 * for more information about the message format, see project rosjava_messages/turtlesim
 * 
 * @author Jaroslav Vitku
 *
 */
public class PositionSensor extends AbstractNodeMain {

	private final java.lang.String ros2ann = "turtle1/pose";
	private final java.lang.String me = "TurtlePositionSensor";

	private int poc;
	private int mod = 20;
	
	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(me); }

	@Override
	public void onStart(final ConnectedNode connectedNode) {
		
		poc = 0;
		
		Subscriber<turtlesim.Pose> subscriber = 
				connectedNode.newSubscriber(ros2ann, turtlesim.Pose._TYPE);
		
		subscriber.addMessageListener(new MessageListener<turtlesim.Pose>() {
			
			@Override
			public void onNewMessage(Pose p) {
				poc++;
				if((poc%mod)!=0)
					return;
				
				float[] data = new float[5];
				data[0] = p.getX();
				data[1] = p.getY();
				data[2] = p.getTheta();
				
				data[3] = p.getLinearVelocity();
				data[4] = p.getAngularVelocity();

				System.out.println(me+" turtle X,Y,Theta position: "+data[0]+" "+data[1]+"" +
						" "+data[2]+"\t velocities: " +data[3]+" "+data[4]);
			}
		});
	}

}
