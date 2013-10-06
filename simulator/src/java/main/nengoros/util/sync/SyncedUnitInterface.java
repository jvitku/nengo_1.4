package nengoros.util.sync;

import ca.nengo.model.StructuralException;

/**
 * Represents basically an object that can be in two states: ready/notReady
 * If you want to use hierarchical infrastructure, extend abstract SyncedUnit
 *  
 * @author Jaroslav Vitku
 *
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
