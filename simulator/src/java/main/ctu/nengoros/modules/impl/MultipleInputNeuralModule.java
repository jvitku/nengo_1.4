package ctu.nengoros.modules.impl;

import java.util.HashMap;
import java.util.LinkedList;

import ca.nengo.dynamics.Integrator;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.BackendUtils;
import ctu.nengoros.comm.rosBackend.encoders.MultiTerminationEncoder;
import ctu.nengoros.comm.rosBackend.encoders.impl.BasicEncoder;
import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.dynamics.NoIntegrator;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.exceptions.MessageFormatException;
import ctu.nengoros.exceptions.UnsupportedMessageFormatExc;
import ctu.nengoros.modules.AbsNeuralModule;

/**
 *<p>NeuralModule can have multiple Decoders and Encoders.</p> 
 * 
 * <p>Each Encoder corresponds to one Termination: encodes data received on the Termination 
 * to ROS messages and publishes them over the ROS network on the topic which corresponds
 * to the Termination name.</p>
 * 
 * <p>This NeuralModule has encoder which supports combining values from more than 
 * one Terminations. This enables user to connect multiple Origins to one "Termination" (one Encoder).</p>
 *  
 * <p>One Termination is added to each Encoder by default in the constructor. More weighted Terminations
 * can be simply added later or during the simulation.</p>
 *  
 * <p>The NeuralModule holds list of MultipleTerminationEncoders, where each encoder corresponds
 * to one ROS Publisher which publishes data on the correspondingly named ROS topic. </p>
 * 
 * // TODO support for multi-dimensional terminations  
 *
 * @author Jaroslav Vitku
 *
 */
public class MultipleInputNeuralModule extends AbsNeuralModule{

	private static final long serialVersionUID = 8852344940100722448L;

	// instead of Terminations, each module contains MultipleTerminationEncoders
	// where each MultiTerminationEncoder can have one or more terminations
	protected HashMap <String, MultiTerminationEncoder> mTEs;		
	protected LinkedList <MultiTerminationEncoder> orderedMTEs;

	/**
	 * <p>Instantiates a NeuralModule which can have potentially many Terminations connected
	 * to one Encoder. This enables connecting multiple Origins to "one Termination".</p>
	 * 
	 * <p>Each MultiTermination holds several own Terminations whose value is combined together.</p>
	 * 
	 * @param name name of the module
	 * @param group group into which this module belongs
	 */
	public MultipleInputNeuralModule(String name, NodeGroup group) {
		super(name, group);

		this.init(name);
	}

	public MultipleInputNeuralModule(String name, NodeGroup group, boolean synchronous){
		super(name, group, synchronous);

		this.init(name);
	}

	@Override
	protected void init(String name){
		this.setReady(true);

		// these are not used here (TODO: better class hierarchy)
		this.myTerminations = null;
		this.orderedTerminations = null;

		mTEs = new HashMap<String, MultiTerminationEncoder>();
		orderedMTEs = new LinkedList<MultiTerminationEncoder>();
	}


	/**
	 * First, run all necessary Terminations (which belong to my MultiEncoders), 
	 * then, collect all data on Terminations on Encoders, run them.
	 * 
	 * @param startTime
	 * @param endTime
	 * @throws SimulationException  
	 */
	@Override
	public void run(float startTime, float endTime) throws SimulationException {
		myTime = endTime; 

		// run all MultipleEncoders
		for(int i=0; i<this.orderedMTEs.size(); i++){
			this.orderedMTEs.get(i).run(startTime, endTime);
		}
	}
	
	

	/**
	 * This method adds encoder (that is termination to the neural module)
	 * Note: units are left default, TODO: add units to make use of other ROS message types
	 * @param topicName name of termination and the corresponding ROS topic
	 * @param dimension
	 */
	@Override
	public void createEncoder(String topicName, String dataType, int[] dimensionSizes){
		
		int dim;
		Backend ros;
		try {
			ros = BackendUtils.select(topicName, dataType, dimensionSizes, mc.getConnectedNode(), true);
			dim = BackendUtils.countNengoDimension(dimensionSizes);
			
			Integrator noInt = new NoIntegrator();					// do not integrate termination values
			IdentityLTISystem noLTI = new IdentityLTISystem(dim); 	// do not use any decay..
			
			
			//////////// TODO start here in the morning
			// create new MTE here, add to the list of MTEs, check for dimension sizes, that should be all
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
	
	@Override
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
			
			////////////// TODO
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
	
}

