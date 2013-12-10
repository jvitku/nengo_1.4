package ctu.nengoros.comm.rosutils.utilNode.time.impl;

import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import ctu.nengoros.comm.rosutils.utilNode.time.RosTimeUtil;

public class DefaultTimeSlave extends AbstractNodeMain implements RosTimeUtil{

	public static int waitTime = 10;
	private int div = 50;
	public static final String name = "NengoRosTimeSlave";
	private final String me ="["+name+"] ";

	private float lastRead = 0;	// read by the Nengo
	private float lastReceived = 0;

	String cl = "/clock";
	private final int sleeptime = 300;
	Subscriber<rosgraph_msgs.Clock> subscriber;
	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(name); }


	@Override
	public void onStart(ConnectedNode connectedNode){
		// subscribe to given topic
		subscriber = connectedNode.newSubscriber(cl, rosgraph_msgs.Clock._TYPE);

		subscriber.addMessageListener(new MessageListener<rosgraph_msgs.Clock>() {
			// print messages to console
			@Override
			public void onNewMessage(rosgraph_msgs.Clock message) {
				Time tt = message.getClock();

				// convert to seconds in float 
				lastReceived = (float)(tt.secs+tt.nsecs/1000000000.0);
				
				System.out.println("----RECEIVED message with this time: "+lastReceived);
			}
		});

		// just sleep and use the listener (above)
		this.monitorTime(connectedNode);
	}

	private void monitorTime(final ConnectedNode connectedNode){

		// ROS uses these cancellable loops
		connectedNode.executeCancellableLoop(new CancellableLoop() {

			@Override
			protected void setup() {
			}

			@Override
			protected void loop() throws InterruptedException {
				Thread.sleep(sleeptime);
			}
		});
	}
	
	@Override
	public float[] handleTime(float startTime, float endTime) {
		int poc = 0;
		// wait until some new tick is not received
		while(lastRead == lastReceived){
			try {
				/*
				if(poc==0 || poc++%div==0)
					System.out.println("waiting for new clock tick.. "+poc);
					*/
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println(me+"will simulate this "+lastRead+" "+lastReceived);
		
		float[] out = new float[]{lastRead, lastReceived};
		lastRead = lastReceived;
		return out;
	}
}
