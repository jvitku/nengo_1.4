package ctu.nengoros.model.transformMultiTermination.impl;

import ctu.nengoros.model.transformMultiTermination.WeightFactory;

public class BasicWeights implements WeightFactory{

	@Override
	public float[][] eye(int size) { return this.eye(size, DEF_W); }

	@Override
	public float[][] eye(int size, float weight) {
		if(size<=0){
			System.err.println("Invalid size of the matrix");
			return null;
		}
		float[][] out = new float[size][size];
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				if(i==j){
					out[i][j] = weight;
				}else{
					out[i][j] = 0;
				}
			}
		}
		return out;
	}

	@Override
	public float[][] zeros(int inputDim, int outputDim) {
		if(inputDim<=0 || outputDim<=0){
			System.err.println("Invalid size of the matrix");
			return null;
		}
		float[][] out = new float[inputDim][outputDim];
		for(int i=0; i<inputDim; i++)
			for(int j=0; j<outputDim; j++)
				out[i][j] = 0;
		return out;
	}

}
