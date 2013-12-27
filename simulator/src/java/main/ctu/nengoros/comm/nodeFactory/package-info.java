/**
 * Builds containers for ROS nodes. ROS node can be onw of the following: 
 * 
 * Java thread which supports ROSjava and therefore implements org.ros.node.NodeMain. 
 * 
 * Installed ROS node which can be launched by command "rosrun [nodepackage.nodename]"
 * 
 * Or any arbitrary native process. 
 * 
 * TODO: Only the Unix processes are supported so far. Tested on Ubuntu 12 and OS X 10.6, OS X 10.8.     
 * 
 * @author Jaroslav Vitku
 *
 */
package ctu.nengoros.comm.nodeFactory;