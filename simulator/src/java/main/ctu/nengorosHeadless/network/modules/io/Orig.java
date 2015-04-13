package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;

public interface Orig extends Resettable{

	public float[] getValues();

	public int getSize();
	
	public void run(float startTime, float endTime);
	
	public void reset(boolean randomize);
}
