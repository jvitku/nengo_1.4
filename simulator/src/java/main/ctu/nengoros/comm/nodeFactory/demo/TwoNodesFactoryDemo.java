package ctu.nengoros.comm.nodeFactory.demo;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;

/**
 * This demo starts roscore and launches two nodes in one group of nodes.
 * One node publishes integers, the other is subscribed to the corresponding topic.
 * 
 * 
 * 
 * Note: if roscore and rxgraph are found on this computer: will be automatically
 * launched and closed.
 * 
 * @author Jaroslav Vitku
 *
 */
public class TwoNodesFactoryDemo {

	/**
	 * Generally: name of each node is composed of node group +separator+ node name. 
	 * NodeFactory launches only nodes with unique names, so if nodes are independent, 
	 * the NodeFactory prefers to push them each into its own namespace. Namespace
	 * is generated so that complete names of all running nodes in the network are
	 * independent. 
	 * Pushing into namespace shields also ros topics. If nodes are dependent, 
	 * NodeFactory only renames them (similar to namespace pushing) but topics
	 * remain unshielded, and therefore compatible with other nodes.
	 * 
	 * @param args are ignored
	 */
	public static void main(String[] args) {
		//boolean indep=false;		// this launches group with no namespace (modifies names)
		//boolean indep=false;		// this launches group with generated namespace
		String indep = "myCustomMamespace";		// launches group with given namespace
		runDemo(indep);
	}
	
	/**
	 * @param indep whether the group is independent
	 */
	protected static void runDemo(boolean indep){
		
		RosUtils.prefferJroscore(true);
		
		String talker = "resender.mt.IntSender";
		String receiveer = "resender.mt.IntReceiver";
		
		// group called pubsub which is independent?
		NodeGroup g = new NodeGroup("pubsub",indep);
		g.addNC(talker, "talker", "java");
		g.addNC(receiveer, "receiver", "java");		
		
		g.startGroup();
		
		System.out.println("Names of running nodes are: " +
				Mess.toAr(NodeFactory.np.namesOfRunningNodes()));
		
		Mess.waitForKey();
		
		//g.stopGroup(); 
		
		RosUtils.utilsShallStop();
	}
	
	/**
	 * @param indep whether the group is independent
	 */
	protected static void runDemo(String namespace){
		
		RosUtils.prefferJroscore(true);
		
		String talker = "resender.mt.IntSender";
		String receiveer = "resender.mt.IntReceiver";
		
		// group called pubsub which is independent?
		NodeGroup g = new NodeGroup("pubsub",namespace);
		g.addNC(talker, "talker", "java");
		g.addNC(receiveer, "receiver", "java");		
		
		g.startGroup();
		
		System.out.println("Names of running nodes are: " +
				Mess.toAr(NodeFactory.np.namesOfRunningNodes()));
		
		Mess.waitForKey();
		
		//g.stopGroup(); 
		
		RosUtils.utilsShallStop();
	}
	
	
}

