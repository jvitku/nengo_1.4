package ctu.nengoros.comm.nodeFactory.demo.turtle;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;
/**
 * Runs two independent simulations of turtle, each with own controls. 
 * 
 * @author Jaroslav Vitku
 *
 */
public class TwoTurtles {

	public static void main(String[] args) {
		
		
		RosUtils.prefferJroscore(false);
//		String turtlesim = "../testnodes/turtlesim/turtlesim_node"; // turtle under this project
		String[] turtlesim = new String[]{"rosrun", "turtlesim","turtlesim_node"}; // installed one
		
		String className = "ctu.nengoros.test.resender.turtle.Controller";
		String clName = "ctu.nengoros.test.resender.turtle.PositionSensor";

		// create group with a name
		NodeGroup g = new NodeGroup("zelvicka", true);
		
		// add node configurations to the group
		g.addNode(turtlesim, "zelva", "native");
		g.addNode(className, "actuators", "java");
		g.addNode(clName, "sensors", "java");
		
		// create group with a name
		NodeGroup gg = new NodeGroup("zelvicka", true);
		
		// add node configurations to the group
		gg.addNode(turtlesim, "zelva", "native");
		gg.addNode(className, "actuators", "java");
		gg.addNode(clName, "sensors", "java");
		
		// start all nodes in the group
		g.startGroup();
		System.out.println("Names of Running ndoes are :"+Mess.toAr(NodeFactory.np.namesOfRunningNodes())+"|");
		gg.startGroup();
		System.out.println("Names of Running ndoes are now: "+Mess.toAr(NodeFactory.np.namesOfRunningNodes())+"|");

		Mess.waitForKey();
		
		// stop group
		g.stopGroup();
		gg.stopGroup();

		// stop roscore and rxgraph
		RosUtils.utilsShallStop();
		
	}
}

