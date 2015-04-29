package ctu.nengorosHeadless.network.modules.io.impl;

import ca.nengo.model.StructuralException;
import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;

/**
 * Basic implementaiton of the Conneciton interface.
 * 
 * @author Jaroslav Vitku
 *
 */
public class BasicConnection implements Connection {

	private final Orig source;
	private final Term target;
	
	private final float[][] weights; 
	
	public BasicConnection(Orig source, Term target){
		this.source = source;
		this.target = target;
		
		weights = new float[source.getSize()][target.getSize()];
	}
	
	@Override
	public void transferData() {
		float val;
		
		for(int i=0; i<target.getSize(); i++){
			val=0;
			
			for(int j=0; j<source.getSize(); j++){
				//System.out.println("transfering by weught "+weights[j][i]+" "+source.getValues()[j]);
				val += source.getValues()[j] * weights[j][i];
			}
			target.sendValue(val, i);
		}
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
}
