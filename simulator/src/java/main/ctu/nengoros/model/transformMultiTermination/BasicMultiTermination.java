package ctu.nengoros.model.transformMultiTermination;

import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.model.termination.impl.BasicTransformTermination;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.PeripheralsRegisteringNode;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.impl.BasicTermination;

/**
 * MultiTermination which supports only BasicTransformTerminations.
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class BasicMultiTermination extends AbstractTransformMultiTermination {

	private static final long serialVersionUID = 7943836551919849111L;
	public final String me = "[BasicMultiTermination] ";

	public BasicMultiTermination(NeuralModule parent, String name, Integrator integ, int outputDimension) {

		super(parent, name, integ, outputDimension);
	}

	/**
	 * Adds BasicTerminaiton of dimension 1 with auto-generated name. 
	 * @param weight matrix for newly created termination, determines the terminations dimensionality
	 * @return name of newly created Termination
	 * @throws StructuralException if output dimension of weight matrix is incorrect
	 */
	@Override
	public Termination addTermination(float [][] weights) throws StructuralException {
		super.checkDimensions(weights);

		int dim = weights.length;
		IdentityLTISystem noLTI = new IdentityLTISystem(dim);

		String termName = this.generateName(); 

		Termination t = new BasicTransformTermination(parent, noLTI, integ, termName, weights);
		((PeripheralsRegisteringNode) parent).addTermination(t);

		this.myTerminations.put(termName, t);
		this.orderedTerminations.add(t);

		return t;
	}

	@Override
	protected void runAllTerminations(float startTime, float endTime) throws SimulationException {
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
		if(!((t instanceof BasicTermination) || (t instanceof BasicTransformTermination))){
			String message = me+"ERROR: only BasicTerminations and BasicTransformTermination are supported!";
			System.err.println(message);
			throw new SimulationException(message);
		}
	}
}
