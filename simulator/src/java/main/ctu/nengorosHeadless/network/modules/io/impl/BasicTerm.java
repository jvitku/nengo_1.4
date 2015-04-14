package ctu.nengorosHeadless.network.modules.io.impl;

import java.util.Random;

import ctu.nengoros.network.node.synchedStart.impl.SyncedUnit;
import ctu.nengorosHeadless.network.modules.io.Term;

public abstract class BasicTerm extends SyncedUnit implements Term{

	public static final float DEF_VAL = 0;
	
	private final float defVal;
	
	private final int size;
	private final float[] values;
	
	public BasicTerm(int size, String name){
		super.setFullName(name);
		
		if(size<=0){
			System.err.println("Cannot set zero size of size!");
			size = 1;
		}
		this.defVal = DEF_VAL;
		this.size = size;
		this.values = new float[size];
	}

	public BasicTerm(int size, String name, float defVal){
		super.setFullName(name);
		
		if(size != 1){
			System.err.println("currently, default value is supported only on Terminations of size 1");
		}
		
		if(size<=0){
			System.err.println("Cannot set zero size of size!");
			size = 1;
		}
		this.defVal = defVal;
		this.size = size;
		this.values = new float[size];
	}

	
	@Override
	public int getSize() { return this.size; }

	@Override
	public void reset(boolean randomize) {
		if(randomize){
			Random r = new Random();
			for(int i=0; i<size; i++){
				values[i] = r.nextFloat();
			}
		}else{
			for(int i=0; i<size; i++){
				values[i] = defVal;
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
	
	@Override
	public String getName(){ return super.getFullName(); }
	
	@Override
	public float[] getValues() { return this.values; }
}
