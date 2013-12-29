package ctu.nengoros.comm.rosBackend.encoders.multiTermination;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ctu.nengoros.modules.NeuralModule;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.util.TimeSeries;

/**
 * TODO: add the support for weighted multi-dimensional terminations.
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class AbstractMultiTermination implements MultiTermination{

	public static final String me = "[AbstractMultiTermination] ";
	private static final long serialVersionUID = -5806553506661735679L;

	protected final LinkedList <Termination> orderedTerminations;
	protected final Map<String, Float[]> myWeights;				// weight for each Termination
	protected final Map<String, Termination> myTerminations;

	// number of registered terminations
	protected int counter = 0;	
	protected final String name;
	protected final int dimensions;

	protected final Node parent;
	public final float DEF_W = 1;
	
	protected TimeSeries myValue;

	// setup properties of my Terminations
	protected final Integrator integ;
	protected final DynamicalSystem lti;


	public AbstractMultiTermination(NeuralModule parent, String name, /*int dimension,*/ Integrator integ, DynamicalSystem lti2){

		this.lti = lti2;
		this.integ = integ;

		this.name = name;
		this.parent = parent;
		this.orderedTerminations = new LinkedList<Termination>();
		this.myTerminations = new HashMap<String, Termination>();
		this.myWeights = new HashMap<String, Float[]>();

		this.dimensions = lti.getInputDimension();
	}

	/**
	 * Runs all its Terminations, then combines their to its own value. 
	 *
	 * @param startTime simulation time at which running starts (s)
	 * @param endTime simulation time at which running ends (s)
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime) 
			throws SimulationException{

		this.runAllTerminations(startTime, endTime);

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
		return name+"_"+counter++;
	}

	@Override
	public String addTermination(float weight) throws StructuralException{
		return this.addTermination(this.generateWeights(weight));
	}

	// physically create and Register the termination
	@Override
	public abstract String addTermination(final Float[] weights) throws StructuralException;

	@Override
	public String addTerminaton() throws StructuralException{
		return this.addTermination(this.generateWeights(this.DEF_W));
	}

	protected void checkDimensions(final Float[] weights) throws StructuralException{

		if(weights.length != this.dimensions)
			throw new StructuralException(me+"incorrect dimensionality" +
					" of weights, dimension of this MultiTermination is "+this.dimensions);
	}

	protected Float[] readWeights(String name) throws SimulationException{

		if(!this.myWeights.containsKey(name))
			throw new SimulationException(me+"ERROR: weight for this Termination not found: "+name);

		return this.myWeights.get(name);
	}

	protected Float[] generateWeights(float weight){
		Float[] w = new Float[this.dimensions];
		for(int i=0; i<w.length; i++){
			w[i] = weight;
		}
		return w;
	}

	@Override
	public Node getNode() { return this.parent;	}

	@Override
	public int getDimension(){
		// this is how BasicTermination determines its dimension
		return this.dimensions;	
	}
	

	@Override
	public TimeSeries getOutput() { return this.myValue; }
}

