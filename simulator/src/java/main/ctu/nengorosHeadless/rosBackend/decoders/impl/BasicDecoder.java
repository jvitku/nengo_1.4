package ctu.nengorosHeadless.rosBackend.decoders.impl;

import org.apache.log4j.Logger;
import org.ros.internal.message.Message;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.BackendUtils;

import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.exceptions.MessageFormatException;

import ca.nengo.config.Configuration;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.Origin;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.RealOutputImpl;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.network.node.synchedStart.impl.SyncedUnit;
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
public class BasicDecoder extends SyncedUnit implements Decoder {

	protected static final long serialVersionUID = 1L;

	protected static Logger ourLogger = Logger.getLogger(BasicDecoder.class);

	protected Node myNode;
	protected java.lang.String myName;
	protected int myDimension;
	protected Units myUnits;
	protected InstantaneousOutput myValues;
	
	protected ModemContainer modem;

	protected ConnectedNode myRosNode;	// factory for subscriber
	protected Subscriber<std_msgs.Float32MultiArray> mySubscription; // !!!
	protected MessageListener<std_msgs.Float32MultiArray> myListener;

	// TODO: deal somehow with these start/stop times..
	protected float fcn=0;
	protected float startTime=0;
	private float stopTime = 0;

	public Backend ros;	// TODO: this should have been final.. 

	public BasicDecoder(Node node, String name, String dataType, 
			int[] dimensionSizes, Units units, ModemContainer modem, Backend ros) 
					throws MessageFormatException, StructuralException, StartupDelayException{
		super(name);
		this.init(node, name, dataType, dimensionSizes, units, modem, ros);
	}

	/**
	 * The same, but we can choose whether the Decoder will be synchronous or not
	 */
	public BasicDecoder(Node node, String name, String dataType, 
			int[] dimensionSizes, Units units, ModemContainer modem, Backend ros,boolean synchronous) 
					throws MessageFormatException, StructuralException, StartupDelayException{
		super(synchronous,name);
		this.init(node,name,dataType,dimensionSizes,units,modem,ros);
	}

	private void init(Node node, String name, String dataType, 
			int[] dimensionSizes, Units units, ModemContainer modem, Backend ros) 
					throws MessageFormatException, StructuralException, StartupDelayException{
		myNode = node;
		myName = name;
		myUnits = units;

		// get length of vector for Nengo
		myDimension = BackendUtils.countNengoDimension(dimensionSizes);
		myValues = new RealOutputImpl(new float[myDimension], units, 0);

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
		// Nengo stuff
		((NeuralModule)myNode).addOrigin(this);
		super.setReady(true);
	}

	/**
	 * ROS fires onNewRosMessage events, so here is my subscription:
	 * each new ROS message accept, decode and update my values to them.
	 */
	@Override
	public void onNewRosMessage(Message rosMessage) {
		this.setValues(startTime, stopTime, ros.decodeMessage(rosMessage));
		super.setReady(true);	// message received => I am ready
	}

	protected void initConfiguration() {
	}

	/**
	 * @see ca.nengo.config.Configurable#getConfiguration()
	 */
	public Configuration getConfiguration() {
		return null;
	}

	/**
	 * --This method should not be called from the outside--
	 * 	setValues is called by my own Event listener
	 * 
	 * This method is normally called by the Node that contains this Origin, to set the input that is
	 * read by other nodes from getValues(). If the Noise model has been set, noise is applied to the
	 * given values.
	 *
	 * @param startTime Start time of step for which outputs are being defined
	 * @param endTime End time of step for which outputs are being defined
	 * @param values Values underlying RealOutput that is to be output by this Origin in subsequent
	 * 		calls to getValues()
	 */
	public void setValues(float startTime, float endTime, float[] values) {
		assert values.length == myDimension;
		/*
		System.out.println("BasicDecoder, setting these vals: "
					+startTime+" "+endTime+ " value: " +values[0]);
		 */	
		float[] v = values;
		myValues = new RealOutputImpl(v, myUnits, endTime);
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
	public void setValues(InstantaneousOutput values) {
		assert values.getDimension() == myDimension;
		System.out.println("--------------------- ");
		myValues = values;
	}

	/**
	 * @see ca.nengo.model.Origin#getDimensions()
	 */
	public int getDimensions() {
		return myDimension;
	}

	/**
	 * @param dim Origin dimensionality
	 */
	public void setDimensions(int dim) {
		myDimension = dim;
		reset(false);
	}

	/**
	 * @see ca.nengo.model.Origin#getName()
	 */
	public String getName() { return myName; }
	/**
	 * @param name Origin name
	 */
	public void setName(String name) { myName = name; }
	/**
	 * @return Units used by this origin
	 */
	public Units getUnits() { return myUnits; }
	/**
	 * @param units Units used by this origin
	 */
	public void setUnits(Units units) { myUnits = units; }
	/**
	 * @see ca.nengo.model.Origin#getValues()
	 */
	public InstantaneousOutput getValues() throws SimulationException { return myValues; }

	/**
	 * @see ca.nengo.model.Origin#getNode()
	 */
	public Node getNode() { return myNode; }

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {

		myValues = new RealOutputImpl(new float[myDimension], myUnits, 0);
	}

	public void setRequiredOnCPU(boolean val){
	}

	public boolean getRequiredOnCPU(){
		return false;
	}
	
	public BasicDecoder clone(){
		System.err.println("cloning not supported!");
		return null;
	}
	

	/////////// ROS part
/*
	@Override
	public float getStartTime() {
		return this.stopTime;
	}

	@Override
	public float getEndTime() {
		return this.stopTime;
	}
*/
	@Override
	public Origin clone(Node node) throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}
}
