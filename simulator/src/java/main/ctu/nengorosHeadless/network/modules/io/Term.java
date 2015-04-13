package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;

public interface Term extends Resettable {

	public void setWeights(float[][] weights);

	public void setValues(float[] values);
	
	public int getSize();
	
	public void run(float startTime, float endTime);
	
	public void reset(boolean randomize);
}
