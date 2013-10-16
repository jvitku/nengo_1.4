package ctu.nengoros.comm.rosBackend.backend.impl.turtle;

import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSource;
import ctu.nengoros.exceptions.MessageFormatException;
//import std_msgs.MultiArrayLayout;

/**
 * 
 * Should be able to handle messages about position of turtle in turtlesim.  
 * 
 * From rosjava/rosjava_messages:
 * 
package turtlesim;

public interface Pose extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlesim/Pose";
  static final java.lang.String _DEFINITION = "float32 x\nfloat32 y\nfloat32 theta\n\nfloat32 linear_Pose\nfloat32 angular_Pose";
  float getX();
  void setX(float value);
  float getY();
  void setY(float value);
  float getTheta();
  void setTheta(float value);
  float getLinearPose();
  void setLinearPose(float value);
  float getAngularPose();
  void setAngularPose(float value);
}

 * 
 * 
 * @author Jaroslav Vitku
 *
 */
public class PoseBackend extends OnNewRosMessageSource implements Backend{

	// type of messages we can process here
	public static final String MYTYPE="turtlesim/Pose";
	
	private String myTopic;
	
	private final int messageLength = 5;	// 5 floats: x,y,theta, linear,angular (speed I guess)
	private turtlesim.Pose rosMessage;
	
	final Publisher<turtlesim.Pose> publisher;
	final Subscriber<turtlesim.Pose> subscriber;
	
	private boolean pub;	// whether to publish or subscribe
	
	private final String me = "PoseBackend: ";
	
	public PoseBackend(String topic, String type, 
			ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException{
		
		if(!type.toLowerCase().equals(MYTYPE.toLowerCase())){
			throw new MessageFormatException(me+"constructor "+topic,
					"Cannot parse message of this type! expected: "+MYTYPE+
					" and found: "+type);
		}else if(!turtlesim.Pose._TYPE.equalsIgnoreCase(MYTYPE)){
			throw new MessageFormatException(me+"constructor "+topic,
					"Variable MYTYPE has to correspond to type of my message!: "
					+MYTYPE+" != : "+turtlesim.Pose._TYPE);
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
		rosMessage.setX(data[0]);
		rosMessage.setY(data[1]);
		rosMessage.setTheta(data[2]);
		rosMessage.setLinearVelocity(data[3]);
		rosMessage.setAngularVelocity(data[4]);
        publisher.publish(rosMessage);
	}

	public void setReceivedRosMessage(turtlesim.Pose mess){
		this.rosMessage=mess;
	}
	
	/**
	 * On each new message from ROS: store it, fire event: Backend:NewMessage.
	 * @param me this class
	 * @return ROS message listener
	 */
	private MessageListener<turtlesim.Pose> buildML(final PoseBackend thisone){
		
		MessageListener<turtlesim.Pose> ml = 
				new MessageListener<turtlesim.Pose>() {

			@Override
			public void onNewMessage(turtlesim.Pose f) {
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
		data[0] = ((turtlesim.Pose)mess).getX();
		data[1] = ((turtlesim.Pose)mess).getY();
		data[2] = ((turtlesim.Pose)mess).getTheta();
		data[3] = ((turtlesim.Pose)mess).getLinearVelocity();
		data[4] = ((turtlesim.Pose)mess).getAngularVelocity();
		return data;
	}
	

	@Override
	public int gedNumOfDimensions() { return messageLength; }
	
}
