package ctu.nengoros.modules;

import java.util.HashMap;

import ca.nengo.model.Termination;

public interface MultiTerminationNeuralModule extends NeuralModule {
	
	public String addTermination(float[][] weights);

	/**
	 * Adds one new Termination to own MultiTermination, values on this 
	 * Termination will be added to the resulting value.
	 *  
	 * @param weight connection weight of this Termination
	 * @return name of registered Termination
	 */
	public String addTermination(float weight);

	/**
	 * Adds one new Termination with the default weight, probably 1.
	 * 
	 * @return name of the registered Termination
	 */
	public String addTermination();

	public HashMap<String, Termination> getMyTerminations();


}
