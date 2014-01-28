package ctu.nengoros.test.termination;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.dynamics.NoIntegrator;
import ctu.nengoros.model.termination.impl.BasicTransformTermination;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.util.SL;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * Test whether the Transformation works OK.
 * 
 * @author Jaroslav Vitku
 *
 */
public class BasicTransformTerminationn {

	public static Integrator noInt = new NoIntegrator();
	
	
	@Test
	public void identityTransfomr(){

		int indim = 3;
		int outdim = 3;

		float[][] weights = new float[indim][outdim];
		for(int i=0; i<weights.length; i++)
			weights[i][i] = 1;

		String name = "test";
		IdentityLTISystem noLTI = new IdentityLTISystem(indim); 

		DefaultNeuralModule node = Mockito.mock(DefaultNeuralModule.class);

		// define return value for method getUniqueId()
		//test.when(test.getUniqueId()).thenReturn(43);
		//Node node = (Node)new DefaultNeuralModule("NodeName", NodeGroup group);

		BasicTransformTermination t = null;
		try {
			t = new BasicTransformTermination((Node)node, (DynamicalSystem)noLTI, 
					noInt, outdim, name, weights);
		} catch (StructuralException e1) {
			e1.printStackTrace();
			fail();
		}
		System.out.println(t.getName());

		float[] vals = new float[]{11,22,33};
		assertTrue(indim==weights.length);
		assertTrue(outdim==weights[0].length);

		InstantaneousOutput o = new RealOutputImpl(vals, Units.UNK, 0);

		try {	// set values on the termination
			t.setValues(o);
		} catch (SimulationException e) { 
			e.printStackTrace();
			fail();
		}
		try {	// simulate termination
			t.run(0, 1);
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		TimeSeriesImpl tsi = (TimeSeriesImpl) t.getOutput();
		System.out.println("Time Series Impl values are: \n"+SL.toStr(tsi.getValues()));
		assertTrue(tsi.getDimension()==outdim);

		// read output, there should be no dynamics, so all time samples have the same values
		for(int i=0; i<vals.length; i++)
			assertTrue(this.allEqual(tsi.getValues(),i, vals[i]));
	}

	@Test
	public void smallToBig(){

		int indim = 3;
		int outdim = 4;

		float[][] weights = new float[][]{{1,2,0,3},{0,1,0,3},{0,0,1,0}};
		System.out.println("just some transformation matrix \n"+SL.toStr(weights));
		
		assertTrue(indim==weights.length);
		assertTrue(outdim==weights[0].length);
		
		String name = "test2";
		IdentityLTISystem noLTI = new IdentityLTISystem(indim); 

		DefaultNeuralModule node = Mockito.mock(DefaultNeuralModule.class);

		BasicTransformTermination t = null;
		try {
			t = new BasicTransformTermination((Node)node, (DynamicalSystem)noLTI, 
					noInt, outdim, name, weights);
		} catch (StructuralException e1) {
			e1.printStackTrace();
			fail();
		}
		System.out.println(t.getName());


		float[] vals = new float[]{11,22,33};			// i
		float[] output = new float[]{11,44,33,99};		// o

		InstantaneousOutput o = new RealOutputImpl(vals, Units.UNK, 0);	// to be passed to the Term.

		try {	// set values on the termination
			t.setValues(o);
		} catch (SimulationException e) { 
			e.printStackTrace();
			fail();
		}
		try {	// simulate termination
			t.run(0, 1);
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		TimeSeriesImpl tsi = (TimeSeriesImpl) t.getOutput();
		System.out.println("Time Series Impl values are: \n"+SL.toStr(tsi.getValues()));

		assertTrue(tsi.getDimension()==outdim);
		
		// read output, there should be no dynamics, so all time samples have the same values
		for(int i=0; i<output.length; i++)
			assertTrue(this.allEqual(tsi.getValues(), i, output[i]));
	}
	@Test
	public void bigToSmall(){

		int indim = 4;
		int outdim = 3;
		
		float[][] weights = new float[][]{{1,2,0,3},{0,1,0,3},{0,0,1,0}};
		float[][]w2 = new float[indim][outdim];	// transpose the one above
		for(int i=0; i<w2.length; i++){
			for( int j=0; j<w2[0].length; j++){
				w2[i][j] = weights[j][i]; 
			}
		}
		System.out.println("just some transformation matrix \n"+SL.toStr(w2));
		
		assertTrue(indim==w2.length);
		assertTrue(outdim==w2[0].length);
		
		String name = "test3";
		IdentityLTISystem noLTI = new IdentityLTISystem(indim); 

		DefaultNeuralModule node = Mockito.mock(DefaultNeuralModule.class);

		BasicTransformTermination t = null;
		try {
			t = new BasicTransformTermination((Node)node, (DynamicalSystem)noLTI, 
					noInt, outdim, name, w2);
		} catch (StructuralException e1) {
			e1.printStackTrace();
			fail();
		}
		System.out.println(t.getName());
		
		float[] vals = new float[]{11,22,33,44};			// i
		float[] output = new float[]{187,154,33};		// o

		InstantaneousOutput o = new RealOutputImpl(vals, Units.UNK, 0); // pass there previous output

		try {	// set values on the termination
			t.setValues(o);
		} catch (SimulationException e) { 
			e.printStackTrace();
			fail();
		}
		try {	// simulate termination
			t.run(0, 1);
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		TimeSeriesImpl tsi = (TimeSeriesImpl) t.getOutput();
		System.out.println("Time Series Impl values are: \n"+SL.toStr(tsi.getValues()));

		assertTrue(tsi.getDimension()==outdim);
		
		// read output, there should be no dynamics, so all time samples have the same values
		for(int i=0; i<output.length; i++)
			assertTrue(this.allEqual(tsi.getValues(), i, output[i]));
	}
	
	/**
	 * Return true if value (for all time samples) equals to a given value 
	 * @param values array of values read on the Termination, this is: float[timeSamples][dimension]
	 * @param dimension dimension to check the value in
	 * @param value value to be found in the dimension for all time samples
	 * @return true if the values equal
	 */
	private boolean allEqual(float[][] values, int dimension, float value){
		if(dimension>values[0].length){
			System.err.println("dimension out of range");
			return false;
		}
		for(int i=0; i<values.length; i++){
			if(values[i][dimension] != value)
				return false;
		}
		return true;
	}

}
