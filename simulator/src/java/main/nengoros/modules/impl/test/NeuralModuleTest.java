package nengoros.modules.impl.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import nengoros.modules.AsynNeuralModule;
import nengoros.comm.nodeFactory.modem.ModemContainer;
import nengoros.comm.rosBackend.backend.Backend;
import nengoros.comm.rosBackend.backend.BackendUtils;
import nengoros.comm.rosBackend.decoders.impl.BasicDecoder;
import nengoros.comm.rosBackend.encoders.Encoder;
import nengoros.comm.rosBackend.encoders.impl.BasicEncoder;
import nengoros.dynamics.IdentityLTISystem;
import nengoros.dynamics.NoIntegrator;
import nengoros.exceptions.ConnectionException;
import nengoros.exceptions.MessageFormatException;
import nengoros.exceptions.UnsupportedMessageFormatExc;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.Origin;
import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.util.ScriptGenException;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * testing purposes:
 * 	-create NodeGroup with some ROS nodes
 *  -add modem to the group
 *  -create this neuron with the modem
 *  -add some coders/decoders to this neuron
 * 
 * @author Jaroslav Vitku
 *
 * uses asynchronous communication with ROS backends
 */
public class NeuralModuleTest implements AsynNeuralModule{ 

	public static final String me = "[NeuralModuleTest] ";
	
	private static final long serialVersionUID = -5590968314570316769L;
	float myTime;

	private Properties myProperties;
	
	private Map<String, Origin> myOrigins;			// map of origins used
	private Map<String, Termination> myTerminations;// map of terminations used

	private LinkedList <Origin> orderedOrigins;		// ordered list (.toArray())
	private LinkedList <Termination> orderedTerminations;

	private String myDocumentation;
	
	double t_start,t_end,t;		
	
	String myName;

	Units myU = Units.UNK;
	
	protected int myNumGPU = 0;
	protected int myNumJavaThreads = 1;
	protected boolean myUseGPU = false;
	private SimulationMode myMode;

	private final ModemContainer mc;
	
	/**
	 * Initialize complete smart neuron, that means 
	 * modem and a corresponding ros node. 
	 * Ros node can be aither rosjava node or native C++ node.
	 * 
	 * @param name name of smart neuron
	 */
	public NeuralModuleTest(String name, ModemContainer modContainer){
		this.myProperties = new Properties();
		this.myTime=0;
        this.myName=name;
        this.setMode(SimulationMode.DEFAULT);
        this.t_start=0;
        this.t_end=0;
        this.t=0;
        this.myOrigins = new HashMap<String, Origin>(5);
		this.myTerminations = new HashMap<String, Termination>(5);
		this.orderedOrigins = new LinkedList <Origin> ();
		this.orderedTerminations = new LinkedList <Termination> ();
		
		if(modContainer == null)
			System.err.println(me+"modem probably not initialized!!!! ");
		this.mc = modContainer;
	}
	
	
	/**
	 * This method should add decoder (that is nengo origin to the smart neuron)
	 * Note: units are left default, TODO: add units to make use of other ROS message types
	 * @param topicName name of the origin and ROS topic
	 * @param dimensionSizes dimensions of decoded data 
	 */
	public void createDecoder(String topicName, String dataType, int[] dimensionSizes){
		try {
			Backend ros = BackendUtils.select(topicName, dataType, dimensionSizes, mc.getConnectedNode(), false);
			new BasicDecoder(this, topicName, dataType, dimensionSizes, Units.UNK, mc, ros);
			
		} catch (MessageFormatException e) {
			System.err.println(me+"Given message type had probably a bad format!");
			e.printStackTrace();
		} catch (UnsupportedMessageFormatExc e) {
			System.err.println(me+"This format of ROS message probably not supported so far!");
			e.printStackTrace();
		} catch (StructuralException e) {
			System.err.println(me+"could not add my Origin to smart neuron!!");
			e.printStackTrace();
		} catch (ConnectionException e) {
			System.err.println(me+"my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}
	}
	
	public void createDecoder(String topicName, String dataType, int dimensionSize){
		this.createDecoder(topicName, dataType, new int[]{dimensionSize});
	}


	/**
	 * Decoder with number of primitive data types given by the ROS message type.
	 */
	@Override
	public void createDecoder(String topicName, String dataType) {
		try {
			Backend ros = BackendUtils.select(topicName, dataType, mc.getConnectedNode(), false);
			int dim = ros.gedNumOfDimensions();
			new BasicDecoder(this, topicName, dataType, new int[]{dim}, Units.UNK, mc, ros);
			
		} catch (MessageFormatException e) {
			System.err.println(me+"Given message type had probably a bad format!");
			e.printStackTrace();
		} catch (UnsupportedMessageFormatExc e) {
			System.err.println(me+"This format of ROS message probably not supported so far!");
			e.printStackTrace();
		} catch (StructuralException e) {
			System.err.println(me+"could not add my Origin to smart neuron!!");
			e.printStackTrace();
		} catch (ConnectionException e) {
			System.err.println(me+"my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This method adds encoder (that is termination to the smart neuron)
	 * Note: units are left default, TODO: add units to make use of other ROS message types
	 * @param topicName name of termination and the corresponding ROS topic
	 * @param dimension
	 */
	public void createEncoder(String topicName, String dataType, int[] dimensionSizes){
		
		int dim;
		Backend ros;
		try {
			ros = BackendUtils.select(topicName, dataType, dimensionSizes, mc.getConnectedNode(), true);
			dim = BackendUtils.countNengoDimension(dimensionSizes);
			
			Integrator noInt = new NoIntegrator();					// do not integrate termination values
			IdentityLTISystem noLTI = new IdentityLTISystem(dim); 	// do not use any decay..
			
			new BasicEncoder(this, noLTI, noInt, topicName, dimensionSizes, dataType, Units.UNK, mc, ros);
			
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
			ros = BackendUtils.select(topicName, dataType, /*dimensionSizes,*/ mc.getConnectedNode(), true);
			int dim = ros.gedNumOfDimensions();

			Integrator noInt = new NoIntegrator();			// do not integrate termination values
			IdentityLTISystem noLTI = new IdentityLTISystem(dim); 	// do not use any decay..
			
			new BasicEncoder(this, noLTI, noInt, topicName, new int[]{dim}, dataType, Units.UNK, mc, ros);
			
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
	
	
	
	/**
	 * do not use this, use method createEn/Decoder
	 * 
	 * @param name name of origin, should correspond to published topic
	 * @throws StructuralException 
	 */
	public void createOrigin(String name, Origin o) throws StructuralException{
		
		if(myOrigins.containsKey(name)){
			System.err.println("Origin with the same name already here, ignoring!");
			throw new StructuralException("Origin with this name already connected! "+name);
		}
		myProperties.setProperty("o__"+name, "Origin named: "+name);
		myOrigins.put(name, o);
		orderedOrigins.add(o);
	}
	
	/**
	 * 
	 * @param name termination name, should correspond to subscribed ROS topic
	 * @throws StructuralException 
	 */
	public void createTermination(String name, Termination t) throws StructuralException{
		
		if(myTerminations.containsKey(name)){
			System.err.println("Termination iwth this name already here, ignoring!");
			throw new StructuralException("Termination iwth this name already here, ignoring! " + name);
		}
		myProperties.setProperty("t__"+name, "Termination named: "+name);
		myTerminations.put(name,t);
		orderedTerminations.add(t);
	}
	

	/**
	 * here it is done...
	 * TODO: solve the questions about the (start/stop) time, somehow.. 
	 */
	@Override
	public void run(float startTime, float endTime) throws SimulationException {
		myTime = endTime;
		this.runAllEncoders(startTime, endTime);
		
	}
	
	private void runAllEncoders(float startTime, float endTime) throws SimulationException{
		Termination t;
		for(int i=0; i<orderedTerminations.size(); i++){
			t=orderedTerminations.get(i);
			if(t instanceof Encoder)
				((Encoder)t).run(startTime, endTime);
		}
	}
	
	@Override
	public void reset(boolean randomize) {
	mc.resetModem();	// should call reset() for all nodes in the group (including modem itself)	
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
			return ((BasicEncoder)t).getOutput();
			
		}else if(myOrigins.containsKey(kk)){
			Origin o = myOrigins.get(kk);
			float[] values = ((RealOutput) o.getValues()).getValues();
			result = new TimeSeriesImpl(new float[]{myTime}, new float[][]{values}, 
										Units.uniform(Units.UNK, values.length));
			return result;
		}
		throw new SimulationException("Probeable:getHistory: " +
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
		
	//	this.printOrigins();
		
		if ( !myOrigins.containsKey(name) ) {
			throw new StructuralException("There is no Origin named " + name);
		}
		return myOrigins.get(name);
	}
	
	protected void printOrigins(){
		System.out.print(me+"printing orogins now: ");
		for(int i=0; i<orderedOrigins.size(); i++)
			System.out.print(" "+orderedOrigins.get(i).getName());
		System.out.println("done");
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
	
	public NeuralModuleTest clone(){
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
		System.err.println("TODO: toScript not implemented so far!");
		// TODO Auto-generated method stub
		return null;
	}






}
