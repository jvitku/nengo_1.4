package ctu.nengoros.modules.impl;

import java.util.HashMap;
import java.util.LinkedList;

import ca.nengo.model.SimulationException;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosBackend.multiTerimnationEncoder.MultiTerminationEncoder;
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
public class MultipleInputAsyncNeuralModule extends AbsNeuralModule{

	private static final long serialVersionUID = 8852344940100722448L;


	//protected Map<String, Termination> myTerminations;	// map of terminations used

	// instead of Terminations, each module contains MultipleTerminationEncoders
	// where each MultipleTerminationEncoder can have one or more terminations
	protected HashMap <String, MultiTerminationEncoder> mTEs;		
	protected LinkedList <MultiTerminationEncoder> orderedMTEs;

	public MultipleInputAsyncNeuralModule(String name, NodeGroup group) {
		super(name, group);

		// these are not used here (TODO: better class hierarchy)
		this.myTerminations = null;
		this.orderedTerminations = null;

		mTEs = new HashMap<String, MultiTerminationEncoder>();
		orderedMTEs = new LinkedList<MultiTerminationEncoder>();
	}


	public MultipleInputAsyncNeuralModule(String name, NodeGroup group, boolean synchronous){
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

		// run all Terminations of this Module
		this.runAllEncoderTerminations(startTime, endTime);

		// collect data on all Terminations and pass data to ROS network  
		this.runCollectingAllTerminationValues(startTime, endTime);
	}


	/**
	 * For each MultiTerminationEncoder, run all its Terminations
	 * @param startTime
	 * @param endTime
	 * @throws SimulationException
	 */
	private void runAllEncoderTerminations(float startTime, float endTime) throws SimulationException{

		for(int i=0; i<orderedMTEs.size(); i++){
			orderedMTEs.get(i).runAllTerminations(startTime, endTime);
		}
	}

	/**
	 * For each MultiTerminationEncoder, collect data on its Terminations
	 * and pass the result ("input value on dendrite")
	 * @param startTime
	 * @param endTime
	 * @throws SimulationException
	 */
	private void runCollectingAllTerminationValues(float startTime, float endTime) throws SimulationException{

		for(int i=0; i<orderedMTEs.size(); i++){
			orderedMTEs.get(i).runCollectDataOnTerminations(startTime, endTime);
		}
	}
}

