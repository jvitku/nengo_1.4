package ctu.nengoros.comm.rosutils.utilNode.time;

/**
 * This thing handles simulation time between the ROS network and Nengo simulator.
 * 
 * It is to be used in the ca.nengo.util.impl.NodeThreadPool.step() method.
 * 
 * There are three main possibilities how to run the simulation:
 * -Nengo as a Time master (Nengo provides its simulation time to other ROS nodes) this is chosen by default.
 * -Time is ignored by the ROS network
 * -Nengo as a Time slave (Some of ROS nodes provides its simulation time to other ROS nodes and to the Nengo simulator)
 * 
 * 
 * @author Jaroslav Vitku
 *
 */
public interface RosTimeUtil {
	
	
	/**
	 * Three possibilities:
	 * -time master: this method publishes stopTime to the Clock topic, @see http://wiki.ros.org/Clock 
	 * and returns unchanged values of startTIme and stopTime 
	 * -time ignored: does nothing, returns unchanged values
	 * -time slave: reads the Clock value 
	 * 
	 * @param startTime startTime of the Nengo simulation step 
	 * @param stopTime stopTime of the Nengo simulation step
	 * @return new float[startTime,stopTime], potentially changed according to external time master
	 */
	float[] handleTime(float startTime, float stopTime);

}
