package ctu.nengorosHeadless.simulator;

import ca.nengo.model.StructuralException;
import ctu.nengorosHeadless.network.connections.InterLayerWeights;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;

public interface EASimulator extends Simulator{
	
	/**
	 * @return a fitness value of one individual (TODO multithreaded) 
	 */
	public float getFitnessVal();

	
	/**
	 * Returns intelayer connection of a given model. Interlayer connect two layers by full connections.
	 * InterLayerWeights holds a structure with information about IOGroups. 
	 * 
	 * @param no number of interlayer in the model (starting with 0)
	 * @return
	 */
	public InterLayerWeights getInterLayerNo(int no);
	
	
	public void registerOrigin(Orig o, int interLayerNo) throws StructuralException;
	public void registerTermination(Term t, int interLayerNo) throws StructuralException;
	
	/**
	 * Fully connect everything in one interlayer, store the array of connections in the simulator
	 * 
	 * @param interLayerNo
	 */
	public void makeFullConnections(int interLayerNo);

}
