package ctu.nengorosHeadless.simulator.impl;

import ctu.nengorosHeadless.simulator.InterLayerConnections;

public class BasicInterLayerConnections implements InterLayerConnections{

	private IOGroup[] inputs;
	private IOGroup[] outputs;

	private float[][] weights;

	public BasicInterLayerConnections(){
		// TODO online adding of new inputUnits
	}

	@Override
	public IOGroup[] getInputs() { return inputs; }

	@Override
	public int getNoOfInputUnits() { 
		int poc = 0; 
		for(int i=0; i<inputs.length; i++){
			poc+= inputs[i].getNoUnits();
		}
		return poc;
	}

	@Override
	public IOGroup[] getOutputs() { return this.outputs; }

	@Override
	public int getNoOfOutputUnits() {
		int poc = 0; 
		for(int i=0; i<outputs.length; i++){
			poc+= outputs[i].getNoUnits();
		}
		return poc;
	}

	@Override
	public float[][] getWeightMatrix() { return this.weights; }


}







