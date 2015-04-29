package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.StructuralException;

/**
 * Connects Origins and Terminations by weighted connection.
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Connection{

	/**
	 * Called each simulation step
	 */
	public void transferData();
	
	/**
	 * getWeights and alter them
	 * 
	 * @return 2D array of weights of size [Orig.getSize(), Term.getSize()]
	 */
	public float[][] getWeights();
	
	
	/**
	 * Stack connection matrix into single vector of weights
	 *  
	 * @return vector of connection weights of length NxM (Orig.getSize() x Term.getSize()) 
	 */
	public float[] getVector();

	/**
	 * The opposite of the getVector(), weights are stored in the for of connection matrix.
	 * 
	 * @param weights vector of new weights (length of NxM), the values are cloned
	 * @throws StructuralException if the vector has incorrect length
	 */
	public void setVector(float[] weights) throws StructuralException;
	
}
