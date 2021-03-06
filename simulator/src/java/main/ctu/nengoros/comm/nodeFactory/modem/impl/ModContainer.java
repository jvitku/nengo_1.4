package ctu.nengoros.comm.nodeFactory.modem.impl;

import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.nodeFactory.modem.Modem;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.exceptions.ConnectionException;

public class ModContainer implements ModemContainer {

	private final NodeGroup myGroup;
	
	private Modem modem;
	private final String name;
	private boolean running = false;
	
	private final NodeConfiguration conf;
	private final NodeMainExecutor nme;
	
	public ModContainer(NodeConfiguration nc, String name, NodeMainExecutor n, NodeGroup g){
		nme = n;
		this.name = name;
		conf = nc;
		
		if(nc.getNodeName() != null)
			name = nc.getNodeName().toString();
		
		myGroup = g;
	}
	
	@Override
	public ConnectedNode getConnectedNode() throws ConnectionException{
		return modem.getConnectedNode();
	}
	
	@Override
	public void start() {
		if(running){
			System.err.println(name+": I am already running!");
			return;		
		}
		System.out.println(name+": OK, starting myself.");
		nme.execute(this.getModem(), conf);
		running = true;
	}

	@Override
	public void stop() {
		System.out.println(name+" OK, shutting down myself");
		if(isRunning()){
			nme.shutdownNodeMain(this.getModem());
			NodeFactory.killModem(this);
			running = false;
		}
		if(myGroup.isRunning()){
			System.out.println(name+" also shutting down my group");
			myGroup.stopGroup();
		}
	}
	
	@Override
	public String getName() { return name; }

	@Override
	public NodeConfiguration getConfiguration() { return conf;	}

	@Override
	public void reset() {
		//System.err.println(name+": resetting may not be supported directly, but through the ROS service");
	}
	
	@Override
	public void resetModem(){
		myGroup.reset();
	}

	@Override
	public Modem getModem() { return modem; }

	@Override
	public void setModem(Modem myModem) { modem = myModem; }
	
	@Override
	public boolean isRunning() { return running; }

	@Override
	public void useLogging(boolean use) {
		// TODO able to turn out the logging of java nodes
		System.out.println("java node, turn on/off logging is still TODO..");
	}

	@Override
	public NodeGroup getMyGroup() {
		return this.myGroup;
	}

	
}
