package nengoros.comm.rosBackend.backend.impl.turtle;

import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import nengoros.comm.rosBackend.backend.Backend;
import nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSource;
//import std_msgs.MultiArrayLayout;
import nengoros.exceptions.MessageFormatException;

/**
 * 
 * Should be able to publish messages which control turtle in turtlesim.  
 * 
 * 
 * 
 * From rosjava/rosjava_messages:
 * 
    package turtlesim;

	public interface Velocity extends org.ros.internal.message.Message {
  		static final java.lang.String _TYPE = "turtlesim/Velocity";
  		static final java.lang.String _DEFINITION = "float32 linear\nfloat32 angular";
  		float getLinear();
  		void setLinear(float value);
  		float getAngular();
  		void setAngular(float value);
	}
 * 
 * 
 * @author Jaroslav Vitku
 *
 */
public class VelocityBackend extends OnNewRosMessageSource implements Backend{

	// type of messages we can process here
	public static final String MYTYPE="turtlesim/Velocity";
	
	private String myTopic;
	
	private final int messageLength = 2;	// 2 floats: linear and angular velocity
	private turtlesim.Velocity rosMessage;
	
	final Publisher<turtlesim.Velocity> publisher;
	final Subscriber<turtlesim.Velocity> subscriber;
	
	private boolean pub;	// whether to publish or subscribe
	
	private final String me = "VelocityBackend: ";
	
	public VelocityBackend(String topic, String type, 
			ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException{
		
		if(!type.toLowerCase().equals(MYTYPE.toLowerCase())){
			throw new MessageFormatException(me+"constructor "+topic,
					"Cannot parse message of this type! expected: "+MYTYPE+
					" and found: "+type);
		}else if(!turtlesim.Velocity._TYPE.equalsIgnoreCase(MYTYPE)){
			throw new MessageFormatException(me+"constructor "+topic,
					"Variable MYTYPE has to correspond to type of my message!: "
					+MYTYPE+" != : "+turtlesim.Velocity._TYPE);
		}
		this.pub = publish;
		this.myTopic = topic;
		
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
			System.err.println(me+"I am set only to subscribe!");
			return;
		}
		if(data.length != messageLength){
			System.err.println(me+"wrong dimension of data: message: "+turtlesim.Pose._TYPE+
					" is composed of "+messageLength+" floats");
			return;
		}
		rosMessage = publisher.newMessage();
		rosMessage.setLinear(data[0]);
		rosMessage.setAngular(data[1]);
        publisher.publish(rosMessage);
	}

	public void setReceivedRosMessage(turtlesim.Velocity mess){
		this.rosMessage=mess;
	}
	
	/**
	 * On each new message from ROS: store it, fire event: Backend:NewMessage.
	 * @param me this class
	 * @return ROS message listener
	 */
	private MessageListener<turtlesim.Velocity> buildML(final VelocityBackend thisone){
		
		MessageListener<turtlesim.Velocity> ml = 
				new MessageListener<turtlesim.Velocity>() {

			@Override
			public void onNewMessage(turtlesim.Velocity f) {
	//			thisone.checkDimensionSizes(f);
				thisone.setReceivedRosMessage(f);		// save obtained Message
				thisone.fireOnNewMessage(f);			// inform Nengo about new ROS message
			}
		};
		return ml;
	}
	
	/**
	 * Get message data (this receives arrays of ints, just cast them to floats).
	 */
	@Override
	public float[] decodeMessage(Message mess) {
		
		float[] data = new float[messageLength]; 
		data[0] = ((turtlesim.Velocity)mess).getLinear();
		data[1] = ((turtlesim.Velocity)mess).getAngular();
		return data;
	}
	

	@Override
	public int gedNumOfDimensions() { return messageLength; }
	
}
