package ctu.nengorosHeadless.network.modules.io.impl;

import java.util.Random;

import ca.nengo.model.SimulationException;

import ctu.nengoros.network.node.synchedStart.impl.SyncedUnit;
import ctu.nengorosHeadless.network.modules.io.Orig;

public abstract class BasicOrig extends SyncedUnit implements Orig{

	public static final float DEF_VAL = 0;
	public float def_val = DEF_VAL;

	protected final int size;
	protected float[] values;

	public BasicOrig(int size, String name){
		super.setFullName(name);
		
		this.def_val = DEF_VAL;
		
		if(size<=0){
			System.err.println("Incorrect size of Origin");
			size = 1;
		}
		this.size = size;
		this.values = new float[size];
	}

	/**
	 * Should wait for all ROS messages, decode them to vector of floats and store the values.
	 */
	@Override
	public abstract void run(float startTime, float endTime) throws SimulationException;

	@Override
	public float[] getValues() { return this.values; }

	@Override
	public int getSize() { return size; }

	@Override
	public void reset(boolean randomize) {
		if(randomize){
			Random r = new Random();
			for(int i=0; i<size; i++){
				values[i] = r.nextFloat();
			}
		}else{
			for(int i=0; i<size; i++){
				values[i] = def_val;
			}			
		}
	}
	
	@Override
	public String getName(){ return super.getFullName(); }
}
