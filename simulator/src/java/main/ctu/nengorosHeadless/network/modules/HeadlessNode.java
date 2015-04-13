package ctu.nengorosHeadless.network.modules;

import ctu.nengorosHeadless.network.modules.ioTmp.Origin;
import ctu.nengorosHeadless.network.modules.ioTmp.Terminaiton;
import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;


/**
 * Headless version of the ca.nengo.model.Node
 * 
 * @author Jaroslav Vitku
 *
 */
public interface HeadlessNode extends Resettable{
	/**
	 * @return Name of Node (must be unique in a Network)
	 */
	public String getName();

	/**
	 * @param name The new name
	 * @throws StructuralException if name already exists?
	 */
	public void setName(String name) throws StructuralException;

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
	 * @return Sets of ouput channels (eg spiking outputs, gap junctional outputs, etc.)
	 */
	public Origin[] getOrigins();

	/**
	 * TODO this is new 
	 * @param o
	 * @throws StructuralException
	 */
	public void addOrigin(Origin o) throws StructuralException;

	
	/**
	 * @param name Name of an Origin on this Node
	 * @return The named Origin if it exists
	 * @throws StructuralException if the named Origin does not exist
	 */
	public Origin getOrigin(String name) throws StructuralException;

	/**
	 * @return Sets of input channels (these have the same dimension as corresponding Origins
	 * 		to which they are connected).
	 */
	public Terminaiton[] getTerminations();

	/**
	 * @param name Name of a Termination onto this Node
	 * @return The named Termination if it exists
	 * @throws StructuralException if the named Termination does not exist
	 */
	public Terminaiton getTermination(String name) throws StructuralException;
	

	/**
	 * This method tells the Node (network) that is being deleted from Nengo, 
	 * Node can inform its children (child Nodes) that are being deleted, 
	 * these nodes can e.g. stop their ROS backends, their ROS nodes etc..	 
	 * 
	 * Note that it is expected that node returns from this method when 
	 * everything that should be done before his deletion is done. 
	 * 
	 * Also, Node does not have to react to this (as in pure Nengo version).
	 * 
	 * ///my @author Jaroslav Vitku
	 */
	public void notifyAboutDeletion();
}
