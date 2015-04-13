package ctu.nengorosHeadless.network.modules.io.transformMultiTermination.impl;

import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.dynamics.NoIntegrator;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.Termination;
import ca.nengo.model.impl.BasicTermination;

/**
 * BBuilds terminations for the MultiTermination
 * 
 * @author Jaroslav Vitku
 *
 */
public class TerminationFactory {

	static Integrator noInt = new NoIntegrator();	// do not integrate termination values

	/**
	 * Builds BasicTermination which does not change input value.
	 * @param parent
	 * @param name
	 * @param dim
	 * @return created BasicTermination
	 */
	public static Termination buldBasicTermination(Node parent, String name, int dim){
		IdentityLTISystem noLTI = new IdentityLTISystem(dim); // TODO dynamics here 	
		
		Termination t = new BasicTermination(parent, noLTI, noInt, name);
		return t;
	}
	
	/**
	 * Builds plastic Termination with no explicit rule(s) for changing the weights.
	 * The Weights are specified in the constructor. 
	 * 
	 * @param parent
	 * @param name
	 * @param dim
	 * @return
	 *
	public static Termination buildPlasticUniformTermination(Node parent, String name, int dim,
			Float[][] weights){
		
		IdentityLTISystem noLTI = new IdentityLTISystem(dim); // TODO dynamics here
		
		Termination t = new UnchangingWeightsTermination(parent, noLTI, noInt, name);
		return null;
	}*/
}
