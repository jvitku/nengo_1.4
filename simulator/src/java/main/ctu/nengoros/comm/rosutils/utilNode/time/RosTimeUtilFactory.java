package ctu.nengoros.comm.rosutils.utilNode.time;

import java.util.ArrayList;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.javanode.JavaNodeContainer;
import ctu.nengoros.comm.nodeFactory.javanode.JavaNodeLauncher;
import ctu.nengoros.comm.rosutils.utilNode.time.impl.DefaultTimeMaster;
import ctu.nengoros.comm.rosutils.utilNode.time.impl.DefaultTimeSlave;
import ctu.nengoros.comm.rosutils.utilNode.time.impl.IgnoreTime;

/**
 * This produces RosTimeUtils - nodes which handle the time between Nengo and ROS network.
 *  
 * This should start a selected ROSNode for handling time. 
 * 
 * @author Jaroslav Vitku
 *
 */
public class RosTimeUtilFactory {
	
	public static final String noSimTime = "/use_sim_time:=false";
	public static final String simTime = "/use_sim_time:=true";
	
	public static final String defaultTimeMaster = "ctu.nengoros.comm.rosutils.utilNode.time.impl.DefaultTimeMaster";
	public static final String defaultTimeSlave = "ctu.nengoros.comm.rosutils.utilNode.time.impl.DefaultTimeSlave";
	
	/**
	 * Launches default time master node.
	 * 
	 * @param utilNodes ArrayList of utility nodes, which are all shutdown before exiting the app.
	 * @return the launched RosTimeUtil
	 */
	public static RosTimeUtil startDefaultTimeMaster(ArrayList<JavaNodeContainer> utilNodes){
		
		JavaNodeContainer jnc = JavaNodeLauncher.launchNode(new String[]{defaultTimeMaster,noSimTime},DefaultTimeMaster.name,NodeFactory.nme);
		utilNodes.add(jnc);
		jnc.start();
		
		return (RosTimeUtil)jnc.getNode();
	}
	
	/**
	 * Launches the default time slave node.
	 * 
	 * @return launched node
	 */
	public static RosTimeUtil startDefaultTimeSlave(ArrayList<JavaNodeContainer> utilNodes){
		JavaNodeContainer jnc = JavaNodeLauncher.launchNode(new String[]{defaultTimeSlave, simTime},DefaultTimeSlave.name,NodeFactory.nme);
		utilNodes.add(jnc);
		jnc.start();
		
		return (RosTimeUtil)jnc.getNode();
	}
	
	/**
	 * Returns thing which ignores time completely. 
	 * @return
	 */
	public static RosTimeUtil getTimeIgnoringUtil(){
		return new IgnoreTime();
	}
}
	
