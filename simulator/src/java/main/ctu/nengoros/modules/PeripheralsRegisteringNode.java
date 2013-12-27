package ctu.nengoros.modules;

import ca.nengo.model.Origin;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;

/**
 * This is typically NeuralModule which can create Origin or Termination. This means 
 * to register the component into own list peripherals, so the component is
 * called each simulation step = each calling the run(start,stop).
 *  
 * @author Jaroslav Vitku
 *
 */
public interface PeripheralsRegisteringNode {

	/**
	 * This is used by Decoders to add their origin to the NeuralModule, 
	 * should not be used from any other place.
	 * 
	 * @param topicName name of topic (this will be the same with the origin name)
	 * @param dec decoder which sets values of this origin (Decoders implement Origin)
 	 * @throws StructuralException is thrown if there already is orig. with this name
	 *
	 */
	public void createOrigin(String topicName, Origin o) throws StructuralException;
	
	/**
	 * <p>This is used by Encoders to add their termination to the NeuralModule,
	 * should not be used from any other place. Data from this termination
	 * are read by encoder, encoded into ROS message format and sent to ROS net.</p>
	 * 
	 * @param topicName name of the ROS topic (and Nengo termination)
	 * @param enc encoder which sets values (implements Termination)
	 * @throws StructuralException is thrown if there already is term. with this name
	 */
	public void createTermination(String topicName, Termination t) throws StructuralException;

}
