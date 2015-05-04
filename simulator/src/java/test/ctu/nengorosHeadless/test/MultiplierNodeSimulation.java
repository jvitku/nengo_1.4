package ctu.nengorosHeadless.test;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.model.transformMultiTermination.impl.BasicWeights;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.util.SL;
import ctu.nengorosHeadless.network.connections.Connection;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.simulator.NodeBuilder;
import ctu.nengorosHeadless.simulator.impl.AbstractSimulator;
import ctu.nengorosHeadless.simulator.test.nodes.MultiplierNode;

public class MultiplierNodeSimulation {

	public static final boolean SYNC= true;
	public static final float multiplyBy = 3;
	public static final float multiplyBy2 = 3;
	public static final int NOINPUTS = 3;

	/**
	 * Run the simulation with the multiplier node:
	 * -generate random vectors
	 * -send data to be multiplied by selected value
	 * -check results
	 * 
	 * If this test passes, the communication is synchronized and correct
	 */
	//@Ignore
	@Test
	public void synchronizedCommunicationTest(){
		MultiplierNodeSimulation t = new MultiplierNodeSimulation();

		System.out.println("instantiating the simulator");

		MultiplierNodeSimulationX sim = t.new MultiplierNodeSimulationX();

		System.out.println("loading nodes..");

		sim.defineNetwork();
		System.out.println("starting the simulation now");

		sim.run(0, 100);

		System.out.println("ending the simulation");
		sim.cleanup();
	}


	/**
	 * This generates random vector, passes it through two multiplier nodes
	 * (each multiplies by something else) and checks the result. 
	 * 
	 * The weighted connection is used for transferring the data (eye matrix).
	 */
	@Ignore
	@Test
	public void simulatorCommunicationTest(){
		MultiplierNodeSimulation t = new MultiplierNodeSimulation();

		System.out.println("instantiating the simulator");

		MultiplierNodeSimulationY sim = t.new MultiplierNodeSimulationY();

		System.out.println("loading nodes..");

		sim.defineNetwork();
		System.out.println("starting the simulation now");

		sim.run(0, 100);

		System.out.println("ending the simulation");
		sim.cleanup();
	}

	/**
	 * The same as the simulatorCommunicationTest, but this uses weight w=0.5 between the nodes.
	 */
	@Ignore
	@Test
	public void simulatorWeightedCommunicationTest(){
		MultiplierNodeSimulation t = new MultiplierNodeSimulation();

		System.out.println("instantiating the simulator");

		MultiplierNodeSimulationZ sim = t.new MultiplierNodeSimulationZ();

		System.out.println("loading nodes..");

		sim.defineNetwork();
		System.out.println("starting the simulation now");

		sim.run(0, 100);

		System.out.println("ending the simulation");
		sim.cleanup();
	}


	public class MultiplierNodeSimulationZ extends MultiplierNodeSimulationY{

		public static final float WEIGHT = 0.5f;

		@Override
		public void defineNetwork() {

			try {
				ms = NodeBuilder.multiplierNode("mul", NOINPUTS, log, multiplyBy);
				this.nodes.add(ms);

				ms2 = NodeBuilder.multiplierNode("mul", NOINPUTS, log, multiplyBy2);
				this.nodes.add(ms2);

				Connection c = this.connect(
						ms.getOrigin(MultiplierNode.topicDataOut),
						ms2.getTermination(MultiplierNode.topicDataIn),0);

				float[][] w = c.getWeights();

				BasicWeights.pseudoEye(w,WEIGHT);		 

			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (StartupDelayException e) {
				e.printStackTrace();
			} catch (StructuralException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run(float from, float to){

			this.prepareForSimulaiton();
			float[] source = new float[NOINPUTS];
			float[] expected = new float[NOINPUTS];
			float[] result = new float[NOINPUTS];

			t = from;
			int step = 0;

			while(t<=to){
				try {

					// set values
					source = generateVector(NOINPUTS);
					expected = multiplyVector(source, multiplyBy);

					ms.getTermination(MultiplierNode.topicDataIn).setValues(source);

					////////////// first step (multiplied once)
					this.makeStep();
					t += dt;

					result = ms.getOrigin(MultiplierNode.topicDataOut).getValues();
					if(step>0){
						assertTrue(expected(result, expected));
					}

					////////////// second step (multiplied once again by something else)
					this.makeStep();
					t += dt;

					expected = multiplyVector(expected, multiplyBy2);	// multiplied again
					expected = multiplyVector(expected, WEIGHT);		// weighted connection
					result = ms2.getOrigin(MultiplierNode.topicDataOut).getValues();
					if(step>0){
						assertTrue(expected(result, expected));
					}
					step++;

				} catch (StructuralException e) {
					e.printStackTrace();
				} catch (SimulationException e) {
					e.printStackTrace();
				}	
			}
		}
	}



	public class MultiplierNodeSimulationY extends AbstractSimulator{

		public static final int log = 1; 

		protected NeuralModule ms, ms2;

		/**
		 * Connect the motivation source into the loop
		 */
		@Override
		public void defineNetwork() {

			try {

				ms = NodeBuilder.multiplierNode("mul", NOINPUTS, log, multiplyBy);
				this.nodes.add(ms);

				ms2 = NodeBuilder.multiplierNode("mul", NOINPUTS, log, multiplyBy2);
				this.nodes.add(ms2);

				Connection c = this.connect(
						ms.getOrigin(MultiplierNode.topicDataOut),
						ms2.getTermination(MultiplierNode.topicDataIn), 0);

				float[][] w = c.getWeights();
				BasicWeights.pseudoEye(w,1);	// should just pass the data further	 

			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (StartupDelayException e) {
				e.printStackTrace();
			} catch (StructuralException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run(float from, float to){

			this.prepareForSimulaiton();
			float[] source = new float[NOINPUTS];
			float[] expected = new float[NOINPUTS];
			float[] result = new float[NOINPUTS];

			t = from;
			int step = 0;

			while(t<=to){
				try {

					// set values
					source = generateVector(NOINPUTS);
					expected = multiplyVector(source, multiplyBy);

					ms.getTermination(MultiplierNode.topicDataIn).setValues(source);

					////////////// first step (multiplied once)
					this.makeStep();
					t += dt;

					result = ms.getOrigin(MultiplierNode.topicDataOut).getValues();
					if(step>0){
						assertTrue(expected(result, expected));
					}

					////////////// second step (multiplied once again by something else)
					this.makeStep();
					t += dt;

					expected = multiplyVector(expected, multiplyBy2);	// multiplied again
					result = ms2.getOrigin(MultiplierNode.topicDataOut).getValues();
					if(step>0){
						assertTrue(expected(result, expected));
					}
					step++;

				} catch (StructuralException e) {
					e.printStackTrace();
				} catch (SimulationException e) {
					e.printStackTrace();
				}	
			}
		}
	}

	public class MultiplierNodeSimulationX extends AbstractSimulator{

		public static final int log = 1; 

		private NeuralModule ms;

		/**
		 * Connect the motivation source into the loop
		 */
		@Override
		public void defineNetwork() {

			try {

				ms = NodeBuilder.multiplierNode("mul", NOINPUTS, log, multiplyBy);
				this.nodes.add(ms);

			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (StartupDelayException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run(float from, float to){

			this.prepareForSimulaiton();
			float[] source, expected, result;

			t = from;
			int step = 0;

			while(t<=to){
				try {

					source = generateVector(NOINPUTS);
					expected = multiplyVector(source, multiplyBy);

					ms.getTermination(MultiplierNode.topicDataIn).setValues(source);

					this.makeStep();
					t += dt;

					result = ms.getOrigin(MultiplierNode.topicDataOut).getValues();
					System.out.println(" received this value "+ SL.toStr(result));

					if(step>0){
						assertTrue(expected(result, expected));
					}
				} catch (StructuralException e) {
					e.printStackTrace();
				} catch (SimulationException e) {
					e.printStackTrace();
				}	
			}
		}
	}

	public static float[] multiplyVector(float[] source, float by){
		float[] out = new float[source.length];
		for(int i=0; i<source.length; i++){
			out[i] = source[i]*by;
		}
		return out;
	}

	public static float[] generateVector(int len){
		Random r = new Random();
		float[] out = new float[len];
		for(int i=0; i<out.length; i++){
			out[i] = r.nextFloat();
		}
		return out;
	}

	public static boolean expected(float[] a1, float[] a2){
		if(a1.length != a2.length){
			return false;
		}
		for(int i=0; i<a1.length; i++){
			if(a1[i] != a2[i]){
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		MultiplierNodeSimulation t = new MultiplierNodeSimulation();

		System.out.println("instantiating the simulator");

		MultiplierNodeSimulationX sim = t.new MultiplierNodeSimulationX();

		System.out.println("loading nodes..");

		sim.defineNetwork();
		System.out.println("starting the simulation now");

		sim.run(0, 100);
		System.out.println("all done, reset, waiting");
		sim.reset(false);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("ending the simulation");

		sim.cleanup();
	}
}
