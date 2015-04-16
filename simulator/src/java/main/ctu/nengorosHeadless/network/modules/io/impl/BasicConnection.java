package ctu.nengorosHeadless.network.modules.io.impl;

import ctu.nengoros.model.transformMultiTermination.impl.BasicWeights;
import ctu.nengoros.util.SL;
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
			
			//System.out.println("transfering this array: "+SL.toStr(source.getValues()));
			//System.out.println("weight matrix is: \n"+BasicWeights.printMatrix(weights));
			
			for(int j=0; j<source.getSize(); j++){
				
				val += source.getValues()[j] * weights[j][i];
			}
			target.sendValue(val, i);
		}
	}

	@Override
	public float[][] getWeights() { return this.weights; }
}
