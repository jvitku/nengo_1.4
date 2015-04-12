package ctu.nengorosHeadless.network.modules.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import ca.nengo.model.Origin;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.impl.BasicTermination;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.encoders.Encoder;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.model.transformMultiTermination.MultiTermination;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.network.node.synchedStart.StartupManager;
import ctu.nengoros.network.node.synchedStart.impl.BasicStartupManager;
import ctu.nengoros.network.node.synchedStart.impl.SyncedUnit;
import ctu.nengorosHeadless.network.modules.HeadlessNode;
import ctu.nengorosHeadless.network.modules.NeuralModule;

public class DefaultNeuralModule extends SyncedUnit implements NeuralModule{

	public static final String me = "[DefaultNeuralModule] ";

	private static final long serialVersionUID = -5590968314570316769L;
	protected float myTime;

	// map of origins used (used by Decoders)
	protected Map<String, Origin> myOrigins;			
	protected LinkedList <Origin> orderedOrigins;		

	// map of terminations used (are registered by Encoders)
	protected Map<String, Termination> myTerminations;	
	protected LinkedList <Termination> orderedTerminations;

	// run all these Encoders after Terminations
	protected Map<String, Encoder> myEncoders;		
	protected LinkedList<Encoder> orderedEncoders;	

	double t_start,t_end,t;		

	protected String myName;

	protected ModemContainer mc;
	private volatile boolean isStarted = false;
	public static final boolean DEF_RESETNODES = true;
	private boolean shouldResetRosNodes = DEF_RESETNODES;
	protected StartupManager startup = new BasicStartupManager(this);
	
	/**
	 * <p>The NeuralModule which features BasicEncoders (with BasicMultiTerminations),
	 * BasicDecoders (which are synchronous by default, that means they wait for
	 * ROS message to be received), which is synchronous by default.</p>
	 * 
	 * <p>The simulator waits for all synchronous components at each sim. step</p>
	 * 
	 * @param name name of the Module
	 * @param group NodeGroup which belongs to this Module (group can have one
	 * or multiple ROS nodes and one Modem for translating communication between
	 * ROS and Nengo)
	 * @throws ConnectionException group of running ROS nodes should already contain
	 * the Modem, if no Modem is found, ROS communication will not be available 
	 * @throws StartupDelayException 
	 * 
	 *  @see ctu.nengoros.comm.nodeFactory.modem.Modem
	 *  @see ctu.nengoros.util.sync.impl.SyncedUnit
	 *  @see ctu.nengoros.comm.rosBackend.decoders.impl.BasicDecoder
	 *  @see ctu.nengoros.comm.rosBackend.encoders.impl.BasicEncoder
	 */
	public DefaultNeuralModule(String name, NodeGroup group) throws ConnectionException,
	StartupDelayException{
		super(name);	// make this unit synchronous by default

		this.init(name, group);
	}

	/**
	 * <p>The NeuralModule which features BasicEncoders (with BasicMultiTerminations),
	 * BasicDecoders (which are synchronous by default, that means they wait for
	 * ROS message to be received), which is synchronous by default.</p>
	 * 
	 * <p>The simulator waits for all synchronous components at each sim. step</p>
	 * 
	 * @param name name of the Module
	 * @param group NodeGroup which belongs to this Module (group can have one
	 * or multiple ROS nodes and one Modem for translating communication between
	 * ROS and Nengo) 
	 * @param synchronous the Nengo simulator waits each time step for all 
	 * synchronous Modules 
	 * @throws ConnectionException group of running ROS nodes should already contain
	 * the Modem, if no Modem is found, ROS communication will not be available  
	 * 
	 *  @see ctu.nengoros.comm.nodeFactory.modem.Modem
	 *  @see ctu.nengoros.util.sync.impl.SyncedUnit
	 *  @see ctu.nengoros.comm.rosBackend.decoders.impl.BasicDecoder
	 *  @see ctu.nengoros.comm.rosBackend.encoders.impl.BasicEncoder
	 */
	public DefaultNeuralModule(String name, NodeGroup group, boolean synchronous)
			throws ConnectionException, StartupDelayException{

		super(synchronous,name);	// choose whether to be synchronous or not

		this.init(name, group);
	}

	protected void init(String name, NodeGroup group) throws ConnectionException, StartupDelayException{

		this.myName=name;

		// group not running already? start it 
		if(! group.isRunning())
			group.startGroup();

		// try to obtain the modem (for ROS communication)
		ModemContainer modContainer = group.getModem();

		if(modContainer == null){
			String mess = me+" modem probably not initialized! NeuralModule not ready!";
			System.err.println(mess);
			this.setReady(false); // stop the simulation..
			throw new ConnectionException(mess);
		}
		this.mc = modContainer;

		this.setReady(true);
		this.myTime=0;

		this.t_start=0;
		this.t_end=0;
		this.t=0;
		
		this.myOrigins = new HashMap<String, Origin>(5);
		this.myTerminations = new HashMap<String, Termination>(5);
		this.myEncoders = new HashMap<String,Encoder>(5);

		this.orderedOrigins = new LinkedList <Origin> ();
		this.orderedTerminations = new LinkedList <Termination> ();
		this.orderedEncoders = new LinkedList<Encoder>();

		startup.addChild(mc.getStartupManager());
		mc.getModem().getStartupManager().awaitStarted(); 
		this.isStarted = true;
	}

	@Override
	public void awaitStarted() throws StartupDelayException { startup.awaitStarted(); }

	@Override
	public void setSynchronous(boolean synchronous) {
		super.setSynchronous(synchronous);
	}
	
	@Override
	public String getName() { return this.myName; }

	@Override
	public void setName(String name){ this.myName = name; }
	
	@Override
	public void run(float startTime, float endTime) throws SimulationException {
		myTime = endTime;

		this.runAllTerminations(startTime, endTime);	// run all terminations to collect input values

		super.discardChildsReady(); 	// wait for all registered synchronous decoders to receive message
		
		this.runAllEncoders(startTime, endTime);	// encode data on registered Terminations and send to ROS

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

		// reset modem (potentially reset/restart ROS nodes) 
		if(this.shouldResetRosNodes)
			mc.reset(randomize);

		for(int i=0; i<this.orderedEncoders.size(); i++)
			this.orderedEncoders.get(i).reset(randomize);

		for(int i=0; i<this.orderedTerminations.size(); i++)
			this.orderedTerminations.get(i).reset(randomize);
	}


	@Override
	public Origin[] getOrigins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Origin getOrigin(String name) throws StructuralException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Termination[] getTerminations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Termination getTermination(String name) throws StructuralException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void notifyAboutDeletion() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StartupManager getStartupManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addOrigin(Origin o) throws StructuralException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTermination(Termination t) throws StructuralException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printTerminationNames() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printOriginNames() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setShouldResetNodes(boolean shouldReset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MultiTermination getMultiTermination(String name)
			throws StructuralException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Termination newTerminationFor(String name)
			throws StructuralException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Termination newTerminationFor(String name, float weight)
			throws StructuralException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Termination newTerminationFor(String name, float[][] weights)
			throws StructuralException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createDecoder(String topicName, String dataType,
			int[] dimensionSizes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createDecoder(String topicName, String dataType,
			int dimensionSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createDecoder(String topicName, String dataType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createEncoder(String topicName, String dataType,
			int[] dimensionSizes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createEncoder(String topicName, String dataType,
			int dimensionSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createEncoder(String topicName, String dataType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createConfigEncoder(String topicName, String dataType,
			float[] defaultValues) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createConfigEncoder(String topicName, String dataType,
			float defaultValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createDecoder(String topicName, String dataType,
			int[] dimensionSizes, boolean synchronous) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createDecoder(String topicName, String dataType,
			int dimensionSize, boolean synchronous) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createDecoder(String topicName, String dataType,
			boolean synchronous) {
		// TODO Auto-generated method stub
		
	}
	

}
