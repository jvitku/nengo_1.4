package ctu.nengoros.comm.rosBackend.encoders.multiTermination;

import ctu.nengoros.modules.NeuralModule;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.impl.BasicTermination;

/**
 * MultiTermination which supports only BasicTerminations.
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class BasicMultiTermination extends AbstractMultiTermination {

	private static final long serialVersionUID = 7943836551919849111L;
	public final String me = "[BasicMultiTermination] ";

	public BasicMultiTermination(NeuralModule parent, String name, int dimension, Integrator integ, DynamicalSystem lti) {
		super(parent, name, dimension, integ, lti);
	}

	/**
	 * Adds BasicTerminaiton of dimension 1 with auto-generated name. 
	 * @param weight name of newly created termination
	 * @return name of newly created Termination
	 * @throws StructuralException 
	 */
	@Override
	public String addTermination(float weight) throws StructuralException {

		// TODO: add this weight to all dimensions of multi-dimensional input
		/*
		if(dimension != 1){
			String mess = me+"ERROR: my dimension is "+dimension+
					", now only one-dimensional Terminations are supported";
			
			System.err.println(mess);
			throw new StructuralException(mess);
		}*/

		String termName = this.generateName();
		//Termination t = TerminationFactory.buldBasicTermination(parent, termName, this.dimension);
		
		Termination t = new BasicTermination(parent, lti, integ, termName);
		
		this.myTerminations.put(termName, t);
		this.orderedTerminations.add(t);
		return name;
	}

	@Override
	protected void runAllTerminations(float startTime, float endTime)
			throws SimulationException {
		for(int i=0; i<this.orderedTerminations.size(); i++){

			Termination t = this.orderedTerminations.get(i);

			this.checkInstance(t);
			((BasicTermination)t).run(startTime, endTime);
		}
	}

	protected void checkInstance(Termination t) throws SimulationException{
		if(!(t instanceof BasicTermination)){
			String message = me+"ERROR: only BasicTerminations are supported!";
			System.err.println(message);
			throw new SimulationException(message);
		}
	}
}
