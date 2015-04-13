package ctu.nengorosHeadless.network.modules.io;

/**
 * Connects Origins and Terminations by weighted connection.
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Connection {

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
	
}
