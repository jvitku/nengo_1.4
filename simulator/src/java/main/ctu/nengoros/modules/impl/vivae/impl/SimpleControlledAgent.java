package ctu.nengoros.modules.impl.vivae.impl;

import ca.nengo.model.Origin;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ctu.nengoros.modules.AsynNeuralModule;
import ctu.nengoros.modules.impl.vivae.VivaeAgent;

/**
 * This is basic Vivae Agent controller. It connects ROS topics 
 * (publisher and subscriber) with the Nengo simulator. 
 * Publishes sensory data and its speed.
 * 
 * @author Jaroslav Vitku
 *
 */
public class SimpleControlledAgent implements VivaeAgent{

	private final AsynNeuralModule parent;
	private final String name; 
	private final int numIns = 2;
	private int numOuts = 0; 

	// these components are connected to the Nengo network
	private final Origin myOrigin;	
	private final Termination myTermination;
	
	/**
	 * Build agent, that is: create connections: ROS->Nengo and Nengo->ROS
	 * @param myParent SmartNeuron that is able to createEncoder/decoder
	 * @param name name of the Agent
	 * @param pub topic where the agent will be publishing
	 * @param sub topic to which agent will be subscribed
	 * @param numSensors length of vector of sensory data provided by the agent
	 * @throws StructuralException if origin or termination (just created) are not found
	 */
	public SimpleControlledAgent(AsynNeuralModule myParent, String name, String pub, String sub, int numSensors) 
			throws StructuralException{
		parent = myParent;
		this.name = name;
		numOuts = numSensors;
		
		// connect ROS with the Nengo network here
		parent.createEncoder(sub, "float", numIns);
		// numSensors is requested from the simulator, actual num of outputs is determined by the Vivae simulator
		parent.createDecoder(pub, "float", numOuts);	
		
		// try to find my Nengo components
		myOrigin = parent.getOrigin(pub);
		myTermination = parent.getTermination(sub);
	}

	@Override
	public String getName() { return name;	}

	@Override
	public Termination getTermination() { return myTermination; }

	@Override
	public Origin getOrigin() {	return myOrigin; }

}
