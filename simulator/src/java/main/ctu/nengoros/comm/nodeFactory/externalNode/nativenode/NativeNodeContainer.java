package ctu.nengoros.comm.nodeFactory.externalNode.nativenode;

import ctu.nengoros.comm.nodeFactory.externalNode.ExternalNodeContainer;



/**
 * 
 * NativeNode should be able to create process by means of ProcessBuilder
 * (ideally in a constructor) according to launchCommand and prepare it 
 * for starting. These nodes should support manual and auto-stopping.
 * 
 * @author Jaroslav Vitku
 *
 */

public interface NativeNodeContainer extends ExternalNodeContainer{
	
	/**
	 * @return the command which was (will be) used to start external process
	 */
	public String[] getLauchCommand();

	/**
	 * This thread is able to automatically stop the process
	 * according to the variable 
	 */
	@Deprecated
	public void startAutoKiller();
	
}


