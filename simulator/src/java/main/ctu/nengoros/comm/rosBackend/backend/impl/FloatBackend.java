package ctu.nengoros.comm.rosBackend.backend.impl;



import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.BackendUtils;
import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSource;
import ctu.nengoros.exceptions.MessageFormatException;
//import std_msgs.MultiArrayLayout;
import ctu.nengoros.util.SL;

/**
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
public class FloatBackend extends OnNewRosMessageSource implements Backend{

	// type of messages we can process here
	public static final String MYTYPE="std_msgs/Float32MultiArray";
	
	private String myTopic;
	
	private std_msgs.Float32MultiArray rosMessage;
	
	final Publisher<std_msgs.Float32MultiArray> publisher;
	final Subscriber<std_msgs.Float32MultiArray> subscriber;
	
	private boolean pub;	// whether to publish or subscribe
	
	private int messageLength;	// number of primitive data fields (Nengo floats)
	
	//private MultiArrayLayout ml;
	
	public FloatBackend(String topic, String type, 
			int[] dimensionSizes, ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException{
		
		if(!type.toLowerCase().equals(MYTYPE.toLowerCase())){
			throw new MessageFormatException("BasicIdentity constructor "+topic,
					"Cannot parse message of this type! expected: "+MYTYPE+
					" and found: "+type);
		}else if(!std_msgs.Float32MultiArray._TYPE.equalsIgnoreCase(MYTYPE)){
			throw new MessageFormatException("BasicIdentity constructor "+topic,
					"Variable MYTYPE has to correspond to type of my message!: "
					+MYTYPE+" != : "+std_msgs.Float32MultiArray._TYPE);
		}
		
		this.messageLength = BackendUtils.countNengoDimension(dimensionSizes);
		this.pub = publish;
		this.myTopic = topic;
		
		if(pub){
			subscriber = null;
			publisher = myRosNode.newPublisher(myTopic, MYTYPE);
			rosMessage = publisher.newMessage();
			// TODO: define multiarrayLayout here
			// so far we are able to send only 1D vectors of data
			/*
			ml = rosMessage.getLayout();
			List<MultiArrayDimension> dims = ml.getDim();
			ml.setDim(dims);
			rosMessage.setLayout(ml);
			*/
			
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
		rosMessage.setData(data);
        publisher.publish(rosMessage);
	}

	public void setReceivedRosMessage(std_msgs.Float32MultiArray mess){
		this.rosMessage=mess;
	}
	
	/**
	 * On each new message from ROS: store it, fire event: Backend:NewMessage.
	 * @param me this class
	 * @return ROS message listener
	 */
	private MessageListener<std_msgs.Float32MultiArray> buildML(final FloatBackend me){
		
		MessageListener<std_msgs.Float32MultiArray> ml = 
				new MessageListener<std_msgs.Float32MultiArray>() {

			@Override
			public void onNewMessage(std_msgs.Float32MultiArray f) {
				me.checkDimensionSizes(f);
				me.setReceivedRosMessage(f);	// save obtained Message
				me.fireOnNewMessage(f);			// inform Nengo about new ROS message
			}
		};
		return ml;
	}
	
	public void checkDimensionSizes(std_msgs.Float32MultiArray mess){
		if(mess.getData().length != this.messageLength)
			System.err.println("FloatBackend: ROS message format exception!! " +
					"Expected message with: "+this.messageLength+
					" and found message with length "+mess.getData().length+
					" message is: "+SL.toStr(mess.getData()));
	}

	/**
	 * Get message data (this receives arrays of floats, so just return the payload).
	 */
	@Override
	public float[] decodeMessage(Message mess) {
		float[] d = ((std_msgs.Float32MultiArray)mess).getData();
		return d;
	}

	@Override
	public int gedNumOfDimensions() { return messageLength; }
	
}
