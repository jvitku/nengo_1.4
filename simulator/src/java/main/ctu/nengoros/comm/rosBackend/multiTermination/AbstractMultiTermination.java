package ctu.nengoros.comm.rosBackend.multiTermination;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

	protected LinkedList <Termination> orderedTerminations;
	protected Map<String, Termination> myTerminations;

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
	
	public final float DEF_W = 1;

	
	protected String mess = me+"ERROR: suport fot multidimensional Terminations" +
			"is not implemented so far!";
	
	public AbstractMultiTermination(Node parent, String name, int dimension){
		
		this.t_start=0;
		this.t_end=0;
		this.t=0;

		this.dimension = dimension;
		this.name = name;
		this.parent = parent;
		this.orderedTerminations = new LinkedList<Termination>();
		this.myTerminations = new HashMap<String, Termination>();
		
		//TODO
		if(dimension>1)
			System.err.println(mess);
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


	protected String generateName(){
		return name+"_"+counter++;
	}

	@Override
	public String addTermination(float[][] weights) throws StructuralException{
		// TODO
		this.checkDimensions(weights);
		return null;
	}


	@Override
	public String addTerminaton() throws StructuralException{
		return this.addTermination(this.DEF_W);
	}

	// TODO: add support for multidimensional terminations
	protected void checkDimensions(float[][] weights) throws StructuralException{
		
		System.err.println(mess);
		throw new StructuralException(mess);
	}
	

}
