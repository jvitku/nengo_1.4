package ctu.nengoros.modules.impl;

import java.util.HashMap;
import java.util.LinkedList;

import ca.nengo.model.SimulationException;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosBackend.encoders.MultiTerminationEncoder;
import ctu.nengoros.modules.AbsNeuralModule;

/**
 * Asynchronous neural module with support of multiple Terminations for one input.
 * 
 * Decoders remain the same, the difference is in encoders:
 *  -each encoder can have multiple Terminations, inputs from these terminations are summed
 * 	-if encoder is added, one Termination is created (backwards compatibility)
 * 	-new termination can be added by means of method addNewWeightedTermination(String name, float weight)
 * 
 *  // TODO support for array terminations  
 *
 * @author Jaroslav Vitku
 *
 */
public class MultipleInputNeuralModule extends AbsNeuralModule{

	private static final long serialVersionUID = 8852344940100722448L;


	//protected Map<String, Termination> myTerminations;	// map of terminations used

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
	 * @param startTime
	 * @param endTime
	 * @throws SimulationException  
	 */
	@Override
	public void run(float startTime, float endTime) throws SimulationException {
		myTime = endTime; 
		
		// TODO

	}


}

