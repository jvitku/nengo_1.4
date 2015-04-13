package ctu.nengorosHeadless.network.modules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.network.node.synchedStart.StartupManager;
import ctu.nengoros.network.node.synchedStart.impl.BasicStartupManager;
import ctu.nengoros.network.node.synchedStart.impl.SyncedUnit;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;
import ctu.nengorosHeadless.rosBackend.encoders.Encoder;

public class NeuralModule extends SyncedUnit implements HeadlessNode{

	// map of origins used (used by Decoders)
	protected Map<String, Orig> myOrigins;			
	protected LinkedList <Orig> orderedOrigins;		

	// map of terminations used (are registered by Encoders)
	protected Map<String, Term> myTerminations;	
	protected LinkedList <Term> orderedTerminations;

	// run all these Encoders after Terminations
	protected Map<String, Encoder> myEncoders;		
	protected LinkedList<Encoder> orderedEncoders;	

	protected float myTime;
	protected ModemContainer mc;
	private volatile boolean isStarted = false;
	public static final boolean DEF_RESETNODES = true;
	private boolean shouldResetRosNodes = DEF_RESETNODES;
	
	protected StartupManager startup = new BasicStartupManager(this);
	
	public NeuralModule(String name, NodeGroup group, boolean synchronous) throws ConnectionException, StartupDelayException{

		super(synchronous,name);	// choose whether to be synchronous or not
		this.init(name, group);
	}

	protected void init(String name, NodeGroup group) throws ConnectionException, StartupDelayException{

		this.myTime = 0;
		// group not running already? start it 
		if(! group.isRunning())
			group.startGroup();

		// try to obtain the modem (for ROS communication)
		ModemContainer modContainer = group.getModem();

		if(modContainer == null){
			String mess = super.getFullName()+" modem probably not initialized! NeuralModule not ready!";
			System.err.println(mess);
			this.setReady(false); // stop the simulation..
			throw new ConnectionException(mess);
		}
		this.mc = modContainer;

		this.setReady(true);
		
		this.myOrigins = new HashMap<String, Orig>(5);
		this.myTerminations = new HashMap<String, Term>(5);
		
		this.myEncoders = new HashMap<String,Encoder>(5);

		this.orderedOrigins = new LinkedList <Orig> ();
		this.orderedTerminations = new LinkedList <Term> ();
		this.orderedEncoders = new LinkedList<Encoder>();

		startup.addChild(mc.getStartupManager());
		mc.getModem().getStartupManager().awaitStarted(); 
		this.isStarted = true;
	}
	

	@Override
	public void run(float startTime, float endTime) throws SimulationException {
		myTime = endTime;

		this.runAllTerminations(startTime, endTime);	// run all terminations to collect input values

		super.discardChildsReady(); 	// wait for all registered synchronous decoders to receive message
		
		this.runAllEncoders(startTime, endTime);	// encode data on registered Terminations and send to ROS
	}

	private void runAllTerminations(float startTime, float endTime) throws SimulationException{
		Term t;
		for(int i=0; i<orderedTerminations.size(); i++){
			t=orderedTerminations.get(i);
			if(t instanceof Term)
				((Term)t).run(startTime, endTime);
			else{
				throw new SimulationException(super.getFullName()+" only BasicTerminations are supporeted here!");
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
	public Orig getOrigin(String name) throws StructuralException {
		if ( !myOrigins.containsKey(name) ) {
			throw new StructuralException("There is no Origin named " + name);
		}
		return myOrigins.get(name);
	}

	@Override
	public Term getTermination(String name) throws StructuralException {
		if ( !myTerminations.containsKey(name) ) {
			throw new StructuralException("There is no Termination named " + name);
		}
		return myTerminations.get(name);
	}

	@Override
	public void notifyAboutDeletion() {
		System.out.print(super.getFullName()+" OK, I am being deleted, will close ROS componnets ");
		mc.stop(); 
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
	public StartupManager getStartupManager() { return this.startup; }

	@Override
	public boolean isStarted() { return this.isStarted; }

	@Override
	public void createDecoder(String topicName, String dataType, int dimensionSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createEncoder(String topicName, String dataType, int dimensionSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createConfigEncoder(String topicName, String dataType, float defValue) {
		// TODO Auto-generated method stub
		
	}

}
