package ctu.nengoros.comm.nodeFactory.modem;

import org.ros.node.ConnectedNode;

import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.network.node.synchedStart.impl.StartedObject;

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
public interface Modem extends ROSNode, StartedObject{
	
	/**
	 * Get factory for subscribers and publishers
	 * @return connectedNode - ROS factory for the ROS components
	 * @throws StartupDelayException if a modem not started in a given time
	 */
	public ConnectedNode getConnectedNode() throws ConnectionException, StartupDelayException;

}
