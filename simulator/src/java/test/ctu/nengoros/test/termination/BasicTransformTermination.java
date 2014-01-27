package ctu.nengoros.test.termination;

import org.junit.Test;
import org.mockito.Mockito;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.dynamics.NoIntegrator;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.Termination;

/**
 * Test whether the Transformation works OK.
 * 
 * @author Jaroslav Vitku
 *
 */
public class BasicTransformTermination {

	@Test
	public void transfomr(){
		
		int indim = 3;
		int outdim = 5;
		
		float[][] weights = new float[indim][outdim];
		
		String name = "test";
		
		Integrator noInt = new NoIntegrator();
		IdentityLTISystem noLTI = new IdentityLTISystem(indim); 
		
		 DefaultNeuralModule test = Mockito.mock(DefaultNeuralModule.class);
		 
		  // define return value for method getUniqueId()
		 test.when(test.getUniqueId()).thenReturn(43);
		  
		//Node node = (Node)new DefaultNeuralModule("NodeName", NodeGroup group);
		
		
		BasicTransformTermination t = new BasicTransformTermination(node, (DynamicalSystem)noLTI, 
				noInt, outdim, name, weights);
		
	}
	
}
