package ctu.nengoros.comm.rosutils.utilNode.time;

import java.util.ArrayList;

import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeMainExecutor;

import ctu.nengoros.comm.nodeFactory.javanode.JavaNodeContainer;
import ctu.nengoros.comm.nodeFactory.javanode.JavaNodeLauncher;
import ctu.nengoros.comm.rosutils.utilNode.params.ParamHandler;
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
	
	public static final NodeMainExecutor nme = DefaultNodeMainExecutor.newDefault();
	
	public static final String time = "/use_sim_time";
	public static final String simTime = "/use_sim_time:=true";
	public static final String noSimTime = "/use_sim_time:=false";
	
	public static final String defaultTimeMaster = "ctu.nengoros.comm.rosutils.utilNode.time.impl.DefaultTimeMaster";
	public static final String defaultTimeSlave = "ctu.nengoros.comm.rosutils.utilNode.time.impl.DefaultTimeSlave";
	
	public static final String defaultParameterHandler = "ctu.nengoros.comm.rosutils.utilNode.params.ParamHandler";
	
	/**
	 * Launches default time master node.
	 * 
	 * @param utilNodes ArrayList of utility nodes, which are all shutdown before exiting the app.
	 * @return the launched RosTimeUtil
	 */
	public static RosTimeUtil startDefaultTimeMaster(ArrayList<JavaNodeContainer> utilNodes){
		
		JavaNodeContainer jnc = JavaNodeLauncher.launchNode(new String[]{defaultTimeMaster,noSimTime},DefaultTimeMaster.name, nme);
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
		JavaNodeContainer jnc = JavaNodeLauncher.launchNode(new String[]{defaultTimeSlave, simTime},DefaultTimeSlave.name, nme);
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
	
	public static void stopNME(){ nme.shutdown(); }
	
	
	/**
	 * Start the parameter handler for Nengo, this does not listen to the simulation time, so 
	 * bug in the Rosjava (where timeProvider not started, the getCurrentTime and getLog throw NLP)
	 * will not affect it. 
	 * 
	 * @param utilNodes arrayList of utility nodes that should be stopped before application exits
	 * @return running parameter handler
	 */
	public static ParamHandler startParamHandler(ArrayList<JavaNodeContainer> utilNodes){
		JavaNodeContainer jnc = JavaNodeLauncher.launchNode(new String[]{defaultParameterHandler, noSimTime},ParamHandler.name, nme);
		utilNodes.add(jnc);
		jnc.start();
		
		return (ParamHandler)jnc.getNode();
	}
}
	
