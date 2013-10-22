package ctu.nengoros.comm.rosBackend.backend.impl;

import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.OnNewRosMessageSource;
import ctu.nengoros.comm.rosBackend.transformations.BooleanTransform;
import ctu.nengoros.comm.rosBackend.transformations.impl.BooleanSimple;
import ctu.nengoros.exceptions.MessageFormatException;

public class BooleanBackend extends OnNewRosMessageSource implements Backend{

	// type of messages we can process here
	public static final String MYTYPE="std_msgs/Bool";

	private String myTopic;

	private std_msgs.Bool rosMessage;

	final Publisher<std_msgs.Bool> publisher;
	final Subscriber<std_msgs.Bool> subscriber;

	private boolean pub;	// whether to publish or subscribe

	private int messageLength;	// number of primitive data fields (Nengo floats)

	private BooleanTransform btr;

	//private MultiArrayLayout ml;

	public BooleanBackend(String topic, String type, 
			int[] dimensionSizes, ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException{

		if(!type.toLowerCase().equals(MYTYPE.toLowerCase())){
			throw new MessageFormatException("BasicIdentity constructor "+topic,
					"Cannot parse message of this type! expected: "+MYTYPE+
					" and found: "+type);
		}else if(!std_msgs.Bool._TYPE.equalsIgnoreCase(MYTYPE)){
			throw new MessageFormatException("BasicIdentity constructor "+topic,
					"Variable MYTYPE has to correspond to type of my message!: "
							+MYTYPE+" != : "+std_msgs.Bool._TYPE);
		}

		// I did not find BoolMultiArray, just a single Bool in the std_msgs
		this.messageLength = 1;
		this.pub = publish;
		this.myTopic = topic;

		this.btr = new BooleanSimple();

		// This backend can be used either for publishing or listening to incoming messages
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
	 * @throws MessageFormatException 
	 */
	@Override
	public void publish(float[] data) {
		if(!pub){
			System.err.println("I am set only to subscribe!");
			return;
		}
		if(data.length > 1){
			try {
				throw new MessageFormatException("BooleanBackend: BooleanSimple transformation",
						"I received array of floats, but I can process only single values; lengths of 1");
			} catch (MessageFormatException e) {
				e.printStackTrace();
			}
		}	
		rosMessage = publisher.newMessage();
		rosMessage.setData(btr.float2bool(data[0]));
		publisher.publish(rosMessage);
	}

	public void setReceivedRosMessage(std_msgs.Bool mess){
		this.rosMessage=mess;
	}

	/**
	 * On each new message from ROS: store it, fire event: Backend:NewMessage.
	 * @param me this class
	 * @return ROS message listener
	 */
	private MessageListener<std_msgs.Bool> buildML(final BooleanBackend me){

		MessageListener<std_msgs.Bool> ml = 
				new MessageListener<std_msgs.Bool>() {

			@Override
			public void onNewMessage(std_msgs.Bool b) {
				me.checkDimensionSizes(b);
				me.setReceivedRosMessage(b);	// save obtained Message
				me.fireOnNewMessage(b);			// inform Nengo about new ROS message
			}
		};
		return ml;
	}

	/**
	 * just a single bool, not an array
	 * @param mess
	 */
	public void checkDimensionSizes(std_msgs.Bool mess){
	}

	/**
	 * Get message data (this receives arrays of floats, so just return the payload).
	 */
	@Override
	public float[] decodeMessage(Message mess) {
		
		boolean b = ((std_msgs.Bool)mess).getData();
		float[] d = new float[1];
		d[0] = btr.bool2float(b);
		return d;
	}

	@Override
	public int gedNumOfDimensions() { return messageLength; }

}
