package ctu.nengoros.comm.nodeFactory.externalNode.javanode;


import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;

import ctu.nengoros.comm.nodeFactory.externalNode.ExternalNodeContainer;


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
	//public void reset();

	/**
	 * NodeMain is interface for a ROS communication-enabled class
	 * @return node which belongs to this container
	 */
	public NodeMain getNode();
	
	/**
	 * Adds a node to this container
	 * @param myNode
	 */
	public void setNodeMain(NodeMain myNode);

	/**
	 * Configuration which launches the node (name, namespace..)
	 * @return NodeConfituration of this node
	 */
	public NodeConfiguration getConfiguration();

}
