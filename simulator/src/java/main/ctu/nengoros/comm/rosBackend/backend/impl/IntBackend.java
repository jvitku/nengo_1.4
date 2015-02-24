package ctu.nengoros.comm.rosBackend.backend.impl;


import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.BackendUtils;
import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSource;
import ctu.nengoros.comm.rosBackend.transformations.IntegerTransform;
import ctu.nengoros.comm.rosBackend.transformations.impl.IntegerSimpleRounding;
import ctu.nengoros.exceptions.MessageFormatException;
//import std_msgs.MultiArrayLayout; // TODO
import ctu.nengoros.util.SL;

/**
 * 
 * Receives and publishes Integers on the ROS side, Nengo side sees only arrays of floats.
 * Here just cast Integer to float and the other way..
 * 
 * This serves as a backend for direct communication with ROS nodes. 
 * It can be used either for publishing messages OR for receiving messages.
 * 
 * Publishing: just call method publish()
 * Receiving: just extend this: MyEventListenerInterface and subscribe to events
 * fired by this class by this class. Event informs that new ROS message is 
 * available, then call decodeMessage(Message m) to obtain the float[] data. 
 * 
 * Note: so far this is able to send only 1D vectors of data.
 * 
 * @author Jaroslav Vitku
 *
 */
public class IntBackend extends OnNewRosMessageSource implements Backend{

	// type of messages we can process here
	public static final String MYTYPE="std_msgs/Int32MultiArray";
	
	private String myTopic;
	
	private std_msgs.Int32MultiArray rosMessage;
	
	final Publisher<std_msgs.Int32MultiArray> publisher;
	final Subscriber<std_msgs.Int32MultiArray> subscriber;
	
	private boolean pub;	// whether to publish or subscribe
	
	private int messageLength;	// number of primitive data fields (Nengo floats)
	
	//private MultiArrayLayout ml;
	
	private IntegerTransform itr;	// how to convert values
	
	public IntBackend(String topic, String type, 
			int[] dimensionSizes, ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException{
		
		if(!type.toLowerCase().equals(MYTYPE.toLowerCase())){
			throw new MessageFormatException("BasicIdentity constructor "+topic,
					"Cannot parse message of this type! expected: "+MYTYPE+
					" and found: "+type);
		}else if(!std_msgs.Int32MultiArray._TYPE.equalsIgnoreCase(MYTYPE)){
			throw new MessageFormatException("BasicIdentity constructor "+topic,
					"Variable MYTYPE has to correspond to type of my message!: "
					+MYTYPE+" != : "+std_msgs.Int32MultiArray._TYPE);
		}
		
		this.messageLength = BackendUtils.countNengoDimension(dimensionSizes);
		this.pub = publish;
		this.myTopic = topic;
		this.itr = new IntegerSimpleRounding();
		
		if(pub){
			subscriber = null;
			publisher = myRosNode.newPublisher(myTopic, MYTYPE);
			rosMessage = publisher.newMessage();			
		}else{
			this.publisher=null;
			subscriber = myRosNode.newSubscriber(myTopic, MYTYPE);
			subscriber.addMessageListener(this.buildML(this));
		}
	}

	/**
	 * This thing gets data of given format and publishes them as a ROS message 
	 * to a specified topic. This methods hold the necessary transformations and publishing.
	 */
	@Override
	public void publish(float[] data){
		if(!pub){
			System.err.println("I am set only to subscribe!");
			return;
		}
		rosMessage = publisher.newMessage();
		rosMessage.setData(itr.float2int(data));
        publisher.publish(rosMessage);
	}

	public void setReceivedRosMessage(std_msgs.Int32MultiArray mess){
		this.rosMessage=mess;
	}
	
	/**
	 * On each new message from ROS: store it, fire event: Backend:NewMessage.
	 * @param me this class
	 * @return ROS message listener
	 */
	private MessageListener<std_msgs.Int32MultiArray> buildML(final IntBackend me){
		
		MessageListener<std_msgs.Int32MultiArray> ml = 
				new MessageListener<std_msgs.Int32MultiArray>() {

			@Override
			public void onNewMessage(std_msgs.Int32MultiArray f) {
				me.checkDimensionSizes(f);
				me.setReceivedRosMessage(f);	// save obtained Message
				me.fireOnNewMessage(f);			// inform Nengo about new ROS message
			}
		};
		return ml;
	}
	
	public void checkDimensionSizes(std_msgs.Int32MultiArray mess){
		if(mess.getData().length != this.messageLength)
			System.err.println("IntBackend: ROS message format exception!! " +
					"Expected message with: "+this.messageLength+
					" and found message with length "+mess.getData().length+
					" message is: "+SL.toStr(mess.getData()));
	}

	/**
	 * Get message data (this receives arrays of ints, just cast them to floats).
	 */
	@Override
	public float[] decodeMessage(Message mess) {
		
		int[] ii = ((std_msgs.Int32MultiArray)mess).getData();

		return itr.int2float(ii);
	}
	

	@Override
	public int gedNumOfDimensions() { return messageLength; }
	
	
}
