package ctu.nengorosHeadless.network.modules.io;

import ca.nengo.model.Resettable;

public interface MultiTermination extends Resettable{

	/**
	 * Adds new termination (to be connected to another Origin)
	 * @param weights
	 */
	public void addTermination(float[][] weights);
	
	/**
	 * Connects the owner (parent NeuralModule) to the Origin of another Node.  
	 * @param weights connection weights
	 * @param source Origin of another node
	 */
	public void connectTo(float[][] weights, Origin source);
	
	/**
	 * Called each step, values are read, encoded and sent to the ROS node(s) here.
	 * @param startTime
	 * @param endTime only this time step is used
	 */
	public void run(int startTime, int endTime);
	
}
