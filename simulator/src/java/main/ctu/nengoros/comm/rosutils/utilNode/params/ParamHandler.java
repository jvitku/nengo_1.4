package ctu.nengoros.comm.rosutils.utilNode.params;

import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;

import ctu.nengoros.rosparam.node.RosparamNode;
/**
 * Allows Nengo to set/read parameters from the parameter server (ROS master).
 * 
 * This could be implemented as one ROS node, but due to problems with timeProvider
 * ( @see: ctu.nengoros.time.AbstractTimeNode , @see http://code.google.com/p/rosjava/issues/detail?id=148 )
 * both, Rosparam and RosTimeUtilFactory will be implemented as separate nodes, where RosTimeUtilFactory 
 * may not bee launched at all.  
 * 
 * @author Jaroslav Vitku
 *
 */
public class ParamHandler extends RosparamNode{
	
	public static final String name = "NengoParameterHandler";
	public final String me = "["+name+"] ";
	
	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(name); }
	
	@Override
	public void onStart(ConnectedNode connectedNode){
		super.onStart(connectedNode);
		//l = connectedNode.getLog();
	//	l.info(me+"launched! Waiting for commands..");
	}	

}
