package ctu.nengorosHeadless.simulator;

import ctu.nengorosHeadless.network.connections.InterLayerWeights;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;
import ca.nengo.model.StructuralException;

public interface EALayeredSimulator extends EASimulator{
	
	/**
	 * Fully connect all Origins with all Termination in the given InterLayer
	 * 
	 * @param interLayerNo no. of interlayer
	 * @throws StructuralException 
	 */
	public void makeFullConnections(int interLayerNo) throws StructuralException;


	/**
	 * Returns intelayer connection of a given model. Interlayer connect two layers by full connections.
	 * InterLayerWeights holds a structure with information about IOGroups. 
	 * 
	 * @param no number of interlayer in the model (starting with 0)
	 * @return
	 */
	//public InterLayerWeights getInterLayerNo(int no);
	
	
	public void registerOrigin(Orig o, int interLayerNo) throws StructuralException;
	public void registerTermination(Term t, int interLayerNo) throws StructuralException;
	
}
