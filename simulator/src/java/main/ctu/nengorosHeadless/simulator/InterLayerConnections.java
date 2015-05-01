package ctu.nengorosHeadless.simulator;

import ctu.nengorosHeadless.simulator.impl.IOGroup;

public interface InterLayerConnections {
	
	public IOGroup[] getInputs();
	
	public int getNoOfInputUnits();
	
	public IOGroup[] getOutputs();
	
	public int getNoOfOutputUnits();
	
	
	/**
	 * Size of getNoOfInputUnits() * getNoOfOutputUnits()
	 * 
	 * @return weight matrix representing full interlayer connections 
	 */
	public float[][] getWeightMatrix();
	
}
