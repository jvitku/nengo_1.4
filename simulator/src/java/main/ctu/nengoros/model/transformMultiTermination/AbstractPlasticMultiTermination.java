package ctu.nengoros.model.transformMultiTermination;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ctu.nengoros.model.plasticity.PlasticTermination;
import ctu.nengoros.modules.NeuralModule;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.util.TimeSeries;

/**
 * <p>The main difference compared to the {@link ctu.nengoros.model.multiTermination.AbstractMultiTermination}
 * is that this uses PlasticTerminations, which means that weights of these Terminations may, or may
 * not change during the simulation run.</p>
 * 
 * <p>Each {@link ctu.nengoros.model.plasticity.PlasticTermination} extends 	 
 * 
 *  
 * TODO check modifications
 * TODO add Thread features
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class AbstractPlasticMultiTermination implements PlasticMultiTermination{

	public static final String me = "[AbstractPlasticMultiTermination] ";
	private static final long serialVersionUID = -5806553506661735678L;

	protected final LinkedList <PlasticTermination> orderedTerminations;
	protected final Map<String, Float[][]> myWeights;				// weight for each Termination
	protected final HashMap<String, Termination> myTerminations;

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

	public AbstractPlasticMultiTermination(NeuralModule parent, String name, /*int dimension,*/ Integrator integ, DynamicalSystem lti2){

		this.lti = lti2;
		this.integ = integ;

		this.name = name;
		this.parent = parent;
		this.orderedTerminations = new LinkedList<PlasticTermination>();
		this.myTerminations = new HashMap<String, Termination>();
		this.myWeights = new HashMap<String, Float[][]>();

		this.dimensions = lti.getInputDimension();

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
	public Termination addTermination(float weight) throws StructuralException{
		return this.addTermination(this.generateWeights(weight));
	}

	public abstract Termination addTermination(final Float[] weights) throws StructuralException;

	@Override
	public Termination addTermination() throws StructuralException{
		return this.addTermination(this.generateWeights(this.DEF_W));
	}

	protected void checkDimensions(final Float[] weights) throws StructuralException{

		if(weights.length != this.dimensions)
			throw new StructuralException(me+"incorrect dimensionality" +
					" of weights, dimension of this MultiTermination is "+this.dimensions);
	}

	/**
	 * Check dimension of 2D transformation matrix 
	 * @param weights transformation matrix
	 * @throws StructuralException thrown if dimensions are incorrect
	 * @see 
	 */
	protected void checkDimensions(final Float[][] weights) throws StructuralException{

		if(weights.length==0)
			throw new StructuralException(me+"incorrect dimensionality" +
					" of weights, expected 2D matrix with first non-zero dimension" +
					"and the second dimension of size: "+this.dimensions);

		if(weights[0].length != this.dimensions)
			throw new StructuralException(me+"incorrect dimensionality" +
					" of weights, size of the second dimension of the weight " +
					"matrix shoudl is "+this.dimensions);
	}

	protected Float[][] readWeights(String name) throws SimulationException{

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

