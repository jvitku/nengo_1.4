package ctu.nengoros.network.transformations;

import ca.nengo.model.Resettable;
import ca.nengo.model.StructuralException;

/**
 * 
 * <p>This Transformation has the purpose to define a weight matrix, which
 * transforms output of Origin to a Termination.</p>
 *  
 * <p>Transformation is a part of Termination. It has the similar purpose as
 * the weight_func in the nef core, but here, the Transformation could change 
 * its weights during the simulation run.</p>   
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Transformation extends Resettable{
	
	/**
	 * Number of input dimensions, this has to be consistent with
	 * the number of dimensions of an {@link ca.nengo.model.Origin} which 
	 * will be connected to this.
	 * @return number of input dimensions
	 */
	public int getInputDimension();
	
	/**
	 * Number of output dimensions, this has to be consistent with 
	 * the number of dimensions of an {@link ca.nengo.model.Termination} which
	 * will be connected to this.
	 * @return number of output dimensions
	 */
	public int getOutputDimension();
	
	/**
	 * This Transformation has the purpose to define weight matrix, which
	 * transforms output of Origin to Termination. These weights can be modulatory,
	 * that is: the weights can change during the simulation.
	 * @return weights of size [{@link #getInputDimension()},{@link #getOutputDimension()}]
	 */
	public float[][] getWeights();

	/**
	 * Change a current value of the weight matrix 
	 * @param weights 2s array of weights between connected Origin and Termination
	 * @throws StructuralException thrown if the dimensions do not match 
	 */
	public void setWeights(float[][] weights) throws StructuralException;
}
