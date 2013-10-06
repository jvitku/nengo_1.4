package nengoros.comm.nodeFactory.modem;

import nengoros.comm.nodeFactory.ExternalNodeContainer;
import nengoros.comm.nodeFactory.NodeGroup;
import nengoros.exceptions.ConnectionException;

import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;

public interface ModemContainer extends ExternalNodeContainer{

	/**
	 * This may not be supported
	 */
	public void reset();

	/**
	 * Modem is interface for a ROS/Nengo modem
	 * @return
	 */
	public Modem getModem();
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
	 * Add reference to modems group, modem should be able to 
	 * shutdown the group of nodes it belongs into. Shutting 
	 * down the group should be called from within the method stop()
	 * @param g
	 */
	
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
	 * @return
	 */
	@Deprecated
	public NodeConfiguration getConfiguration();

}