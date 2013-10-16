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
 * Reads RGB (3-channel) color from turtle, data are converted from byte to float and back..
 *  
 * From rosjava/rosjava_messages:
 * 
package turtlesim;

public interface Color extends org.ros.internal.message.Message {
  static final java.lang.String _TYPE = "turtlesim/Color";
  static final java.lang.String _DEFINITION = "uint8 r\nuint8 g\nuint8 b\n";
  byte getR();
  void setR(byte value);
  byte getG();
  void setG(byte value);
  byte getB();
  void setB(byte value);
}
 * 
 * 
 * @author Jaroslav Vitku
 *
 */
public class ColorBackend extends OnNewRosMessageSource implements Backend{

	// type of messages we can process here
	public static final String MYTYPE="turtlesim/Color";
	
	private String myTopic;
	
	private final int messageLength = 3;	// RGB
	private turtlesim.Color rosMessage;
	
	final Publisher<turtlesim.Color> publisher;
	final Subscriber<turtlesim.Color> subscriber;
	
	private boolean pub;	// whether to publish or subscribe
	
	private final String me = "ColorBackend: ";
	
	public ColorBackend(String topic, String type, 
			ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException{
		
		if(!type.toLowerCase().equals(MYTYPE.toLowerCase())){
			throw new MessageFormatException(me+"constructor "+topic,
					"Cannot parse message of this type! expected: "+MYTYPE+
					" and found: "+type);
		}else if(!turtlesim.Color._TYPE.equalsIgnoreCase(MYTYPE)){
			throw new MessageFormatException(me+"constructor "+topic,
					"Variable MYTYPE has to correspond to type of my message!: "
					+MYTYPE+" != : "+turtlesim.Color._TYPE);
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
			System.err.println(me+"wrong dimension of data: message: "+turtlesim.Color._TYPE+
					" is comColord of "+messageLength+" floats");
			return;
		}
		rosMessage = publisher.newMessage();
		rosMessage.setR((byte)data[0]);
		rosMessage.setG((byte)data[1]);
		rosMessage.setB((byte)data[1]);
        publisher.publish(rosMessage);
	}

	public void setReceivedRosMessage(turtlesim.Color mess){
		this.rosMessage=mess;
	}
	
	/**
	 * On each new message from ROS: store it, fire event: Backend:NewMessage.
	 * @param me this class
	 * @return ROS message listener
	 */
	private MessageListener<turtlesim.Color> buildML(final ColorBackend thisone){
		
		MessageListener<turtlesim.Color> ml = 
				new MessageListener<turtlesim.Color>() {

			@Override
			public void onNewMessage(turtlesim.Color f) {
	//			thisone.checkDimensionSizes(f);
				thisone.setReceivedRosMessage(f);		// save obtained Message
				thisone.fireOnNewMessage(f);			// inform Nengo about new ROS message
			}
		};
		return ml;
	}
	
	/**
	 * Get message data (this receives 3 bytes, just cast them to floats).
	 */
	@Override
	public float[] decodeMessage(Message mess) {
		
		float[] data = new float[messageLength]; 
		data[0] = (float)((turtlesim.Color)mess).getR();
		data[1] = (float)((turtlesim.Color)mess).getG();
		data[2] = (float)((turtlesim.Color)mess).getB();
		return data;
	}
	

	@Override
	public int gedNumOfDimensions() { return messageLength; }
	
}
