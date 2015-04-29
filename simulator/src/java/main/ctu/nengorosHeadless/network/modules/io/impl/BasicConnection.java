package ctu.nengorosHeadless.network.modules.io.impl;

import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;

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
}
