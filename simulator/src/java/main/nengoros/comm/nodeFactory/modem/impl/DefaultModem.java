package nengoros.comm.nodeFactory.modem.impl;

import nengoros.comm.nodeFactory.modem.Modem;
import nengoros.exceptions.ConnectionException;

import org.apache.commons.logging.Log;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;

/**
 * This is an implementation of ROS part of Modem.
 * Practically it does nothing, all message listening and publishing is done 
 * in Encoders and Decoders. The task for this one is only to register into the ROS 
 * network and get the ConnectedNode class (factory for everything ROS-related..).
 * 
 * Note: encoders and decoders use ConnectedNode obtained from here for everything 
 *  
 * @author Jaroslav Vitku
 *
 */
public class DefaultModem implements Modem {

	public final String me = "[DefaultModem] ";
	
	private final int sleepTime = 300;
	
	private String myName;
	private Log log;
	private ConnectedNode myRosSide;

	private final int maxWait=17000; // wait 17 seconds for my modem, then throw connection exception
	
	public DefaultModem(){
		myName = "Modem";
	}
	
	public DefaultModem(String name){
		myName = name;
	}

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(myName); }

	@Override
	public void onStart(ConnectedNode connectedNode) {
		log = connectedNode.getLog();
		myRosSide = connectedNode;
		
		System.out.println(me+" starting..!");
		log.info(me+" starting..!");
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

	/**
	 * This could pass null pointer, so methods that are using this have to check it.
	 */
	@Override
	public ConnectedNode getConnectedNode() throws ConnectionException {
		int poc =0;
		// Here we wait for node to be launched to get access to it's resources
		while(!nodeLaunched()){
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {e.printStackTrace();}
			if(poc++ > 2)
				System.out.println("Waiting for MyModem to initialize:" +
					" ..Logger inited: "+ (log!=null)+ 
					" ..myRosSide inited: "+(myRosSide!=null));
			if(poc*sleepTime>maxWait)
				throw new ConnectionException(me+" Giving up waiting for myModem to initialize!"); 
		}
		log.info("MyModem: giving myConnectedNode to someone..");
		return myRosSide;
	}
	
	private boolean nodeLaunched(){
		if(log!=null && myRosSide!=null)
			return true;
		return false;
	}
}
