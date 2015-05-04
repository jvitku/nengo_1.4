package ctu.nengorosHeadless.simulator;

import ctu.nengorosHeadless.network.connections.Connection;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;
import ca.nengo.model.Resettable;
import ca.nengo.model.StructuralException;

public interface Simulator extends Resettable{
	
	/**
	 * Call before the run method (waits for all nodes ready, then rests them) 
	 */
	public void prepareForSimulaiton();
	
	/**
	 * Runs the simulation for a given time period
	 * 
	 * @param startTime
	 * @param endTime
	 */
	public void run(float startTime, float endTime);
	
	/**
	 * @param dt step size t+=dt
	 */
	public void setDt(float dt);
	
	/**
	 * Called by the run method, exchanges data between nodes.
	 * 
	 * Pass the data in the simulator by means of weighted connections,
	 * send the messages to ROS nodes, wait for response from all. 
	 */
	public void makeStep();
	
	/**
	 * Defines the network topology and loads nodes, override this for a custom model.
	 */
	public void defineNetwork();
	
	/**
	 * Connects two nodes, registers new weights
	 * @param o Origin ~ source connection
	 * @param t Target ~ target connection (given by the owning node's topic name)
	 * @param interLayerNo index of interlayer where the connection belongs to (may be ignored)
	 * @return newly registered connection
	 * @throws StructuralException if some error occured
	 */
	public Connection connect(Orig o, Term t, int interLayerNo) throws StructuralException;

	/**
	 * TODO
	 * @param file
	 */
	public void setLogToFile(boolean file);

	public boolean isRunning();
	
	/**
	 * Call this after the simulation, kills all nodes, shuts down the core.
	 */
	public void cleanup();
	
	/**
	 * The network already defined?
	 * @return
	 */
	public boolean networkDefined();

	void makeFullConnections(int interLayerNo) throws StructuralException;
}
