package ctu.nengorosHeadless.network.connections.impl;

import ca.nengo.model.StructuralException;
import ctu.nengorosHeadless.network.connections.StandaloneConnection;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;

/**
 * Basic implementaiton of the Conneciton interface.
 * 
 * @author Jaroslav Vitku
 *
 */
public class BasicStandaloneConnection extends BasicConnection implements StandaloneConnection {

	private final float[][] weights; 

	public BasicStandaloneConnection(Orig source, Term target){
		super(source, target);

		weights = new float[source.getSize()][target.getSize()];
	}

	@Override
	public float[][] getWeights() { return this.weights; }

	@Override
	public float[] getVector() {
		float[] out = new float[weights.length*weights[0].length];

		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[0].length; j++){

				out[i*weights.length+j] = weights[i][j];
			}
		}
		return out;
	}

	@Override
	public void setVector(float[] weights) throws StructuralException {
		for(int i=0; i<this.weights.length; i++){
			for(int j=0; j<this.weights[0].length; j++){

				this.weights[i][j] = weights[i*this.weights.length+j];
			}
		}		
	}

	@Override
	public void setWeights(float[][] w) throws StructuralException {
		if(w.length != source.getSize() || w[0].length != target.getSize()){
			throw new StructuralException("Incorrect size of the weight matrix");
		}
		for(int i=0; i<this.weights.length; i++){
			for(int j=0; j<this.weights[0].length; j++){

				this.weights[i][j] = w[i][j];
			}
		}
	}
}
