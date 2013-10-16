package ctu.nengoros.comm.nodeFactory.nativenode.impl;

import java.util.List;

import ctu.nengoros.comm.nodeFactory.NodeFactory;

/**
 * Compared to RunnableNode, this allows NodeFactory (more concretely
 * a NameProvider) to know about stopping the process. So the NameProvider
 * can remove this unique name of ROS node from list of used names.
 * 
 * @author Jaroslav Vitku
 *
 */
public class UNodeContainer extends RunnableNode {

	// do not use this by default (all nodes belong somewhere, so they are stopped)
	private boolean useAutokiller = false;
	
	public UNodeContainer(List<String> command, String nodeName) {
		super(command, nodeName);
	}
	
	public UNodeContainer(String[] command, String nodeName){
		super(command, nodeName);
	}

	public UNodeContainer(List<String> command, String nodeName, boolean merge){
		super(command, nodeName, merge);
	}
	
	public UNodeContainer(String[] command, String nodeName, boolean merge){
		super(command, nodeName, merge);
	}
	
	@Override
	public void start(){
		super.start();
		
		if(useAutokiller)
			super.startAutoKiller();
	}
	
	/**
	 * Here is the only modification, this class notifies NameProvider 
	 * about exiting. 
	 * Note: starting the node is noted while creating it, not calling start().
	 */
	@Override
	public void stop() {
		super.stop();
		NodeFactory.killNode(this);
	}
	
	/**
	 * define whether you want to start autokiller or not. 
	 * @param use
	 */
	public void setUseAutoKiller(boolean use){
		useAutokiller = use;		
	}


}
