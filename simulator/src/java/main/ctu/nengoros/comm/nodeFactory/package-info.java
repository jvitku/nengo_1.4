/**
 * Builds containers for ROS nodes. ROS node can be e.g.:
 * -java thread which supports ROSjava and therefore implements org.ros.node.NodeMain
 * -installed ROS node which can be launched by command "rosrun [nodepackage.nodename]"
 * -in fact an arbitrary native process 
 * 
 * note: so far only Unix processes are supported, tested on Ubuntu 12 and OS X 10.6.8     
 * 
 * @author Jaroslav Vitku
 *
 */
package ctu.nengoros.comm.nodeFactory;