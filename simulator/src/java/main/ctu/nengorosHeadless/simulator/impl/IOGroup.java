package ctu.nengorosHeadless.simulator.impl;

/**
 * Each IOGroup can represent either Termination (input) or Origin (output) of a Neural Module.
 * 
 * Each IOGroup has data vector of given length. This corresponds to the same no. of "neurons", their
 * outputs/inputs in the source/target layer.
 * 
 * IOgroup has vector of indexes in the global vector of source/target neurons.
 *  
 * @author Jaroslav Vitku
 *
 */
public class IOGroup {
	
	private final int noUnits;
	private final int[] indexes;
	
	public IOGroup(int[] indexes){
		this.noUnits = indexes.length; 
		this.indexes = indexes;
	}
	
	public int[] getIndexes(){
		return this.indexes;
	}

	public int getNoUnits(){
		return this.noUnits;
	}
}
