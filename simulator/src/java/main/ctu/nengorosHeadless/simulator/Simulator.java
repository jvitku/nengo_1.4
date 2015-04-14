package ctu.nengorosHeadless.simulator;

import ca.nengo.model.Resettable;

public interface Simulator extends Resettable{
	
	public void setDt(float step);
	
	public void simulate(float startTime, float endTime);
	
	public void defineNetwork();

	public void setLogToFile(boolean file);

	public boolean isRunning();
}
