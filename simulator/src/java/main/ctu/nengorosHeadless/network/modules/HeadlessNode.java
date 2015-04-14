package ctu.nengorosHeadless.network.modules;

import ctu.nengoros.network.node.synchedStart.SyncedUnitInterface;
import ctu.nengoros.network.node.synchedStart.impl.StartedObject;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;
import ctu.nengorosHeadless.rosBackend.encoders.Encoder;
import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;

/**
 * Headless version of the ca.nengo.model.Node
 * 
 * @author Jaroslav Vitku
 *
 */
public interface HeadlessNode extends Resettable, StartedObject, SyncedUnitInterface{

	/**
	 * Runs the Node (including all its components), updating internal state and outputs as needed.
	 * Runs should be short (eg 1ms), because inputs can not be changed during a run, and outputs
	 * will only be communicated to other Nodes after a run.
	 *
	 * @param startTime simulation time at which running starts (s)
	 * @param endTime simulation time at which running ends (s)
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime) throws SimulationException;

	/**
	 * Build the ROS input (decodes incoming ROS messages) into array of floats. Creates and registers own Origin.
	 * 
	 * @param topicName relative name of the ROS topic
	 * @param dataType  type of data in the ROS message, e.g. "float"
	 * @param dimensionSizes size of decoded float array
	 */
	public void createDecoder(String topicName, String dataType, int dimensionSize);
	
	/**
	 * Part of the Termination (creates and registers it).
	 * 
	 * @param topicName
	 * @param dataType
	 * @param dimensionSize
	 */
	public void createEncoder(String topicName, String dataType, int dimensionSize);
	
	/**
	 * The same as encoder, but has size of 1 and features default value, when not connected in the network.
	 *  
	 * @param topicName
	 * @param dataType
	 * @param defValue
	 */
	public void createConfigEncoder(String topicName, String dataType, float defValue);
	
	/**
	 * Used for connecting two NeuralModules.
	 * 
	 * @param name Name of an Origin on this Node
	 * @return The named Origin if it exists
	 * @throws StructuralException if the named Origin does not exist
	 */
	public Orig getOrigin(String name) throws StructuralException;

	/**
	 * Called by the Origin/Decoder
	 * @param o
	 * @throws StructuralException
	 */
	public void addOrigin(Orig o) throws StructuralException;
	
	/**
	 * Used for connecting two NeuralModules.
	 * 
	 * @param name Name of a Termination onto this Node
	 * @return The named Termination if it exists
	 * @throws StructuralException if the named Termination does not exist
	 */
	public Term getTermination(String name) throws StructuralException;
	
	public void addEncoder(Encoder enc) throws StructuralException;
	
	/**
	 * This method tells the Node (network) that is being deleted from Nengo, 
	 * Node can inform its children (child Nodes) that are being deleted, 
	 * these nodes can e.g. stop their ROS backends, their ROS nodes etc..	 
	 * 
	 * Note that it is expected that node returns from this method when 
	 * everything that should be done before his deletion is done. 
	 * 
	 * Also, Node does not have to react to this (as in pure Nengo version).
	 */
	public void notifyAboutDeletion();
}
