package ctu.nengorosHeadless.network.connections.impl;

import ca.nengo.model.StructuralException;
import ctu.nengorosHeadless.network.connections.Connection;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;

public abstract class BasicConnection implements Connection {

	protected final Orig source;
	protected final Term target;

	
	public BasicConnection(Orig source, Term target){
		this.source = source;
		this.target = target;
	}

	@Override
	public void transferData() {
		float val;
		float [][] weights = this.getWeights();
		
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
	public abstract float[][] getWeights();

	@Override
	public abstract void setWeights(float[][] w) throws StructuralException;

}
