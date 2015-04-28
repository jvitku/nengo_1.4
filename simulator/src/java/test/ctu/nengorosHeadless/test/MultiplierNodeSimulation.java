package ctu.nengorosHeadless.test;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.util.SL;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.simulator.NodeBuilder;
import ctu.nengorosHeadless.simulator.impl.AbstractSimulator;
import ctu.nengorosHeadless.simulator.test.nodes.MultiplierNode;

public class MultiplierNodeSimulation {

	
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
	
	public static final boolean SYNC= true;
	public static final float multiplyBy = 3;
	public static final int NOINPUTS = 3;

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
				/*
				Connection c = this.connect(
						ms.getOrigin(MultiplierNode.topicDataOut),
						ms.getTermination(MultiplierNode.topicDataIn));

				float[][] w = c.getWeights();
				//BasicWeights.pseudoEye(w,0);	// motivation goes up as no reward comes 
				//w[1][0] = 2;
				w[0][0] = 12;
				 */

				//ms.getTermination(MultiplierNode.topicDataIn).sendValue(10, 0);
				//ms.getTermination(MultiplierNode.topicDataIn).setValues(new float[]{11});

				// ---- data:

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

					source = this.generateVector(NOINPUTS);
					expected = this.multiplyVector(source, multiplyBy);

					ms.getTermination(MultiplierNode.topicDataIn).setValues(source);

					this.makeStep();
					t += dt;

					result = ms.getOrigin(MultiplierNode.topicDataOut).getValues();
					System.out.println(" received this value "+ SL.toStr(result));

					if(step>0){
						assertTrue(expected(result, expected));
					}
					
					/*
					if(!expected(result, expected) && step++>0){
						
						System.err.println("fasdfadsfads \n\n\n error ");
						
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}*/

				} catch (StructuralException e) {
					e.printStackTrace();
				} catch (SimulationException e) {
					e.printStackTrace();
				}	
			}
		}

		private float[] multiplyVector(float[] source, float by){
			float[] out = new float[source.length];
			for(int i=0; i<source.length; i++){
				out[i] = source[i]*by;
			}
			return out;
		}

		private float[] generateVector(int len){
			Random r = new Random();
			float[] out = new float[len];
			for(int i=0; i<out.length; i++){
				out[i] = r.nextFloat();
			}
			return out;
		}

		private boolean expected(float[] a1, float[] a2){
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
