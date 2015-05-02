package ctu.nengorosHeadless.network.connections;

import ca.nengo.model.StructuralException;


/**
 * Connects Origins and Terminations by weighted connection. Can transfer data between them.
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
	 * Sets new weights between Origin and Termination.
	 * 
	 * @param w
	 */
	public void setWeights(float[][] w) throws StructuralException;

}
