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
 * 
 * The velocity - based control of ROS demo turtlesim is outdated, now the geometry/Twist is used.
 * 
 * Should be able to publish messages which control turtle in turtlesim.  
 * 
 * 
 * 
 * From rosjava/rosjava_messages:
 * 
 package geometry_msgs;


   public interface Twist extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "geometry_msgs/Twist";
  static final java.lang.String _DEFINITION = "# This expresses velocity in free space broken into it\'s linear and angular parts. \nVector3  linear\nVector3  angular\n";
  geometry_msgs.Vector3 getLinear();
  void setLinear(geometry_msgs.Vector3 value);
  geometry_msgs.Vector3 getAngular();
  void setAngular(geometry_msgs.Vector3 value);
}

 package geometry_msgs;
 
 public interface Vector3 extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "geometry_msgs/Vector3";
  static final java.lang.String _DEFINITION = "# This represents a vector in free space. \n\nfloat64 x\nfloat64 y\nfloat64 z";
  double getX();
  void setX(double value);
  double getY();
  void setY(double value);
  double getZ();
  void setZ(double value);
}

 
 * 
 * @author Jaroslav Vitku
 *
 */
public class TwistBackend extends OnNewRosMessageSource implements Backend{

	// type of messages we can process here
	public static final String MYTYPE="geometry/Twist";
	
	private String myTopic;
	
	private final int messageLength = 6;	// 2x3 floats: linear and angular velocity in x,y,z
	private geometry_msgs.Twist rosMessage;
	
	final Publisher<geometry_msgs.Twist> publisher;
	final Subscriber<geometry_msgs.Twist> subscriber;
	
	// This is probably not the correct way how to generate Twist message:
	// TODO make this better?
	final Publisher<geometry_msgs.Vector3> dummyPublisherA, dummyPublisherB;
	private geometry_msgs.Vector3 linvel, angularvel;
	
	private boolean pub;	// whether to publish or subscribe
	
	private final String me = "VelocityBackend: ";
	
	public TwistBackend(String topic, String type, 
			ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException{
		
		if(!type.toLowerCase().equals(MYTYPE.toLowerCase())){
			throw new MessageFormatException(me+"constructor "+topic,
					"Cannot parse message of this type! expected: "+MYTYPE+
					" and found: "+type);
		}else if(!turtlesim.Velocity._TYPE.equalsIgnoreCase(MYTYPE)){
			throw new MessageFormatException(me+"constructor "+topic,
					"Variable MYTYPE has to correspond to type of my message!: "
					+MYTYPE+" != : "+geometry_msgs.Twist._TYPE);
		}
		this.pub = publish;
		this.myTopic = topic;
		
		if(pub){
			subscriber = null;
			publisher = myRosNode.newPublisher(myTopic, MYTYPE);
			rosMessage = publisher.newMessage();
			
			dummyPublisherA = myRosNode.newPublisher(myTopic, MYTYPE);
			dummyPublisherB = myRosNode.newPublisher(myTopic, MYTYPE);
			linvel = dummyPublisherA.newMessage();
			angularvel = dummyPublisherB.newMessage();
		}else{
			dummyPublisherA = null;
			dummyPublisherB = null;
					
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
		
		linvel.setX(data[0]);
		linvel.setY(data[1]);
		linvel.setZ(data[2]);
		
		angularvel.setX(data[3]);
		angularvel.setY(data[4]);
		angularvel.setY(data[5]);
		
		rosMessage.setLinear(linvel); 	
		rosMessage.setAngular(angularvel);
		
        publisher.publish(rosMessage);
	}

	public void setReceivedRosMessage(geometry_msgs.Twist mess){
		this.rosMessage=mess;
	}
	
	/**
	 * On each new message from ROS: store it, fire event: Backend:NewMessage.
	 * @param me this class
	 * @return ROS message listener
	 */
	private MessageListener<geometry_msgs.Twist> buildML(final TwistBackend thisone){
		
		MessageListener<geometry_msgs.Twist> ml = 
				new MessageListener<geometry_msgs.Twist>() {

			@Override
			public void onNewMessage(geometry_msgs.Twist f) {
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
		// read linear velocity
		data[0] = (float) (((geometry_msgs.Twist)mess).getLinear()).getX();
		data[1] = (float) (((geometry_msgs.Twist)mess).getLinear()).getY();
		data[2] = (float) (((geometry_msgs.Twist)mess).getLinear()).getZ();
		
		// read angular velocity
		data[3] = (float) (((geometry_msgs.Twist)mess).getAngular()).getX();
		data[4] = (float) (((geometry_msgs.Twist)mess).getAngular()).getY();
		data[5] = (float) (((geometry_msgs.Twist)mess).getAngular()).getZ();
		
		return data;
	}
	

	@Override
	public int gedNumOfDimensions() { return messageLength; }
	
}
