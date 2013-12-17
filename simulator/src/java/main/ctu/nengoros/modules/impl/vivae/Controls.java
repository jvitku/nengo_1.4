package ctu.nengoros.modules.impl.vivae;

import ca.nengo.model.StructuralException;

/**
 * This handles the Vivae simulation over the ROS network. 
 * The important things are:
 * 	-loadMap
 * 	-start
 * 	-stop
 *  -destroy
 *  
 *  -addAgent	 (adds Origin and Termination to the parent Neuron)
 *  -removeAgent (removes Origins and Terminations)
 *  
 *   @author Jaroslav Vitku
 */  
public interface Controls {


	void start();

	void stop();

	void destroy();

	boolean loadMap(String path);
	
	boolean init();

	// destroy current, init, re-register agents, start
	boolean reset();
	
	boolean setVisible(boolean visible);
	
	// Exception is thrown if agents origin or termination could not be created
	void addAgent(String name) throws StructuralException;
	void addAgent(String name, int numSensors) throws StructuralException;
	void addAgent(String name, int numSensors, double maxDistance, 
			double FrictionSensor) throws StructuralException;
	
	void removeAgent(String name);
	
	

}
