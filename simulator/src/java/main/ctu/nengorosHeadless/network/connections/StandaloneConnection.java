package ctu.nengorosHeadless.network.connections;

import ca.nengo.model.StructuralException;

public interface StandaloneConnection extends Connection{

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
