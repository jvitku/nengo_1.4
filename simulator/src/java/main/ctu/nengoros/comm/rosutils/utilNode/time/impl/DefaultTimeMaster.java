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
	private boolean simRunning = false;
	private final int sleepTime = 500;

	protected final java.lang.String cl = "/clock";

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
		l = connectedNode.getLog();

		l.info(me+"Node started, will publish Nengo clock!");

		connectedNode.executeCancellableLoop(new CancellableLoop() {
			@Override
			protected void setup() { 
			}

			
			@Override
			protected void loop() throws InterruptedException {

				if(!simRunning)
					publishZero(connectedNode);
				
				Thread.sleep(sleepTime);
			}
		});
	}

	/**
	 * Scenario: Nengo starts, python script starts this TimeMaster, 
	 * python script adds ROS nodes, which will not initialize (@see ctu.nengoros.time.AbstractTimeNode ), 
	 * si before the simulation starts, this will periodically publish time = 0
	 * @param cn ConnectedNode
	 */
	private void publishZero(ConnectedNode cn){
		Clock mess = pub.newMessage();
		mess.setClock(new Time(0)); 
		pub.publish(mess);
	}

	/**
	 * Publish actual time (endTime) across the ROS network.
	 */
	@Override
	public float[] handleTime(float startTime, float endTime) {

		if(pub==null){
			System.err.println(me+" publisher still not initialized, my ROS node launched already??");
			return new float[]{startTime, endTime};
		}

		Clock mess = pub.newMessage();
		mess.setClock(new Time(endTime));
		pub.publish(mess);

		float end = (float)(mess.getClock().secs+mess.getClock().nsecs/1000000000.0);
		l.debug(me+"publishind this time value: "+end+" where nengo gave me this endTime: "+endTime);

		return new float[]{startTime, endTime};
	}

	/**
	 * If new script is launched, the TimeMaster does not have to be restarted.
	 * But we need to publish periodically zero again..
	 */
	@Override
	public void simulationStopped() { this.simRunning = false; }
	
}
