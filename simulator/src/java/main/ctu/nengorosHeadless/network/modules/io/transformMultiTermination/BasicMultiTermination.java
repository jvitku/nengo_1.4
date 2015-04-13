package ctu.nengorosHeadless.network.modules.io.transformMultiTermination;

import ctu.nengoros.modules.PeripheralsRegisteringNode;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.TransformTermination;
import ctu.nengorosHeadless.network.modules.io.impl.BasicTransformTermination;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;

/**
 * MultiTermination which supports only BasicTransformTerminations.
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class BasicMultiTermination extends AbstractTransformMultiTermination {

	public final String me = "[BasicMultiTerminationSImple] ";

	public BasicMultiTermination(NeuralModule parent, String name, int outputDimension) {

		super(parent, name, outputDimension);
	}

	/**
	 * Adds BasicTerminaiton of dimension 1 with auto-generated name. By default, the first Termination
	 * is added in the constructor. The Encoder can be created as configEncoder ({@link }, so that 
	 * output of the Termination is set to default values. Calling this method causes that the
	 * default values for the Termination 0 are set back to zero.
	 * 
	 * @param weight matrix for newly created termination, determines the terminations dimensionality
	 * @return name of newly created Termination
	 * @throws StructuralException if output dimension of weight matrix is incorrect
	 */
	@Override
	public Termination addTermination(float [][] weights) throws StructuralException {
		super.checkDimensions(weights);

		int dim = weights.length;

		String termName = this.generateName(); 

		Termination t = new BasicTransformTermination(parent, termName, weights);
		((PeripheralsRegisteringNode) parent).addTermination(t);

		// if a default value is set to the Termination 0, discard it now
		if(this.orderedTerminations.size()>0)
			((TransformTermination)this.orderedTerminations.get(0)).resetDefaultOutputValues();
		
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
