package ctu.nengoros.comm.nodeFactory.javanode;


import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;

import ctu.nengoros.comm.nodeFactory.ExternalNodeContainer;


/**
 * It is a container for NodeMain (java ROS node)
 * 
 * @author Jaroslav Vitku
 *
 */
public interface JavaNodeContainer extends ExternalNodeContainer{

	/**
	 * reset called by Nengo
	 */
	public void reset();

	/**
	 * NodeMain is interface for a ROS communication-enabled class
	 * @return
	 */
	public NodeMain getNode();
	public void setNodeMain(NodeMain myNode);

	/**
	 * Configuration which launches the node (name, namespace..)
	 * @return
	 */
	public NodeConfiguration getConfiguration();

}
