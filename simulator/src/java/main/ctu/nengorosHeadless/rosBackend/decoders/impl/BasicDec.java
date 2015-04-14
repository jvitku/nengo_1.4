package ctu.nengorosHeadless.rosBackend.decoders.impl;

import org.apache.log4j.Logger;
import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;

import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.exceptions.MessageFormatException;

import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.impl.BasicOrig;
import ctu.nengorosHeadless.rosBackend.decoders.Decoder;

/**
 * Modification of the BasicDecoder for the headless version of NengoROS. 
 * It does convert the messages into vectors of floats, but does not use/implement
 * the Nengo Origin. 
 * TODO: This class uses Rescaler, which should be able to convert many of data types
 * by rescaling them onto floats. 
 * 
 * @author Jaroslav Vitku
 */
public class BasicDec extends BasicOrig implements Decoder {

	protected static final long serialVersionUID = 1L;

	protected static Logger ourLogger = Logger.getLogger(BasicDec.class);

	protected NeuralModule myNode;
	protected java.lang.String myName;
	protected int myDimension;
	
	protected ModemContainer modem;

	protected ConnectedNode myRosNode;	// factory for subscriber
	protected Subscriber<std_msgs.Float32MultiArray> mySubscription; // !!!
	protected MessageListener<std_msgs.Float32MultiArray> myListener;

	private float myTime = 0;

	public Backend ros;
	
	protected float[] myValues;

	public BasicDec(NeuralModule node, String name, String dataType, int size, ModemContainer modem, Backend ros) 
					throws MessageFormatException, StructuralException, StartupDelayException{
		
		super(size, name);

		this.init(node, name, dataType, size, modem, ros);
	}

	/**
	 * The same, but we can choose whether the Decoder will be synchronous or not
	 */
	public BasicDec(NeuralModule node, String name, String dataType, int size, ModemContainer modem, Backend ros,boolean synchronous) 
					throws MessageFormatException, StructuralException, StartupDelayException{
		super(size, name);
		super.setSynchronous(synchronous);
		
		this.init(node,name,dataType,size,modem,ros);
	}

	private void init(NeuralModule node, String name, String dataType, int size, ModemContainer modem, Backend ros) 
					throws MessageFormatException, StructuralException, StartupDelayException{
		myNode = node;
		myName = name;
		myDimension = size;
		
		myValues = new float[myDimension];

		//get modem and subscribe for events denoting the incoming ROS messages 
		this.modem = modem;		
		if(modem == null)
			System.err.println(myName+" error: modem not inited or set..");
		// here it can wait until ROS node is ready
		try {
			myRosNode = modem.getConnectedNode();
		} catch (ConnectionException e) {
			System.err.println("BasicDecoder: my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}
		// ROS stuff - subscribe to new ROS messages
		this.ros = ros;
		this.ros.addEventListener(this);
		
		((NeuralModule)myNode).addOrigin(this);
		super.setReady(true);
	}

	/**
	 * ROS fires onNewRosMessage events, so here is my subscription:
	 * each new ROS message accept, decode and update my values to them.
	 */
	@Override
	public void onNewRosMessage(Message rosMessage) {
		this.setValues(ros.decodeMessage(rosMessage));
		super.setReady(true);	// message received => I am ready
	}

	/**
	 * --This method should not be called from the outside--
	 * 	setValues is called by my own Event listener
	 * 
	 * This method is normally called by the Node that contains this Origin, to set the input that is
	 * read by other nodes from getValues(). If the Noise model has been set, noise is applied to the
	 * given values.
	 *
	 * @param endTime End time of step for which outputs are being defined
	 * @param values Values underlying RealOutput that is to be output by this Origin in subsequent
	 * 		calls to getValues()
	 */
	public void setValues(float endTime, float[] values) {
		assert values.length == myDimension;
		/*
		System.out.println("BasicDecoder, setting these vals: "
					+startTime+" "+endTime+ " value: " +values[0]);
		 */	
		//float[] v = values;
		//myValues = new RealOutputImpl(v, myUnits, endTime);
		myValues = values.clone();
	}

	/**
	 * --This method should not be called from the outside--
	 * 	setValues is called by my own Event listener
	 * 
	 * This method is normally called by the Node that contains this Origin, to set the input that is
	 * read by other nodes from getValues(). No noise is applied to the given values.
	 *
	 * @param values Values to be output by this Origin in subsequent calls to getValues()
	 */
	public void setValues(float[] values) {
		assert values.length == myDimension;
		System.out.println("--------------------- ");
		myValues = values.clone();
	}

	public int getDimensions() { return myDimension; }

	@Override
	public String getName() { return myName; }
	
	/**
	 * @see ca.nengo.model.Origin#getNode()
	 */
	public NeuralModule getNode() { return myNode; }

	public void reset(boolean randomize) {
		myValues = new float[myDimension];
	}


	/////////// ROS part

	@Override
	public float getEndTime() {
		return this.myTime;
	}

}
