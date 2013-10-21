package ctu.nengoros.comm.nodeFactory.demo.turtle;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;

/**
 * Runs demo with turtle, where turtle has its own controlled and sensoric system 
 * which registers position of turtle in the map.
 * 
 * 
 * This demo launches a group of nodes consisting of 2 java nodes and one native
 * node, which is simulator of turtle, written in C++, part of ROS installation. 
 *
 * After pressing any key, all threads and processes should be ended.
 * 
 * Summary: 
 * 	-demo automatically starts roscore and rxgraph (if ROS installed), for each:
 * 		-process itself
 * 		-2 threads for consuming stdout and stderr
 * 		-1 thread checking whether to stop the process
 * -turtlesim process:
 * 		-process itself
 * 		-2 threads for consuming ..
 * 		-1 thread checking whether to stop
 * -controller and sensor, for each:
 * 		-1 thread with the NodeMain (ROS implementation of Node)
 * 	
 * summary:
 * 		-11 threads + the main one
 * 	
 * 
 * @author Jaroslav Vitku
 *
 */
public class OneTurtle {

	public static void main(String[] args) {
		
		RosUtils.prefferJroscore(false);

		String turtlesim = 	"../testnodes/turtlesim/turtlesim_node";
		String className = 	"resender.turtle.Controller";
		String clName = 	"resender.turtle.PositionSensor";

		// create group with a name
		NodeGroup g = new NodeGroup("zelvicka", true);
		
		// add node configurations to the group
		g.addNode(turtlesim, "zelva", "native");
		g.addNode(className, "actuators", "java");
		g.addNode(clName, "sensors", "java");
		
		// start all nodes in the group
		g.startGroup();
		
		Mess.waitForKey();
		
		// stop group
		g.stopGroup();
		
		RosUtils.utilsShallStop();
	}
}

