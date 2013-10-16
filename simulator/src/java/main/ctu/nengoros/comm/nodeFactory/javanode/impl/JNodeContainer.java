package ctu.nengoros.comm.nodeFactory.javanode.impl;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.javanode.JavaNodeContainer;

/**
 * Implementation of JavaNode,
 * this class serves as a container for NodeMain (an actual Java ROS node)
 * 
 * @author Jaroslav Vitku
 *
 */
public class JNodeContainer implements JavaNodeContainer{

	private NodeMain node;
	private final String name;
	private boolean running = false;
	
	private final NodeConfiguration conf;
	private final NodeMainExecutor nme;
	
	public JNodeContainer(NodeConfiguration nc, String name, NodeMainExecutor n){
		nme = n;
		this.name = name;
		conf = nc;
		
		if(nc.getNodeName() != null)
			name = nc.getNodeName().toString();
	}
	
	@Override
	public void start() {
		if(running){
			System.err.println(name+": I am already running!");
			return;		
		}
		System.out.println(name+": OK, starting myself.");
		nme.execute(this.getNode(), conf);
		running = true;
	}

	@Override
	public void stop() {
		System.out.println(name+": OK, shutting down myself..");
		nme.shutdownNodeMain(this.getNode());
		NodeFactory.killNode(this);
		running = false;
	}
	
	@Override
	public String getName() { return name; }

	@Override
	public NodeConfiguration getConfiguration() { return conf;	}

	@Override
	public void reset() {
		/*
		System.err.println(name+": resetting probably will not be supported by this way" +
				"if you want to reset node, add the corresponding service to it");
		 */
	}

	@Override
	public NodeMain getNode() { return node; }

	@Override
	public void setNodeMain(NodeMain myNode) { node = myNode; }
	@Override
	public boolean isRunning() { return running; }

	@Override
	public void useLogging(boolean use) {
		// TODO able to turn out the logging of java nodes
		System.out.println("java node, turn on/off logging is still TODO..");
	}
	
}
