package ctu.nengorosHeadless.simulator;

import ca.nengo.model.StructuralException;

public interface EASimulator extends Simulator{
	
	/**
	 * Returns a 2D array of connection weights that are used in all models in the simulation.
	 * 
	 * Dim1: all weights for the model 0
	 * Dim2: for each model, there is one row of weights
	 * 
	 * @return
	 */
	public float[][] getWeights();
	
	/**
	 * 
	 * @param weights 2D array of connection weights for all models, see the getWeights() method.
	 * 
	 * @throws StructuralException thrown due to incorrect size of array
	 */
	public void setWeights(float[][] weights) throws StructuralException;

	/**
	 * @return an array of size N of fitness values. For each of N models there is one value. 
	 */
	public float[] getFitnessVals();
	
}
