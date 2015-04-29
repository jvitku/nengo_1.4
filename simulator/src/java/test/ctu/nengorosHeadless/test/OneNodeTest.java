package ctu.nengorosHeadless.test;

import org.hanns.physiology.statespace.ros.BasicMotivation;
import org.junit.Test;

import ca.nengo.model.StructuralException;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.simulator.NodeBuilder;
import ctu.nengorosHeadless.simulator.impl.AbstractSimulator;

import static org.junit.Assert.*;

public class OneNodeTest{

	/**
	 * Launches one node, connects its output to the input, runs the simulation.
	 */
	@Test
	public void oneNodeExample() {
		OneNodeTest t = new OneNodeTest();

		System.out.println("instantiating the simulator");

		OneNodeTestSimulation sim = t.new OneNodeTestSimulation();

		System.out.println("loading nodes..");

		sim.defineNetwork();
		System.out.println("starting the simulation now");

		sim.run(0, 100);
		System.out.println("all done, reset, waiting");
		sim.reset(false);
		System.out.println("ending the simulation");
		sim.cleanup();
	}
	
	public class OneNodeTestSimulation extends AbstractSimulator{
		
		public static final int log = 1; 
		
		/**
		 * Connect the motivation source into the loop
		 */
		@Override
		public void defineNetwork() {

			try {
				NeuralModule ms = NodeBuilder.basicMotivationSource("motSource", 1, 0.1f, log);
				this.nodes.add(ms);

				Connection c = this.connect(
						ms.getOrigin(BasicMotivation.topicDataOut),
						ms.getTermination(BasicMotivation.topicDataIn));

				float[][] w = c.getWeights();
				//BasicWeights.pseudoEye(w,0);	// motivation goes up as no reward comes 
				w[1][0] = 2;
				w[0][0] = 12;
				

			} catch (ConnectionException e) {
				e.printStackTrace();
				fail();
			} catch (StartupDelayException e) {
				e.printStackTrace();
				fail();
			} catch (StructuralException e) {
				e.printStackTrace();
				fail();
			}
		}
	}


	public static void main(String[] args) {
		OneNodeTest t = new OneNodeTest();

		System.out.println("instantiating the simulator");

		OneNodeTestSimulation sim = t.new OneNodeTestSimulation();

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

