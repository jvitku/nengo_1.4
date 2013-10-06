/**
 * This is supposed to implement simple synchronization between Nengo and ROS nodes
 * 
 * Basic idea:
 *	-each Unit can have two states: ready / notReady
 *  -each Unit can have arbitrary number of children
 *  -each children is also unit
 *  
 * Simulator can easily determine whether all components of simulation are ready.
 * Extend unit class in order to synchronize.
 * 
 * 
 */
/**
 * @author Jaroslav Vitku
 *
 */
package nengoros.util.sync;