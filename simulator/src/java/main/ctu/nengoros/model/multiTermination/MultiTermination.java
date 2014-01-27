package ctu.nengoros.model.multiTermination;

import java.io.Serializable;
import java.util.HashMap;

import ca.nengo.model.Node;
import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.util.TimeSeries;

/**
 * <p>This is as similar as possible to the Termination. The main difference is that this holds
 * multiple terminations for one Node and represents one Nodes "input".</p>
 * 
 * <p>Run of the MultiTermination is composed of two steps: run all its Terminations and 
 * sum all values on these terminations. The entire process should be transparent to the 
 * Nengo except the fact that the particular Node is responsible for running own MultiTerminations
 * and read their values</p>   
 *  
 * @author Jaroslav Vitku
 *
 */
public interface MultiTermination  extends Serializable, Resettable, Cloneable{

	public String getName();
	

	/**
	 * Add new weighted termination with the default weight (1).
	 * The Terminations are named as follows: first Termination is created
	 * automatically and corresponds with the name of MultiTermination (backwards
	 * compatibility). Following added Terminations are named as follows:
	 * "[MultiTermination_name]_0",   
	 * "[MultiTermination_name]_1", 
	 *  ...
	 *  
	 * @return new Termination with auto-generated name
	 */
	public Termination addTermination() throws StructuralException;

	/**
	 * Specify the weight of this newly created Termination, if the 
	 * Termination is multi-dimensional, weight will be applied on all dimensions.
	 *  
	 * @param weight the termination can be weighter
	 * @return new weighted Termination with auto-generated name 
	 */
	public Termination addTermination(float weight) throws StructuralException;
	
	
	/**
	 * Specify the weight matrix for this newly created weighted Termination.
	 * 
	 * @param weights weight matrix
	 * @return new weighted Termination with auto-generated name 
	 */
	public Termination addTermination(Float[] weights) throws StructuralException;

	/**
	 * Create new termination of dimension = weights.length and connect it to the 
	 * current parent Encoder of dimension weights[0].length
	 * 
	 * @param weights array of weights where the first dimension corresponds to the 
	 * dimension of an Origin to be connected and the second dimension corresponds to
	 * the parent Encoder  
	 * @return newly created Termination of dimension weights.length
	 * @throws StructuralException thrown if the size of second dimension is not correct
	 */
	public Termination addTermination(Float[][] weights) throws StructuralException;
	
	public HashMap<String,Termination> getTerminations();
	
	/**
	 * This supposed that all my Terminations were ran before, so 
	 * this method just combines values on them to one own value.
	 *  
	 * This method sets the value of TimeSeries myOutput, which can be obtained 
	 * by means of getOutput() method after that.
	 *
	 * @param startTime simulation time at which running starts (s)
	 * @param endTime simulation time at which running ends (s)
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime) throws SimulationException;
	
	
	/**
	 * Get output of this MultiTermination
	 * 
	 * @return TimeSeries representing the actual output
	 */
	public TimeSeries getOutput();

	/**
	 * Get parent Node of my all Terminations.
	 * 
	 * @return
	 */
	public Node getNode();

	
	/**
	 * NUmber of values passed through multiTerminations, the same 
	 * as dimensions of all child Terminations.
	 * @return dimensionality of the multi-termination
	 */
	public int getDimension();
}
