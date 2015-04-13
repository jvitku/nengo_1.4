package ctu.nengorosHeadless.network.modules.io.impl;

import java.util.Random;

import ctu.nengoros.network.node.synchedStart.impl.SyncedUnit;
import ctu.nengorosHeadless.network.modules.io.Term;

public abstract class BasicTerm extends SyncedUnit implements Term{

	public static final float DEF_VAL = 0;
	
	private final int size;
	private final float[] values;
	
	public BasicTerm(int size, String name){
		super.setFullName(name);
		
		if(size<=0){
			System.err.println("Cannot set zero size of size!");
			size = 1;
		}
		this.size = size;
		this.values = new float[size];
	}

	@Override
	public int getSize() { return this.size; }

	/**
	 * This should encode data, send and clear own input
	 */
	@Override
	public abstract void run(float startTime, float endTime);
	
	@Override
	public void reset(boolean randomize) {
		if(randomize){
			Random r = new Random();
			for(int i=0; i<size; i++){
				values[i] = r.nextFloat();
			}
		}else{
			for(int i=0; i<size; i++){
				values[i] = DEF_VAL;
			}
		}
	}

	@Override
	public void sendValue(float value, int index) {
		if(index >= size || size<0){
			System.err.println("Index out of range!");
			return;
		}
		values[index] += value; 
	}
}
