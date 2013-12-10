package ctu.nengoros.comm.rosutils.utilNode.time.impl;

import org.ros.concurrent.CancellableLoop;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import ctu.nengoros.comm.rosutils.utilNode.time.RosTimeUtil;

import org.apache.commons.logging.Log;

import rosgraph_msgs.Clock;

/**
 * Periodically publishes simulation time across the ROS network to be used by other ROS nodes.
 * This time is taken from the Nengo stimulation step.
 * 
 * This node should be launched with the command line parameter: /use_sim_time:=false
 * 
 * @author Jaroslav Vitku
 *
 */
public class DefaultTimeMaster extends AbstractNodeMain implements RosTimeUtil{
	
	public static final String name = "NengoRosTimeMaster";
	private final String me ="["+name+"] ";
	Log l;
	
	protected final java.lang.String cl = "Clock";
	private final int sleeptime = 1000;

	Publisher<rosgraph_msgs.Clock> pub;
	int poc;
	
	/**
	 * Default name of the ROS node
	 */
	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(name); }

	/**
	 * Method called after launching the node. 
	 * After exiting this method, the node will stop working.
	 */
	@Override
	public void onStart(final ConnectedNode connectedNode) {

		pub = connectedNode.newPublisher(cl, rosgraph_msgs.Clock._TYPE);

		System.out.println(me+"Node started, will publish Nengo clock!");

		Clock mess = pub.newMessage();
		mess.setClock(new Time(0));  // before starting the simulation, the time=0 should be published
		pub.publish(mess);
		
		// ROS uses these cancellable loops
		connectedNode.executeCancellableLoop(new CancellableLoop() {
			
			@Override
			protected void setup() { 
				poc = 0;
			}
			
			@Override
			protected void loop() throws InterruptedException {
				
				Clock mess = pub.newMessage();
				mess.setClock(new Time(0));  // before starting the simulation, the time=0 should be published
				pub.publish(mess);
				
				Thread.sleep(sleeptime);
				System.out.println(me+"hi "+poc++);
			}
		});
	}

	/**
	 * Publish actual time across the ROS network.
	 */
	@Override
	public float[] handleTime(float startTime, float stopTime) {
		
		if(pub==null){
			System.err.println(me+" publisher still not initialized, my ROS node launched already??");
			return new float[]{startTime, stopTime};
		}
		
		Clock mess = pub.newMessage();
		mess.setClock(new Time(stopTime)); // TODO check this
		pub.publish(mess);
		//mess.setClock(tt);

		System.out.println(me+"publishind this time value: "+mess.getClock().toString());
		//tt=tt.add(dd);				// add duration
		
		return new float[]{startTime, stopTime};
	}

}
