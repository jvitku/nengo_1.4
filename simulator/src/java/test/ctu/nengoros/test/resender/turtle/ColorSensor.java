package ctu.nengoros.test.resender.turtle;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import turtlesim.Color;


/**
 * Should be able to control turtle in turtlesim: http://www.ros.org/wiki/turtlesim
 * 
 * Sends commands in the following format:
 * 	float32 linear
 *	float32 angular
 * 
 * on the topic /turtle1/command_velocity
 * 
 * for more information about the message format, see project rosjava_messages/turtlesim
 * 
 * @author Jaroslav Vitku
 *
 */
public class ColorSensor extends AbstractNodeMain {

	private final java.lang.String ros2ann = "turtle1/color_sensor";
	private final java.lang.String me = "TurtleColorSensor";

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(me); }


	@Override
	public void onStart(final ConnectedNode connectedNode) {
		

		// subscribe to given topic
		Subscriber<turtlesim.Color> subscriber = 
				connectedNode.newSubscriber(ros2ann, turtlesim.Color._TYPE);
		
		subscriber.addMessageListener(new MessageListener<turtlesim.Color>() {
			
			@Override
			public void onNewMessage(Color c) {
				float[] col = new float[3];
				col[0] = c.getR();
				col[1] = c.getG();
				col[2] = c.getB();
				
				System.out.println(me+" turtle sees this RGB color: "+col[0]+" "+col[1]+" "+col[2]);
			}
		});
		
	}

	
	protected String toAr(float[] f){
		String out = "";
		for(int i=0;i<f.length; i++)
			out = out+"  "+f[i];
		return out;		
	}


}
