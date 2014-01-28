package ctu.nengoros.model.transformMultiTermination;

import java.util.HashMap;
import java.util.LinkedList;

import ctu.nengoros.model.transformMultiTermination.impl.BasicWeights;
import ctu.nengoros.modules.NeuralModule;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.util.TimeSeries;

/**
 * <p>The input to a Node which combines values of multiple own TransformTerminations.</p>
 * 
 * <p>TODO The problem is how to define dynamics for particular TransformTerminations, which can 
 *  have potentially different dimensions.</p> 
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class AbstractTransformMultiTermination implements MultiTermination{

	public static final String me = "[AbstractTransformMultiTermination] ";
	private static final long serialVersionUID = -5806553506661735679L;

	protected final LinkedList <Termination> orderedTerminations;
	protected final HashMap<String, Termination> myTerminations;

	// number of registered terminations
	protected int counter = 0;	
	protected final String name;
	protected final int outputDim;

	protected final Node parent;
	public final float DEF_W = 1;

	protected TimeSeries myValue;

	// setup properties of my Terminations
	protected final Integrator integ;
	//protected final DynamicalSystem lti;
	
	protected final WeightFactory wg = new BasicWeights();	// define default weights

	/**
	 * Common part of TransformMultiTerminations, thing that has similar functionality
	 * as an ordinary Termination, but its output combines values from multiple 
	 * TransformTerminations. Since the TransformTerminations can implement transformation
	 * from input of different dimension than the result has.
	 * @param parent parent node (Encoder or Node probably)
	 * @param name name of this MultiTermination (probably equals to the name of ROS topic)
	 * @param integ integration applied to the inputs of particular MultiTerminations
	 * @param outputDimension dimensionality of this MultiTermination, equals to the dimensionality
	 * of Encoder (Node)
	 */
	public AbstractTransformMultiTermination(NeuralModule parent, String name,
			Integrator integ, int outputDimension){

		//this.lti = defaultDynamics;
		this.integ = integ;

		this.name = name;
		this.parent = parent;
		this.orderedTerminations = new LinkedList<Termination>();
		this.myTerminations = new HashMap<String, Termination>();

		//this.outputDim = lti.getInputDimension();
		this.outputDim = outputDimension;

		try {
			this.addTermination();
		} catch (StructuralException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public HashMap<String,Termination> getTerminations(){
		return this.myTerminations;
	}

	/**
	 * Connect data on all its Terminations, terminations are ran by 
	 * the NeuralModule to which they are registered (too). 
	 *
	 * @param startTime simulation time at which running starts (s)
	 * @param endTime simulation time at which running ends (s)
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime) throws SimulationException{

		this.runCombineValues(startTime, endTime);
	}

	@Override
	public void reset(boolean randomize) {

		for(int i=0; i<this.orderedTerminations.size(); i++){
			Termination t = this.orderedTerminations.get(i);
			t.reset(false);
		}
	}

	@Override
	public String getName() { return name; }

	/**
	 * Runs all its Terminations.
	 * 
	 * @param startTime
	 * @param endTime
	 * @throws SimulationException
	 */
	protected abstract void runAllTerminations(float startTime, float endTime) 
			throws SimulationException;

	/**
	 * Collects data from its Terminations and sets its own value
	 *  
	 * @param startTime
	 * @param endTime
	 * @throws SimulationException
	 */
	protected abstract void runCombineValues(float startTime, float endTime) 
			throws SimulationException;

	/**
	 * This method generates unique name for my Termination based on
	 * the name of MultiTermination. For the backwards compatibility, the
	 * first Termination is created by default and is named identically
	 * as the MultiTermination.
	 * 
	 * @return name of the Termination. If no Terminations registered so far, return my name.
	 */
	protected String generateName(){
		if(counter==0){
			counter++;
			return this.name;
		}
		return name+"_"+((counter++)-1); // start from 0
	}
	
	@Override
	public Termination addTermination() throws StructuralException{
		return this.addTermination(wg.eye(this.outputDim));
	}
	
	@Override
	public Termination addTermination(float weight) throws StructuralException{
		return this.addTermination(wg.eye(this.outputDim, weight));
	}

	@Override
	public abstract Termination addTermination(final float[][] weights) throws StructuralException;

	/**
	 * Check dimension of 2D transformation matrix, this will be used inside the TransformTermination 
	 * @param weights transformation matrix
	 * @throws StructuralException thrown if dimensions are incorrect
	 * @see ctu.nengoros.model.termination.TransformTermination
	 */
	protected void checkDimensions(final float[][] weights) throws StructuralException{

		/*
		if(weights.length != inputDim)
			throw new StructuralException(me+"incorrect dimensionality" +
					" of weight matrix, expected 2D matrix should have the "
					+ "first dimension of size: " +inputDim);
		 */
		if(weights[0].length != this.outputDim)
			throw new StructuralException(me+"incorrect dimensionality" +
					" of weight matrix, size of the second dimension of the weight " +
					"matrix shoudl is "+this.outputDim);
	}

	@Override
	public Node getNode() { return this.parent;	}

	@Override
	public int getDimension(){
		// this is how BasicTermination determines its dimension
		return this.outputDim;	
	}

	@Override
	public TimeSeries getOutput() { return this.myValue; }
}

