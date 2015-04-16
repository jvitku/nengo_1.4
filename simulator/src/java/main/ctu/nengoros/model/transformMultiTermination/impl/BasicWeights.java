package ctu.nengoros.model.transformMultiTermination.impl;

import ctu.nengoros.model.transformMultiTermination.WeightFactory;
import ctu.nengoros.util.SL;

/**
 * Default implementation of matrix Matlab-style helper.
 * 
 * @author Jaroslav Vitku
 *
 */
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
	
	/**
	 * Tries to make identity (scale) matrix, starts from indexes 0,0 and places ones onto the diagonal.
	 * 
	 * 2D matrix does not have to be square 
	 * @param w matrix to be changed for 
	 */
	public static void pseudoEye(float[][] w, float diagValue){
		if(w==null)
			return;
		for(int i=0; i<w.length; i++){
			for(int j=0; j<w[0].length; j++){
				if(i==j){
					w[i][j] = diagValue;
				}else{
					w[i][j] = 0;
				}
			}
		}
	}
	
	public static String printMatrix(float[][] w){
		String out = "";
		for(int i=0; i<w.length; i++){
			out+=SL.toStr(w[i])+"\n";
		}
		return out;
	}

}
