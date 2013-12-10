package ctu.nengoros.comm.rosutils.utilNode.time.impl;

import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import org.apache.commons.logging.Log;

import ctu.nengoros.comm.rosutils.utilNode.time.RosTimeUtil;

/**
 * Here, the Nengo simulator waits for new tick from external clock, reads its value and passes this
 * to the simulation step. 
 * 
 * TODO: do this better, now it seems that simulator does not use the time received correctly.
 * 
 * @author Jaroslav Vitku
 *
 */
public class DefaultTimeSlave extends AbstractNodeMain implements RosTimeUtil{

	public static int waitTime = 10;
	private int div = 50;
	public static final String name = "NengoRosTimeSlave";
	private final String me ="["+name+"] ";

	private float lastRead = 0;	// read by the Nengo
	private float lastReceived = 0;

	Log l;
	String cl = "/clock";
	Subscriber<rosgraph_msgs.Clock> subscriber;
	
	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(name); }


	@Override
	public void onStart(ConnectedNode connectedNode){
		l = connectedNode.getLog();
		
		// subscribe to given topic
		subscriber = connectedNode.newSubscriber(cl, rosgraph_msgs.Clock._TYPE);

		subscriber.addMessageListener(new MessageListener<rosgraph_msgs.Clock>() {
			// print messages to console
			@Override
			public void onNewMessage(rosgraph_msgs.Clock message) {
				Time tt = message.getClock();

				// convert to seconds in float 
				lastReceived = (float)(tt.secs+tt.nsecs/1000000000.0);
				
				l.info(me+"Received this time: "+lastReceived);
			}
		});
	}
	
	@Override
	public float[] handleTime(float startTime, float endTime) {
		int poc = 0;
		// wait until some new tick is not received
		while(lastRead == lastReceived){
			try {
				
				if(poc*waitTime%div==0)
					l.debug(me+"Waiting for clock tick from an external TimeProvider.."+(poc++));
				
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		l.debug(me+"will simulate this "+lastRead+" "+lastReceived);
		
		float[] out = new float[]{lastRead, lastReceived};
		lastRead = lastReceived;
		return out;
	}


	@Override
	public void simulationStopped() {
	}
}
