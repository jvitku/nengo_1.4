package ctu.nengorosHeadless.simulator;

import ctu.nengorosHeadless.network.connections.InterLayerWeights;

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
}
