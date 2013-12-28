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

/**
 * TODO: add the support for weighted multi-dimensional terminations.
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class AbstractMultiTermination implements MultiTermination{

	public final String me = "[AbstractMultiTermination] ";

	private static final long serialVersionUID = -5806553506661735679L;

	protected final LinkedList <Termination> orderedTerminations;
	protected final Map<String, Float> weights;				// weight for each Termination
	protected final Map<String, Termination> myTerminations;
	
	// dimensionality of my input
	protected final int dimension;	

	protected double t_start = 0; 
	protected double t_end=0;
	protected double t=0;

	// the resulting value of this multiTermination
	protected float value = 0;	

	// number of registered terminations
	protected int counter = 0;	
	protected final String name;

	protected final Node parent;
	
	//public final float DEF_W = 1;
	
	protected String mess = me+"ERROR: suport fot multidimensional Terminations" +
			"is not implemented so far!";
	
	// setup properties of my Terminations
	protected final Integrator integ;
	protected final DynamicalSystem lti;
	
	public AbstractMultiTermination(NeuralModule parent, String name, int dimension, Integrator integ, DynamicalSystem lti2){
		
		this.lti = lti2;
		this.integ = integ;
		
		this.t_start=0;
		this.t_end=0;
		this.t=0;

		this.dimension = dimension;
		this.name = name;
		this.parent = parent;
		this.orderedTerminations = new LinkedList<Termination>();
		this.myTerminations = new HashMap<String, Termination>();
		this.weights = new HashMap<String, Float>();
		
		//TODO
		/*
		if(dimension>1)
			System.err.println(mess);
			*/
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
		this.value = 0; 

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

	/**
	 * Add new termination with its own weight.
	 */
	@Override
	public abstract String addTermination(float weight) throws StructuralException;
	
	@Override
	public String addTermination(float[][] weights) throws StructuralException{
		// TODO
		this.checkDimensions(weights);
		return null;
	}

	@Override
	public String addTerminaton() throws StructuralException{
		return this.addTermination(this.dimension);
	}

	// TODO: add support for multidimensional terminations
	protected void checkDimensions(float[][] weights) throws StructuralException{
		
		System.err.println(mess);
		throw new StructuralException(mess);
	}
	
	protected float readWeight(String name) throws SimulationException{
		if(!this.weights.containsKey(name))
 			throw new SimulationException(me+"ERROR: weight for this Termination not found: "+name);
		
		return this.weights.get(name);
	}
	
	@Override
	public Node getNode() { return this.parent;	}

}
