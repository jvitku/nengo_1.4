package ctu.nengorosHeadless.network.connections.impl;

import java.util.ArrayList;

import ca.nengo.model.StructuralException;
import ctu.nengorosHeadless.network.connections.Connection;
import ctu.nengorosHeadless.network.connections.InterLayerWeights;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;

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
	public IOGroup addOrigin(Orig o){
		int inputStart= this.getNoOfInputUnits();
		IOGroup input = new IOGroup(inputStart, o.getSize(), inputs.size(), o);

		inputs.add(input);
		return input;
	}
	
	@Override
	public IOGroup addTermination(Term t){
		int outputStart= this.getNoOfOutputUnits();
		IOGroup output = new IOGroup(outputStart, t.getSize(), outputs.size(), t);

		System.out.println("adding termination: "+t.getUniqueName());
		outputs.add(output);
		return output;
	}
	
	@Override
	public IOGroup getOrigin(String uniqueName) throws StructuralException{
		for(int i=0; i<inputs.size(); i++){
			if(inputs.get(i).getUniqueName().equalsIgnoreCase(uniqueName)){
				return inputs.get(i);
			}
		}
		throw new StructuralException("Origin named: "+uniqueName+" not registered!");
	}
	
	@Override
	public IOGroup getTermination(String uniqueName) throws StructuralException{
		for(int i=0; i<outputs.size(); i++){
			if(outputs.get(i).getUniqueName().equalsIgnoreCase(uniqueName)){
				return outputs.get(i);
			}
		}
		throw new StructuralException("Termination named: "+uniqueName+" not registered!");
	}
	
	
	// TODO remove this?
	@Override
	public IOGroup[] addConnection(int inputDim, int outputDim) {

		int inputStart= this.getNoOfInputUnits();
		int outputStart = this.getNoOfOutputUnits();

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
	public void setWeightsBetween(Orig o, Term t, float[][] w) throws StructuralException{
		IOGroup input = this.getOrigin(o.getUniqueName());
		IOGroup output = this.getTermination(t.getUniqueName());
		
		this.setWeightsBetween(input.getMyIndex(), output.getMyIndex(), w);
	}
	
	@Override
	public float[][] getWeightsBetween(Orig o, Term t) throws StructuralException{
		IOGroup input = this.getOrigin(o.getUniqueName());
		IOGroup output = this.getTermination(t.getUniqueName());
		
		return this.getWeightsBetween(input.getMyIndex(), output.getMyIndex());
	}
	

	@Override
	public void setWeightsBetween(int inputInd, int outputInd, float[][] w) throws StructuralException{
		if(!this.designFinished){
			throw new StructuralException("Design is not finished");
		}
		if(inputInd <0|| inputInd>=inputs.size()){
			throw new StructuralException("Input index out of range");
		}
		if(outputInd <0|| outputInd>=outputs.size()){
			throw new StructuralException("Output index out of range");
		}
		if(w.length != inputs.get(inputInd).getNoUnits()){
			throw new StructuralException("Incorrect input dimension of weight matrix, expected: "
					+inputs.get(inputInd).getNoUnits());
		}
		if(w[0].length != outputs.get(outputInd).getNoUnits()){
			throw new StructuralException("Incorrect output dimension of weight matrix, expected: "
					+outputs.get(inputInd).getNoUnits());
		}
		IOGroup input = inputs.get(inputInd);
		IOGroup output = outputs.get(outputInd);

		int inputPos = 0, outputPos = 0;

		// copy the values into sub-matrix of global interlayer weights
		for(int i=input.getStartingIndex(); i<input.getNoUnits()+input.getStartingIndex(); i++){
			outputPos = 0;

			for(int j=output.getStartingIndex(); j<output.getNoUnits()+output.getStartingIndex(); j++){

				/*
				if(w[inputPos][outputPos] != 0){
					System.err.println("setting weight "+inputPos+" "+outputPos+
							" ij "+ i+" "+j+" to "+w[inputPos][outputPos]);
				}*/
				this.weights[i][j] = w[inputPos][outputPos];
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
		for(int i=input.getStartingIndex(); i<input.getNoUnits()+input.getStartingIndex(); i++){
			outputPos = 0;

			for(int j=output.getStartingIndex(); j<output.getNoUnits()+output.getStartingIndex(); j++){
				//System.out.println("indexing the w: "+inputPos+" "+outputPos+" "+i+" "+j);
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
		//System.err.println("weight matrix is: "+weights.length+" "+weights[0].length);
	}

	@Override
	public float[] getVector() {
		float[] out = new float[weights.length * weights[0].length];
		
		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[0].length; j++){
				
				out[i*weights[0].length+j] = weights[i][j];
			}
		}
		return out;
	}

	@Override
	public void setVector(float[] vector) throws StructuralException {

		if(vector.length != weights.length * weights[0].length){
			throw new StructuralException("will not setVector, incorrect length of the vector!"
					+ "expected: "+(weights.length * weights[0].length)+" given: "+vector.length);
		}
		int pos = 0;
		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[0].length; j++){
				
				weights[i][j] = vector[pos++];
			}
		}
	}
	
	@Override
	public void setVector(Float[] vector) throws StructuralException {

		if(vector.length != weights.length * weights[0].length){
			throw new StructuralException("will not setVector, incorrect length of the vector!"
					+ "expected: "+(weights.length * weights[0].length)+" given: "+vector.length);
		}
		int pos = 0;
		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[0].length; j++){
				
				weights[i][j] = vector[pos++];
			}
		}
	}

	@Override
	public Connection[] makeFullConnections() throws StructuralException{
		if(!this.designFinished){
			throw new StructuralException("Design is not finished");
		}
		Connection[] out = new Connection[inputs.size()*outputs.size()];
		Orig o; 
		Term t;
		
		int pos = 0;
		for(int i=0; i<inputs.size(); i++){
			o = (Orig)inputs.get(i).myIO;
			
			for(int j=0; j<outputs.size(); j++){
				t = (Term)outputs.get(j).myIO;
				
				out[pos++] = new ReferencedInterlayerConnection(o,t,this);
			}
		}
		return out;
	}
}

