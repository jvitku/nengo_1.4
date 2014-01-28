package ctu.nengoros.test.termination;

import org.junit.Test;
import org.mockito.Mockito;

import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.dynamics.NoIntegrator;
import ctu.nengoros.model.termination.impl.BasicTransformTermination;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;

/**
 * Test whether the Transformation works OK.
 * 
 * @author Jaroslav Vitku
 *
 */
public class BasicTransformTerminationn {

	//@Mock
	
	
	@Test
	public void transfomr(){
		
		int indim = 3;
		int outdim = 5;
		
		float[][] weights = new float[indim][outdim];
		
		String name = "test";
		
		Integrator noInt = new NoIntegrator();
		IdentityLTISystem noLTI = new IdentityLTISystem(indim); 
		
		 DefaultNeuralModule node = Mockito.mock(DefaultNeuralModule.class);
		 
		 
		  // define return value for method getUniqueId()
		 //test.when(test.getUniqueId()).thenReturn(43);
		  
		//Node node = (Node)new DefaultNeuralModule("NodeName", NodeGroup group);
		
		
		BasicTransformTermination t = new BasicTransformTermination((Node)node, (DynamicalSystem)noLTI, 
				noInt, outdim, name, weights);
		System.out.println(t.getName());
	}
	
}
