package ctu.nengorosHeadless.network.connections.impl;

import java.util.ArrayList;

import ca.nengo.model.StructuralException;

import ctu.nengorosHeadless.network.connections.InterLayerWeights;

public class BasicInterLayerWeights implements InterLayerWeights{

	private ArrayList<IOGroup> inputs;
	private ArrayList<IOGroup> outputs;

	private float[][] weights;
	private boolean designFinished;

	public BasicInterLayerWeights(){
		inputs = new ArrayList<IOGroup>();
		outputs = new ArrayList<IOGroup>();
		this.designFinished = false;
	}

	@Override
	public IOGroup[] addConnection(int inputDim, int outputDim) {

		int inputStart= this.getNoOfInputUnits()-1;
		int outputStart = this.getNoOfOutputUnits()-1;
		
		IOGroup input = new IOGroup(inputStart, inputDim, inputs.size());
		IOGroup output = new IOGroup(outputStart, outputDim, outputs.size());

		inputs.add(input);
		outputs.add(output);
		
		return new IOGroup[]{input, output};
	}
	
	@Override
	public ArrayList<IOGroup> getInputs() { return inputs; }

	@Override
	public int getNoOfInputUnits() {
		int poc = 0; 
		for(int i=0; i<inputs.size(); i++){
			poc+= inputs.get(i).getNoUnits();
		}
		return poc;
	}

	@Override
	public ArrayList<IOGroup> getOutputs() { return this.outputs; }

	@Override
	public int getNoOfOutputUnits() {
		int poc = 0; 
		for(int i=0; i<outputs.size(); i++){
			poc+= outputs.get(i).getNoUnits();
		}
		return poc;
	}

	@Override
	public void setWeightsBetween(int inputInd, int outputInd, float[][] weights) throws StructuralException{
		if(!this.designFinished){
			throw new StructuralException("Design is not finished");
		}
		if(inputInd <0|| inputInd>=inputs.size()){
			throw new StructuralException("Input index out of range");
		}
		if(outputInd <0|| outputInd>=outputs.size()){
			throw new StructuralException("Output index out of range");
		}
		if(weights.length != inputs.get(inputInd).getNoUnits()){
			throw new StructuralException("Incorrect input dimension of weight matrix, expected: "
					+inputs.get(inputInd).getNoUnits());
		}
		if(weights[0].length != outputs.get(outputInd).getNoUnits()){
			 throw new StructuralException("Incorrect output dimension of weight matrix, expected: "
					+outputs.get(inputInd).getNoUnits());
		}
		IOGroup input = inputs.get(inputInd);
		IOGroup output = outputs.get(outputInd);

		int inputPos = 0, outputPos = 0;

		// copy the values into sub-matrix of global interlayer weights
		for(int i=input.getStartingIndex(); i<input.getNoUnits(); i++){
			for(int j=output.getStartingIndex(); j<output.getNoUnits(); j++){
				this.weights[i][j] = weights[inputPos][outputPos];
				outputPos++;
			}
			inputPos++;
		}
	}

	@Override
	public float[][] getWeightsBetween(int inputInd, int outputInd) throws StructuralException{
		if(!this.designFinished){
			throw new  StructuralException("Design is not finished");
		}
		if(inputInd <0|| inputInd>=inputs.size()){
			 throw new StructuralException("Input index out of range");
		}
		if(outputInd <0|| outputInd>=outputs.size()){
			 throw new StructuralException("Output index out of range");
		}

		IOGroup input = inputs.get(inputInd);
		IOGroup output = outputs.get(outputInd);
		float[][] w = new float[input.getNoUnits()][output.getNoUnits()];

		int inputPos = 0, outputPos = 0;

		// copy the values from the sub-matrix of global interlayer weights
		for(int i=input.getStartingIndex(); i<input.getNoUnits(); i++){
			for(int j=output.getStartingIndex(); j<output.getNoUnits(); j++){
				w[inputPos][outputPos] = this.weights[i][j];
				outputPos++;
			}
			inputPos++;
		}
		return w;
	}

	@Override
	public float[][] getWeightMatrix() {
		if(!this.designFinished){
			System.err.println("Design is not finished");
			return null;
		}
		return this.weights;
	}

	@Override
	public void designFinished() {
		this.designFinished = true;
		this.weights = new float[this.getNoOfInputUnits()][this.getNoOfOutputUnits()];
	}


}







