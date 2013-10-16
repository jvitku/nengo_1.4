package ctu.nengoros.comm.nodeFactory.demo;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.modules.AsynNeuralModule;
import ctu.nengoros.modules.impl.test.NeuralModuleTest;

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
public class ModemF2I {

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
		boolean indep=false;		// this launches group with generated namespace
		//String indep = "myCustomMamespace";		// launches group with given namespace
		runDemo(indep);
	}

	/**
	 * @param indep whether the group is independent
	 */
	protected static void runDemo(boolean indep){

		RosUtils.prefferJroscore(true);


		String minimax = "resender.mpt.F2IPubSub";
		String modem = "nengoros.comm.nodeFactory.modem.impl.MySecondModem";

		NodeGroup g = new NodeGroup("pubsub",false);

		g.addNC(minimax,"minimaxNode","java");
		g.addNC(modem,"modemNode","modem");

		g.startGroup();

		ModemContainer md = g.getModem();
		System.out.println("name of nmodem obtained is:"+md.getName());
		AsynNeuralModule smartOne = new NeuralModuleTest("SmartNeuron",md);

		smartOne.createDecoder("ros2annFloatArr", "int", 2);
		smartOne.createEncoder("ann2rosFloatArr", "float", 4);
		
		System.out.println("Names of running nodes are: " +
				Mess.toAr(NodeFactory.np.namesOfRunningNodes()));

		Mess.waitForKey();

		//g.stopGroup(); 

		RosUtils.utilsShallStop();
	}



}

