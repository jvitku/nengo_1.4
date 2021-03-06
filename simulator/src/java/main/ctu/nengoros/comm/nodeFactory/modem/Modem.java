package ctu.nengoros.comm.nodeFactory.modem;

import org.ros.node.ConnectedNode;

import ctu.nengoros.exceptions.ConnectionException;

/**
 * modem is also a ROS node
 * 
 * this defines abilities of an arbitrary modem, that is:
 * 
 * 	-initialize
 *  -each Coder/Decoder can obtain factory for subscribing/publishing through
 *  the getConnectedNode() method 
 *  
 * @author Jaroslav Vitku
 *
 */
public interface Modem extends ROSNode{
	
	/**
	 * Get factory for subscribers and publishers
	 * @return
	 */
	public ConnectedNode getConnectedNode() throws ConnectionException;

}
