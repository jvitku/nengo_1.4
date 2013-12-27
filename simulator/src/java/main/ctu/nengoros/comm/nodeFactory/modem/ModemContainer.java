package ctu.nengoros.comm.nodeFactory.modem;

import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;

import ctu.nengoros.comm.nodeFactory.ExternalNodeContainer;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.exceptions.ConnectionException;

public interface ModemContainer extends ExternalNodeContainer{

	/**
	 * This may not be supported so far
	 */
	public void reset();

	/**
	 * Modem is interface for a ROS/Nengo modem
	 * @return modem (ROS node) of this ModemContainer
	 */
	public Modem getModem();
	
	/**
	 * Set modem to this ModemContainer
	 * @param myModem modem to be used
	 */
	public void setModem(Modem myModem);

	/**
	 * The most important method is getConnectedNode(), which 
	 * provides factory for ROS publishers/subscribers, this 
	 * factory is used by coders/decoders to create own origins/
	 * terminations. In order to obtain ConnectedNode, the modem
	 * has to be already launched.
	 * 
	 * @return factory for ROS publishers/subscribers
	 */
	public ConnectedNode getConnectedNode() throws ConnectionException; 
	
	/**
	 * This calls (typical Nengo method) reset on modem in the container.
	 * This method should call myGroup.reset() method, which resets all 
	 * nodes in the group, including this one
	 */
	public void resetModem();
	
	
	/**
	 * In case that someone would like to access to group data (e.g. vivae agents)
	 * @return group that I belong to
	 */
	public NodeGroup getMyGroup();
	
	
	/**
	 * Configuration which launches the node (name, namespace..)
	 * @return node cofiguration of the launched Modem
	 */
	@Deprecated
	public NodeConfiguration getConfiguration();

}