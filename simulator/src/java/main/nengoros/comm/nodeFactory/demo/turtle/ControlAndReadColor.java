package nengoros.comm.nodeFactory.demo.turtle;

import nengoros.comm.nodeFactory.NodeGroup;
import nengoros.comm.rosutils.Mess;
import nengoros.comm.rosutils.RosUtils;

/**
 * Launches two nodes, one for controlling the turtle, one for reading the color
 * from sensor. 
 * 
 * The required turtlesim can be ran (if ROS installed) from command line by:
 *  	rosrun turtlesim turtlesim_node
 * 
 * @author Jaroslav Vitku
 *
 */
public class ControlAndReadColor {

	/**
	 * @param args are ignored
	 */
	public static void main(String[] args) {

		boolean runUtils = true;
		
		runDemo(runUtils);
	}
	
	private static void runDemo(boolean runUtils){
		
		RosUtils.setAutorun(runUtils);		
		// define and run controller
		String actuator = "resender.turtle.Controller";
		String colorSensor = "resender.turtle.PositionSensor";
		
		// run non-independent group called sense-act
		// non-independent means that name-space will not be changed
		NodeGroup g = new NodeGroup("senseAct", false);
		
		g.addNC(actuator, "actuator", "java");	// add actuator class
		g.addNC(colorSensor, "sensor", "java");	// add sensor class
		
		g.startGroup();
		
		Mess.waitForKey();
		
		// TODO: make this automatic also for java nodes?
		g.stopGroup();
		
		RosUtils.utilsShallStop();
	}
}
