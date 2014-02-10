package ctu.nengoros.comm.nodeFactory.externalNode;

/**
 * This is container for all nodes that can be ran externally and need
 * to be handled by namespaceSupervisor and NodeFactory.
 * 
 * External nodes are: modems, javanodes, native nodes (C++ e.g...)
 *    
 * @author Jaroslav Vitku
 *
 *
 *
 */
public interface ExternalNodeContainer {
	
	/**
	 * Launch the ROS node.
	 */
	public void start();
	
	/**
	 * Stop the ROS node. 
	 */
	public void stop();
	
	/**
	 * Call reset on the node in container.
	 */
	//public void reset();
		
	/**
	 * Get name which somehow identifies this node.
	 * In the best case return the name of ROS node that 
	 * the node will use. If this information is not available, 
	 * return the name of class, process etc..
	 * This name will be used by namespaceSupervisor
	 *  
	 * @return potential name of the ROS node
	 */
	public String getName();
		
	public boolean isRunning();
	
	/**
	 * whether to print to console
	 * @param use
	 */
	public void useLogging(boolean use);

}
