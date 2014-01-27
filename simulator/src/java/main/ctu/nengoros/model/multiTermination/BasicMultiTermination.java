package ctu.nengoros.model.multiTermination;

import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.PeripheralsRegisteringNode;
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

	public BasicMultiTermination(NeuralModule parent, String name, 
			Integrator integ, DynamicalSystem lti) {

		super(parent, name, integ, lti);
	}
	
	/**
	 * Adds BasicTerminaiton of dimension 1 with auto-generated name. 
	 * @param weight name of newly created termination
	 * @return name of newly created Termination
	 * @throws StructuralException 
	 */
	@Override
	public Termination addTermination(Float [] weights) throws StructuralException {

		super.checkDimensions(weights);	// TODO check this
		String termName = this.generateName(); 

		Termination t = new BasicTermination(parent, lti, integ, termName);
		((PeripheralsRegisteringNode) parent).addTermination(t);
		
		Float[][] w = new Float[weights.length][1];
		for(int i=0; i<weights.length; i++)
			w[i][0] = weights[i];
		
		this.myWeights.put(t.getName(), w);
		this.myTerminations.put(termName, t);
		this.orderedTerminations.add(t);

		return t;
	}
	
	/**
	 * Adds BasicTerminaiton of dimension 1 with auto-generated name. 
	 * @param weight name of newly created termination
	 * @return name of newly created Termination
	 * @throws StructuralException 
	 */
	@Override
	public Termination addTermination(Float [][] weights) throws StructuralException {

		super.checkDimensions(weights);
		String termName = this.generateName(); 

		Termination t = new BasicTermination(parent, lti, integ, termName);
		((PeripheralsRegisteringNode) parent).addTermination(t);

		this.myWeights.put(t.getName(), weights.clone());//TODO use 
		this.myTerminations.put(termName, t);
		this.orderedTerminations.add(t);

		return t;
	}

	@Override
	protected void runAllTerminations(float startTime, float endTime)
			throws SimulationException {
		for(int i=0; i<this.orderedTerminations.size(); i++){

			Termination t = this.orderedTerminations.get(i);

			this.checkInstance(t);

			BasicTermination tt = ((BasicTermination)t);

			// do not run terminations which have not value set?
			//if(tt.getInput() != null)
			tt.run(startTime, endTime);
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
