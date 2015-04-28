package ctu.nengorosHeadless.simulator;

import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;
import ca.nengo.model.Resettable;

public interface Simulator extends Resettable{
	
	public void setDt(float step);
	
	/**
	 * Call before run method (waits for all nodes ready, then rests them) 
	 */
	public void prepareForSimulaiton();
	
	public void run(float startTime, float endTime);
	
	/**
	 * Called by the run method
	 */
	public void makeStep();
	
	public void defineNetwork();
	
	public Connection connect(Orig o, Term t);

	public void setLogToFile(boolean file);

	public boolean isRunning();
	
	public void cleanup();
}
