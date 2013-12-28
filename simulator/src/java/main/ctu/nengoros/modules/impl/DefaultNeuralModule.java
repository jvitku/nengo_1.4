package ctu.nengoros.modules.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.BackendUtils;
import ctu.nengoros.comm.rosBackend.decoders.Decoder;
import ctu.nengoros.comm.rosBackend.decoders.impl.BasicDecoder;
import ctu.nengoros.comm.rosBackend.encoders.Encoder;
import ctu.nengoros.comm.rosBackend.encoders.impl.BasicEncoder;
//import ctu.nengoros.comm.rosBackend.encoders.Encoder;
//import ctu.nengoros.comm.rosBackend.encoders.impl.BasicEncoder;
import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.dynamics.NoIntegrator;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.exceptions.MessageFormatException;
import ctu.nengoros.exceptions.UnsupportedMessageFormatExc;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.util.sync.impl.SyncedUnit;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.Origin;
import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.model.impl.BasicTermination;
import ca.nengo.util.ScriptGenException;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * Extend this Neuron in order to implement your own functionality, or use DefaultNeuralModule. 
 * The same as AbsAsynNeuralModule, but this supports synchronous communication too.  
 * 
 * This module supports synchronous or asynchronous communication with ROS ndoes.
 * Asynchronous case: what comes onto terminations is sent to ROS node(s). What comes
 * from ROS node(s) is passed to origins (do not care when).
 * 
 * Synchronous case: by default: all origins (Decoders) are supposed to be synchronous:
 * 	-each message sent discards "ready" state of all Decoders
 *  -simulator waits until all Decoders receive something
 * If you need some decoder(s) asynchronous, call method CreateDecoder with synchronous set to false.
 * 
 * @author Jaroslav Vitku
 *
 */
public class DefaultNeuralModule extends SyncedUnit implements NeuralModule{ 

	Integrator noInt = new NoIntegrator();					// do not integrate termination values

	public static final String me = "[DefaultNeuralModule] ";

	private static final long serialVersionUID = -5590968314570316769L;
	protected float myTime;

	protected Properties myProperties;

	// map of origins used (used by Decoders)
	protected Map<String, Origin> myOrigins;			
	protected LinkedList <Origin> orderedOrigins;		

	// map of terminations used (are registered by Encoders)
	protected Map<String, Termination> myTerminations;	
	protected LinkedList <Termination> orderedTerminations;

	// run all these Encoders after Terminations
	protected Map<String, Encoder> myEncoders;		
	protected LinkedList<Encoder> orderedEncoders;	

	protected String myDocumentation;

	double t_start,t_end,t;		

	protected String myName;

	Units myU = Units.UNK;

	protected int myNumGPU = 0;
	protected int myNumJavaThreads = 1;
	protected boolean myUseGPU = false;
	protected SimulationMode myMode;

	protected final ModemContainer mc;


	/**
	 * Initialize complete neural module, that means 
	 * modem and a corresponding ros node. 
	 * Ros node can be another rosjava node or native C++ node.
	 * 
	 * @param name name of neural module
	 */
	public DefaultNeuralModule(String name, NodeGroup group){
		super(name);	// make this unit synchronous by default

		if(! group.isRunning()){
			group.startGroup();
		}

		ModemContainer modContainer = group.getModem();
		if(modContainer == null){
			System.err.println(me+"modem probably not initialized!!!! I am not ready!");
			this.setReady(false); // stop the simulation..
		}
		this.mc = modContainer;
		this.init(name);
	}

	/**
	 * This can be used everywhere, because this can be asynchronous too
	 * @param name
	 * @param group
	 * @param synchronous
	 */
	public DefaultNeuralModule(String name, NodeGroup group, boolean synchronous){
		super(synchronous,name);	// choose whether to be synchronous or not
		ModemContainer modContainer = group.getModem();

		if(modContainer == null){
			System.err.println(me+"modem probably not initialized!!!! I am not ready!");
			this.setReady(false); // stop the simulation..
		}
		this.mc = modContainer;
		this.init(name);
	}

	protected void init(String name){
		this.setReady(true);
		this.myName=name;
		this.myProperties = new Properties();
		this.myTime=0;

		this.setMode(SimulationMode.DEFAULT);
		this.t_start=0;
		this.t_end=0;
		this.t=0;
		this.myOrigins = new HashMap<String, Origin>(5);
		this.myTerminations = new HashMap<String, Termination>(5);
		this.myEncoders = new HashMap<String,Encoder>(5);

		this.orderedOrigins = new LinkedList <Origin> ();
		this.orderedTerminations = new LinkedList <Termination> ();
		this.orderedEncoders = new LinkedList<Encoder>();
	}

	@Override
	public void setSynchronous(boolean synchronous) {
		super.setSynchronous(synchronous);
	}

	/**
	 * This method should add decoder (that is Nengo origin to this NeuralModule). 
	 * Each Decoder is registered as a child of thus SynchedUnit, so if the decoder is set
	 * to be synchronous, the simulator will wait for new values on it each sim. step.
	 * 
	 * Note: units are left default, TODO: add units to make use of other ROS message types
	 * 
	 * @param topicName name of the origin and ROS topic
	 * @param dimensionSizes dimensions of decoded data
	 * @param synchronous whether the decoder will be synchronous (see above) 
	 */
	@Override
	public void createDecoder(String topicName, String dataType, int[] dimensionSizes, boolean synchronous) {

		try {
			Backend ros = BackendUtils.select(topicName, dataType, dimensionSizes, mc.getConnectedNode(), false);
			// make decoder synchronous or not (always ready)
			Decoder d = new BasicDecoder(this, topicName, dataType, dimensionSizes, Units.UNK, mc, ros,synchronous);
			// register as child (will or will not block the simulation if is not set to be synchronous)
			super.addChild(d);

		} catch (MessageFormatException e) {
			System.err.println(me+"Given message type had probably a bad format!");
			e.printStackTrace();
		} catch (UnsupportedMessageFormatExc e) {
			System.err.println(me+"This format of ROS message probably not supported so far!");
			e.printStackTrace();
		} catch (StructuralException e) {
			System.err.println(me+"could not add my Origin to neural module!");
			e.printStackTrace();
		} catch (ConnectionException e) {
			System.err.println(me+"my modem was not connected. Probably ROS communication error!");
			e.printStackTrace();
		}
	}

	@Override
	public void createDecoder(String topicName, String dataType, int dimensionSize, boolean synchronous) {
		this.createDecoder(topicName, dataType, new int[]{dimensionSize}, synchronous);		
	}

	/**
	 * Decoder with number of primitive data types given by the ROS message type.
	 */
	@Override
	public void createDecoder(String topicName, String dataType, boolean synchronous) {
		try {
			Backend ros = BackendUtils.select(topicName, dataType, mc.getConnectedNode(), false);

			int dim = ros.gedNumOfDimensions();
			Decoder d = new BasicDecoder(this, topicName, dataType, new int[]{dim}, Units.UNK, mc, ros, synchronous);
			super.addChild(d);

		} catch (MessageFormatException e) {
			System.err.println(me+"Given message type had probably a bad format!");
			e.printStackTrace();
		} catch (UnsupportedMessageFormatExc e) {
			System.err.println(me+"This format of ROS message probably not supported so far!");
			e.printStackTrace();
		} catch (StructuralException e) {
			System.err.println(me+"could not add my Origin to neural module!");
			e.printStackTrace();
		} catch (ConnectionException e) {
			System.err.println(me+"my modem was not connected. Probably ROS communication error!");
			e.printStackTrace();
		}
	}

	public void createDecoder(String topicName, String dataType, int[] dimensionSizes){
		this.createDecoder(topicName, dataType, dimensionSizes, super.synchronous);
	}

	public void createDecoder(String topicName, String dataType, int dimensionSize){
		this.createDecoder(topicName, dataType, new int[]{dimensionSize}, super.synchronous);
	}

	@Override
	public void createDecoder(String topicName, String dataType) {
		this.createDecoder(topicName, dataType, super.synchronous);
	}


	/**
	 * Check whether an encoder to the same topicName is not already registered here.
	 * @param topicName name of the ROS topic and name of the Encoder (base name for its Terminations). 
	 * @throws StructuralException if a Encoder with given name already registered here
	 */
	private void checkEncoderAvailable(String topicName) throws StructuralException{
		if(this.myEncoders.containsKey(topicName))
			throw new StructuralException(me+"Encoder to the requested topic "+topicName+
					" alredy registered to this NeuralModule!");
	}


	/**
	 * This method adds encoder, that is owner of one or multiple Terminations to this NeuralModule.
	 * 
	 * Note: units are left default, TODO: add units to make use of other ROS message types
	 * 
	 * @param topicName name of termination and the corresponding ROS topic
	 * @param dimension
	 */
	public void createEncoder(String topicName, String dataType, int[] dimensionSizes) {

		int dim;
		Backend ros;
		try {
			this.checkEncoderAvailable(topicName);

			ros = BackendUtils.select(topicName, dataType, dimensionSizes, mc.getConnectedNode(), true);
			dim = BackendUtils.countNengoDimension(dimensionSizes);

			IdentityLTISystem noLTI = new IdentityLTISystem(dim); 	// do not use any decay..

			Encoder enc = new BasicEncoder(this, dimensionSizes, noLTI, noInt, topicName, dataType, Units.UNK, mc, ros);
			this.addEncoder(enc);
			//new BasicEncoder(this, noLTI, noInt, topicName, dimensionSizes, dataType, Units.UNK, mc, ros);

		} catch (MessageFormatException e1) {
			System.err.println(me+"Bad message format.");
			e1.printStackTrace();
		} catch (UnsupportedMessageFormatExc e) {
			System.err.println(me+"Message format is not supported so far");
			e.printStackTrace();
		} catch (StructuralException e) {
			System.err.println(me+"Could not add the corresponding termination to Nengo network");
			e.printStackTrace();
		} catch (ConnectionException e) {
			System.err.println(me+"my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}
	}

	public void createEncoder(String topicName, String dataType, int dimensionSize){
		this.createEncoder(topicName, dataType, new int[]{dimensionSize});
	}


	/**
	 * Create encoder where the dimensionality of message data is determined by data type (e.g. turtlesim/Velocity=2)
	 */
	@Override
	public void createEncoder(String topicName, String dataType) {
		Backend ros;
		try {

			this.checkEncoderAvailable(topicName);

			ros = BackendUtils.select(topicName, dataType, /*dimensionSizes,*/ mc.getConnectedNode(), true);
			int dim = ros.gedNumOfDimensions();

			IdentityLTISystem noLTI = new IdentityLTISystem(dim); 	// do not use any decay..

			Encoder enc = new BasicEncoder(this, noLTI, noInt, topicName, dataType, Units.UNK, mc, ros);
			this.addEncoder(enc);

			//new BasicEncoder(this, noLTI, noInt, topicName, new int[]{dim}, dataType, Units.UNK, mc, ros);

		} catch (MessageFormatException e) {
			System.err.println(me+"Bad message format.");
			e.printStackTrace();
		} catch (UnsupportedMessageFormatExc e) {
			System.err.println(me+"Message format is not supported so far");
			e.printStackTrace();
		} catch (StructuralException e) {
			System.err.println(me+"Could not add the corresponding termination to Nengo network");
			e.printStackTrace();
		} catch (ConnectionException e) {
			System.err.println(me+"my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}
	}


	@Override
	public void run(float startTime, float endTime) throws SimulationException {
		myTime = endTime;

		this.runAllTerminations(startTime, endTime);	// run all terminations to collect input values
		
		this.runAllEncoders(startTime, endTime);	// encode data on registered Terminations and send to ROS

		super.discardChildsReady();// wait for all registered synchronous decoders to receive message
	}

	private void runAllTerminations(float startTime, float endTime) throws SimulationException{
		Termination t;
		for(int i=0; i<orderedTerminations.size(); i++){
			t=orderedTerminations.get(i);
			if(t instanceof BasicTermination)
				((BasicTermination)t).run(startTime, endTime);
			else{
				throw new SimulationException(me+"only BasicTerminations are supporeted here!");
			}
		}
	}

	private void runAllEncoders(float startTime, float endTime) throws SimulationException{
		Encoder e;
		for(int i=0; i<orderedEncoders.size(); i++){
			e=orderedEncoders.get(i);
			if(e instanceof Encoder)
				((Encoder)e).run(startTime, endTime);
		}
	}

	@Override
	public void reset(boolean randomize) {
		mc.resetModem();	// should call reset() for all nodes in the group (including modem itself)

		for(int i=0; i<this.orderedEncoders.size(); i++)
			this.orderedEncoders.get(i).reset(randomize);

		for(int i=0; i<this.orderedTerminations.size(); i++)
			this.orderedTerminations.get(i).reset(randomize);

		// System.out.println("reset");
		// TODO: delete history, kill and restart myNode (modem can stay..)
	}

	@Override
	public void setMode(SimulationMode mode) {
		// TODO support simulation mode switching?
		//System.out.println("Mode is not supported so far, will implement only one modem");
		this.myMode=mode;
	}

	@Override
	public SimulationMode getMode() {
		return this.myMode;
	}

	@Override
	public void addChangeListener(Listener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeChangeListener(Listener listener) {
		// TODO Auto-generated method stub
	}

	/**
	 * this is made for Probeable and enables GUI to add a probe (and collect data)
	 * I made every (RealValued) Termination and Origin probeable
	 */
	@Override
	public TimeSeries getHistory(String key) throws SimulationException {		
		TimeSeries result = null;

		String kk = key.substring(3, key.length());

		if(myTerminations.containsKey(kk)){
			Termination t = myTerminations.get(kk);
			// works only for basic terminations with real valued output (hopefully..)
			return ((BasicTermination)t).getOutput();

		}else if(myOrigins.containsKey(kk)){
			Origin o = myOrigins.get(kk);
			float[] values = ((RealOutput) o.getValues()).getValues();
			result = new TimeSeriesImpl(new float[]{myTime}, new float[][]{values}, 
					Units.uniform(Units.UNK, values.length));
			return result;
		}
		throw new SimulationException("Probeable: getHistory: " +
				"this termination or origin is not known!: "+kk);
	}

	/**
	 * also for Probeable..
	 */
	@Override
	public Properties listStates() {
		return myProperties;
	}

	@Override
	public String getName() {
		return this.myName;
	}

	@Override
	public void setName(String name) throws StructuralException {
		super.setName(name);
		this.myName=name;
	}

	@Override
	public Origin[] getOrigins() {
		if (myOrigins.values().size() == 0) {
			return myOrigins.values().toArray(new Origin[0]);
		}
		return orderedOrigins.toArray(new Origin [0]);
	}

	@Override
	public Origin getOrigin(String name) throws StructuralException {

		if ( !myOrigins.containsKey(name) ) {
			this.printOriginNames();
			throw new StructuralException("There is no Origin named " + name);
		}
		return myOrigins.get(name);
	}

	@Override
	public Termination[] getTerminations() {
		if (myTerminations.values().size() == 0) {
			return myTerminations.values().toArray(new Termination[0]);
		}
		return orderedTerminations.toArray(new Termination[0]);
	}

	@Override
	public Termination getTermination(String name) throws StructuralException {
		if ( !myTerminations.containsKey(name) ) {
			this.printTerminationNames();
			throw new StructuralException("There is no Termination named " + name);
		}
		return myTerminations.get(name);
	}

	@Override
	public String getDocumentation() {
		return this.myDocumentation;
	}

	@Override
	public void setDocumentation(String text) {
		this.myDocumentation=text;		
	}

	public DefaultNeuralModule clone(){
		// TODO
		return null;
	}

	@Override
	public void notifyAboutDeletion() {
		System.out.print(me+"OK, I am being deleted, will close ROS componnets "+getName());
		mc.stop(); // TODO stop my group
	}

	@Override
	public Node[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toScript(HashMap<String, Object> scriptData)
			throws ScriptGenException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addOrigin(Origin o) throws StructuralException {

		String name = o.getName();

		if(myOrigins.containsKey(name)){
			System.err.println("Origin with the same name already here, ignoring!");
			throw new StructuralException("Origin with this name already connected! "+name);
		}
		myProperties.setProperty("o__"+name, "Origin named: "+name);
		myOrigins.put(name, o);
		orderedOrigins.add(o);
	}

	@Override
	public void addTermination(Termination t) throws StructuralException {

		System.out.println(me+"adding termination named "+t.getName());
		String name = t.getName();

		if(myTerminations.containsKey(name)){
			System.err.println("Termination iwth this name already here, ignoring!");
			throw new StructuralException("Termination iwth this name already here, ignoring! " + name);
		}
		myProperties.setProperty("t__"+name, "Termination named: "+name);
		myTerminations.put(name,t);
		orderedTerminations.add(t);

	}

	protected void addEncoder(Encoder e) throws StructuralException {
		
		System.out.println(me+"adding encoder named "+e.getName());
		
		if(this.myEncoders.containsKey(e.getName()))
			throw new StructuralException(me+"Encoder named "+e.getName()+" is already registered here!");
				
		myProperties.setProperty("enc__"+e.getName(), "Encoder named: "+e.getName());
		this.myEncoders.put(e.getName(), e);
		this.orderedEncoders.add(e);
	}

	@Override
	public void printTerminationNames() {

		if(this.orderedTerminations.size() == 0)
			System.out.println(me+" list of Terminations is empty!!");
		for(int i=0; i<this.orderedTerminations.size(); i++){
			System.out.println(me+"term no: "+i+" is named "+this.orderedTerminations.get(i).getName());
		}
	}

	@Override
	public void printOriginNames() {
		if(this.orderedOrigins.size() == 0)
			System.out.println(me+" list of Origins is empty!!");
		for(int i=0; i<this.orderedOrigins.size(); i++){
			System.out.println(me+"Origin no: "+i+" is named "+this.orderedOrigins.get(i).getName());
		}
	}

}
