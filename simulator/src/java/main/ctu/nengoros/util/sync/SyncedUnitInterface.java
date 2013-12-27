package ctu.nengoros.util.sync;

import ca.nengo.model.StructuralException;

/**
 * This is supposed to implement simple synchronization between Nengo and ROS nodes.
 * 
 * Basic idea:
 * <ul>
 * 	<li>each Unit can have two states: ready / notReady</li>
 *  <li>each Unit can have arbitrary number of children</li>
 *  <li>each children is also unit</li>
 *  </ul>
 *  
 * Simulator can easily determine whether all components of simulation are ready. 
 * Extend unit class in order to synchronize.
 * 
 *   
 * @author Jaroslav Vitku
 */
public interface SyncedUnitInterface {
	
	public boolean isReady();
	
	public void setReady(boolean ready);
	
	/**
	 * Make me and all childs not ready (except those set to asynchronous - alwaysready)
	 */
	public void discardChildsReady();
	
	public void setSynchronous(boolean ready);
	
	/**
	 * enables to create tree of units
	 * unit is ready if all childs are ready
	 * or if flag "alwaysReady" is on 
	 * @param child
	 */
	public void addChild(SyncedUnitInterface child);
	
	public void removeChild(SyncedUnitInterface child);
	
	public String getName();
	
	public void setName(String name) throws StructuralException; 
}
