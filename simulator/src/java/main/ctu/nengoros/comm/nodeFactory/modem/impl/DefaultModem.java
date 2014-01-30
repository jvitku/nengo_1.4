package ctu.nengoros.comm.nodeFactory.modem.impl;

import org.apache.commons.logging.Log;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;

import ctu.nengoros.comm.nodeFactory.modem.Modem;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.network.node.synchedStart.impl.SyncedStart;

/**
 * <p>This is an implementation of ROS part of Modem.
 * Practically it does nothing, all message listening and publishing is done 
 * in Encoders and Decoders. The task for this one is only to register into the ROS 
 * network and get the ConnectedNode class (factory for everything ROS-related..).</p>
 * 
 * <p>Note: encoders and decoders use ConnectedNode obtained from here for everything.
 * The method {@link #awaitStarted()} is used for waiting for the ROS node (modem) 
 * to start.</p> 
 *  
 * @author Jaroslav Vitku
 */
public class DefaultModem extends SyncedStart implements Modem {

	public static final String DEF_NAME = "Modem";
	public final String me = "["+DEF_NAME+"] ";
	
	private String myName;
	private Log log;
	private ConnectedNode myRosSide;

	public DefaultModem(){ myName = DEF_NAME; }
	public DefaultModem(String name){ myName = name; }

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(myName); }

	@Override
	public void onStart(ConnectedNode connectedNode) { 
		log = connectedNode.getLog();
		myRosSide = connectedNode;
		
		System.out.println(me+" starting..!");
		log.info(me+" starting..!");
		// indicate the modem has successfully started
		this.setStarted();				
	}

	@Override
	public void onShutdown(Node node) {
		System.out.println(me+" shutting down started..!");
		log.info(me+" shutting down started..!");
	}

	@Override
	public void onShutdownComplete(Node node) {
		System.out.println(me+" shutdown complete..!");
		log.info(me+" shutdown complete..!");
	}

	@Override
	public void onError(Node node, Throwable throwable) {
		System.err.println(me+" called on error..!");
		log.info(me+" called on error..!");
	}

	@Override
	public ConnectedNode getConnectedNode() throws ConnectionException, StartupDelayException {
		this.awaitStarted();
		log.info(me+" returning myConnectedNode to someone..");
		return myRosSide;
	}

	@Override
	public String getFullName() { return myName; }
}
