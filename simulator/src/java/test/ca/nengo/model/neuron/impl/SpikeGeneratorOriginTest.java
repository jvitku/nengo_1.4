package ca.nengo.model.neuron.impl;

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SimulationMode;
import ca.nengo.model.SpikeOutput;
import ca.nengo.model.Units;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.model.impl.SpikeOutputImpl;
import ca.nengo.model.neuron.Neuron;
import ca.nengo.model.neuron.SpikeGenerator;
import ca.nengo.model.neuron.impl.SpikeGeneratorOrigin;
import junit.framework.TestCase;

/**
 * Unit tests for SpikeGeneratorOrigin. 
 * 
 * @author Bryan Tripp
 */
public class SpikeGeneratorOriginTest extends TestCase {

	private SpikeGeneratorOrigin myOrigin;
	private MockSpikeGenerator myGenerator;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		myGenerator = new MockSpikeGenerator();
		myOrigin = new SpikeGeneratorOrigin(null, myGenerator);
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.SpikeGeneratorOrigin.getName()'
	 */
	public void testGetName() {
		assertEquals(Neuron.AXON, myOrigin.getName());
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.SpikeGeneratorOrigin.getDimensions()'
	 */
	public void testGetDimensions() {
		assertEquals(1, myOrigin.getDimensions());
	}

	/*
	 * Test method for 'ca.bpt.cn.model.impl.SpikeGeneratorOrigin.getValues()'
	 */
	public void testGetValues() throws SimulationException {
		myGenerator.setNextOutput(true, 1f);
		
		myOrigin.run(new float[]{0f}, new float[]{0f});
		InstantaneousOutput output = myOrigin.getValues();
		assertTrue(output instanceof SpikeOutput);
		assertEquals(1, output.getDimension());
		assertEquals(Units.SPIKES, output.getUnits());
		assertEquals(1, ((SpikeOutput) output).getValues().length);
		assertEquals(true, ((SpikeOutput) output).getValues()[0]);
		
		myGenerator.setMode(SimulationMode.CONSTANT_RATE);
		
		myOrigin.run(new float[]{0f}, new float[]{0f});
		output = myOrigin.getValues();
		assertTrue(output instanceof RealOutput);
		assertEquals(1, output.getDimension());
		assertEquals(Units.SPIKES_PER_S, output.getUnits());
		assertEquals(1, ((RealOutput) output).getValues().length);
		assertTrue(((RealOutput) output).getValues()[0] > .99f);		
	}
	
	private static class MockSpikeGenerator implements SpikeGenerator {

		private static final long serialVersionUID = 1L;
		
		private boolean myNextSpikeOutput;
		private float myNextRateOutput;
		private SimulationMode myMode = SimulationMode.DEFAULT;
		
		public void setNextOutput(boolean nextSpikeOutput, float nextRateOutput) {
			myNextSpikeOutput = nextSpikeOutput;
			myNextRateOutput = nextRateOutput;
		}
		
		public InstantaneousOutput run(float[] time, float[] current) {
			if (myMode.equals(SimulationMode.DEFAULT)) {
				return new SpikeOutputImpl(new boolean[]{myNextSpikeOutput}, Units.SPIKES, 0);
			} else {
				return new RealOutputImpl(new float[]{myNextRateOutput}, Units.SPIKES_PER_S, 0);
			}
		}

		public void reset(boolean randomize) {
			throw new RuntimeException("not implemented");
		}

		public SimulationMode getMode() {
			return myMode;
		}

		public void setMode(SimulationMode mode) {
			myMode = mode;
		}

		@Override
		public SpikeGenerator clone() throws CloneNotSupportedException {
			return (SpikeGenerator) super.clone();
		}
		
	}

}
