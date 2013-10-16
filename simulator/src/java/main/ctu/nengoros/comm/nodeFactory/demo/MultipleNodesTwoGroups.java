package ctu.nengoros.comm.nodeFactory.demo;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;

/**
 * This demo starts (j)roscore (and rxgrpah if found) and launches three nodes.
 * THere are two groups, and two methods:
 * 	-runDemo(false)	
 * 		-runs two identical groups of nodes, and these groups are independent
 * 		-it indicates that they can be pushed into its own namespace, so no conflict emerges
 * 	-runDemo(tru)
 * 		-runs two identical groups of nodes, and these groups are NOT independet
 * 		-this means that they will not be pushed anywhere, and their topics remain unchanged
 * 		-this creates conflict in topic names and both receivers receive messages from both publishers
 * 	-twoDependent
 * 		-runs demo with two nodes and adds the third one, which is detached from communicationsNote: launch rxgraph while demo running for better clarity of examples
 * 
 * Note: roscore and rxgraph are automatically started if found on the computer
 * 
 * @author Jaroslav Vitku
 *
 */
public class MultipleNodesTwoGroups {

	
	/**
	 * @param args are ignored
	 */
	public static void main(String[] args) {

		//runDemo(false);	// run demo with two identical groups without conflict
		//runDemo(true);	// run demo with two identical groups with conflict (same topics)
		twoDependent();		// launch two dependent nodes, and then one independent
	}
	
	/**
	 * Launches demo with two identical groups, we can choose whether to make conflict
	 * (in published and subscribed topics) or not
	 * 
	 * @param makeConflict make conflict between groups
	 */
	protected static void runDemo(boolean makeConflict){
		
		RosUtils.prefferJroscore(true);
		
		String talkerClass = "resender.mt.IntSender";
		String listenerClass = "resender.mt.IntReceiver";

		// if make conflict, groups are not independent (preserve no namespace) and will 
		// be conflict there..
		NodeGroup g1 = new NodeGroup("group1",!makeConflict);
		g1.addNC(talkerClass, "talker", "java");
		g1.addNC(listenerClass, "listener", "java");
		
		g1.startGroup();
		
		System.out.println("Waiting 3 seconds and then starting group with identical nodes");
		Mess.wait(3);
		System.out.println("starting it now");
		
		NodeGroup g2 = new NodeGroup("group1",!makeConflict);
		g2.addNC(talkerClass, "talker", "java");
		g2.addNC(listenerClass, "listener", "java");
		g2.startGroup();
		
		System.out.println("observe conflict resolving ...");
		
		Mess.waitForKey();
		
		g1.stopGroup();
		g2.stopGroup();
		
		RosUtils.utilsShallStop();
	}
	
	/**
	 * Launches demo where two nodes are dependent
	 * and third one is independent (no conflict..)
	 */
	protected static void twoDependent(){
		
		String talkerClass = "resender.onoff.Talker";
		String listenerClass = "resender.mt.IntReceiver";
		
		NodeGroup n = new NodeGroup("demo",false);
		n.addNC(talkerClass, "talker", "java");
		n.addNC(listenerClass, "listener", "java");
		
		System.out.println("Launching two nodes.. waiting several seconds and " +
				"launching another (but independent) talker (identical to the one already running)");
		n.startGroup();
		
		Mess.wait(10);
		
		NodeGroup n2 = new NodeGroup("demo", true);
		n2.addNC(talkerClass, "talker", "java");
		n2.startGroup();
		
		System.out.println("observe conflict resolving.. ");
		
		Mess.waitForKey();
		
		n2.stopGroup();
		n.stopGroup();
		
		RosUtils.utilsShallStop();
	}
}


