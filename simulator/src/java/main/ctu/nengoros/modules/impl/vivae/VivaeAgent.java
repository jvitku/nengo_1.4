package ctu.nengoros.modules.impl.vivae;

import ca.nengo.model.Origin;
import ca.nengo.model.Termination;

/**
 * This is interface for Agent, agent connects own 
 * ROS components with Nengo parent node in the constructor, 
 * so no special method needed here.
 * 
 * @author Jaroslav Vitku
 *
 */
public interface VivaeAgent {

	/**
	 * Each agent should have own unique name
	 * @return
	 */
	public String getName();
	
	/**
	 * get these things in order to be able to connect them in Jython scripts
	 * @return
	 */
	public Termination getTermination();
	
	public Origin getOrigin();
}
