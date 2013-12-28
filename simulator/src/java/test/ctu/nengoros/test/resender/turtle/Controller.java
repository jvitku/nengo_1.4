package ctu.nengoros.test.resender.turtle;

import java.util.Random;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;


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
public class Controller extends AbstractNodeMain {

	private final java.lang.String ann2ros = "turtle1/command_velocity";
	private final java.lang.String me = "TurtleCommander";

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(me); }


	@Override
	public void onStart(final ConnectedNode connectedNode) {
		final Publisher<turtlesim.Velocity> publisher =
				connectedNode.newPublisher(ann2ros, turtlesim.Velocity._TYPE);

		// This CancellableLoop will be canceled automatically when the node shuts
		// down.
		connectedNode.executeCancellableLoop(new CancellableLoop() {
			private int sequenceNumber;

			@Override
			protected void setup() {
				sequenceNumber = 0;
			}
			Random r = new Random();
			double mul = 3;
			
			double speed = mul*5;		// how fast is turtle
			double turns = mul*3;		// how sharp are her turns
			
			int sleepTime = 200;
			
			@Override
			protected void loop() throws InterruptedException {
				
				turtlesim.Velocity mess = publisher.newMessage();
				
				float[] f = new float[2];	// linear and angular velocities
				f[0] = (float) (-speed/2+speed*r.nextFloat());
				f[1] = (float) turns*r.nextFloat();

				mess.setAngular(f[0]);		// set them and publish message
				mess.setLinear(f[1]);
				publisher.publish(mess);

				System.out.println(me+" step: "+sequenceNumber+ "setting velocities: "+f[0]+
						" and: "+f[1]);

				sequenceNumber++;
				Thread.sleep(sleepTime);
			}
		});
	}

}