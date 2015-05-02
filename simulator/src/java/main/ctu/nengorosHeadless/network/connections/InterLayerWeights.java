package ctu.nengorosHeadless.network.connections;

import java.util.ArrayList;

import ca.nengo.model.StructuralException;

import ctu.nengorosHeadless.network.connections.impl.IOGroup;

public interface InterLayerWeights {
	
	public ArrayList<IOGroup> getInputs();
	
	public int getNoOfInputUnits();
	
	public ArrayList<IOGroup> getOutputs();
	
	public int getNoOfOutputUnits();
	
	/**
	 * Adds new connection and returns two IOGroups, one for input and one for output.
	 * @param inputDim length of input vector (Origin)
	 * @param outputDim length of output vector (Termination)
	 * @return two IOGroups, one for input one for output
	 */
	public IOGroup[] addConnection(int inputDim, int outputDim);
	
	
	/**
	 * Size of getNoOfInputUnits() * getNoOfOutputUnits()
	 * 
	 * @return weight matrix representing full interlayer connections 
	 */
	public float[][] getWeightMatrix();
	
	public void designFinished();
	
	/**
	 * Reads weights between two IOGroups (Origin & Termination)
	 * 
	 * @param inputInd index of input in the interlayer connection
	 * @param outputInd index of output in the interlayer connection
	 * @return connection weights between two IOGroups
	 * @throws StructuralException if at least one of the indexes of IOGroups is not found 
	 */
	public float[][] getWeightsBetween(int inputInd, int outputInd) throws StructuralException;

	/**
	 * Sets weights between two IOGroups (Origin & Termination)
	 * 
	 * @param inputInd index of input in the interlayer connection
	 * @param outputInd index of output in the interlayer connection
	 * @param weights weight matrix between the two IOGroups that is written into the global weight matrix
	 * @throws StructuralException if at least one of the indexes of IOGroups is not found or the weights matrix has incorrect dims.
	 */
	public void setWeightsBetween(int inputInd, int outputInd, float[][] weights) throws StructuralException;
	
	/**
	 * Get flattened vector of weights of this layer (for use in the simple EA)
	 * @return
	 */
	public float[] getVector();
	
	/**
	 * Set flattened vector of weights for this layer (basic EA)
	 * @param weights
	 */
	public void setVector(float[] weights) throws StructuralException;
	public void setVector(Float[] weights) throws StructuralException;
	
}
