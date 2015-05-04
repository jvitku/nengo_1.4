package ctu.nengorosHeadless.test;


import static org.junit.Assert.*;

import org.hanns.environments.discrete.ros.GridWorldNode;
import org.hanns.physiology.statespace.ros.BasicMotivation;
import org.hanns.rl.discrete.ros.srp.QLambda;
import org.junit.Test;

import ca.nengo.model.StructuralException;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.model.transformMultiTermination.impl.BasicWeights;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.connections.Connection;
import ctu.nengorosHeadless.network.connections.InterLayerWeights;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.simulator.NodeBuilder;
import ctu.nengorosHeadless.simulator.impl.AbstractLayeredSimulator;

public class QLambdaInterLayerTest{

	@Test
	public void QLambdaTestRun() {
		QLambdaInterLayerTest t = new QLambdaInterLayerTest();

		System.out.println("instantiating the simulator");

		QLambdaTestSim sim = t.new QLambdaTestSim();	

		System.out.println("loading nodes..");

		sim.defineNetwork();
		System.out.println("starting the simulation now");

		sim.run(0, 1000);
		System.out.println("all done, reset, waiting");
		
		//sim.ms.getOrigin(name)
		
		sim.reset(false);

		System.out.println("ending the simulation");

		sim.cleanup();
	}


	public class QLambdaTestSim extends AbstractLayeredSimulator{

		// no interlayers
		public QLambdaTestSim() {
			super(3);
		}

		public static final int log = 100; 
		public static final boolean file = false;

		public NeuralModule ms, ql, gw;
		
		@Override
		public void defineNetwork() {

			try {

				// Motivation source
				ms = NodeBuilder.basicMotivationSource("motSource", 1, 0.1f, log);
				this.nodes.add(ms);

				// Q-Learning
				ql = NodeBuilder.qlambdaASM("qLambda", 2, 4, 10, log, 1, 3);
				this.nodes.add(ql);

				// GridWorld
				int[] size = new int[]{10,10};
				int[] pos = new int[]{4,4};
				int[] obstacles = new int[]{1,1,2,2,3,3,5,5,6,6,7,7,8,8};
				int[] rewards = new int[]{7,6,0,1,9,0,0,1};

				gw = NodeBuilder.gridWorld("world", log, file, size, 4, pos, obstacles, rewards);
				this.nodes.add(gw);

				//float[][] w;

				// world [r,state] ~> motivation [r]
				Connection cddd = this.connect(
						gw.getOrigin(GridWorldNode.topicDataIn),
						ms.getTermination(BasicMotivation.topicDataIn), 0);
				//w = cddd.getWeights();
				//w[0][0] = 1;			// connect only reward to the source

				// motivation [R+mot] ~> importance [i] 
				Connection c = this.connect(
						ms.getOrigin(BasicMotivation.topicDataOut),
						ql.getTermination(QLambda.topicImportance), 1);

				//w = c.getWeights();
				//w[0][0] = 1;			// connect only motivation (not reward) to the importance

				// Q-Learning [actions] ~> world [actions]
				Connection cd = this.connect(
						ql.getOrigin(QLambda.topicDataOut),
						gw.getTermination(GridWorldNode.topicDataOut), 2);
				//w = cd.getWeights();
				//BasicWeights.pseudoEye(w,1);	// one to one connections

				// world [r, state] ~> Q-learning [state]
				Connection cdd = this.connect(
						gw.getOrigin(GridWorldNode.topicDataIn),
						ql.getTermination(QLambda.topicDataIn), 0);
				//w = cdd.getWeights();
				//BasicWeights.pseudoEye(w,1);	// also one to one connections [r,x,y]

				////////////////////
				this.designFinished();
				float[][] w;
				
				w = cddd.getWeights();
				w[0][0] = 1;
				cddd.setWeights(w);
				
				w = c.getWeights();
				w[0][0] = 1;
				c.setWeights(w);
				
				w = cd.getWeights();
				BasicWeights.pseudoEye(w, 1);
				cd.setWeights(w);
				
				w = cdd.getWeights();
				BasicWeights.pseudoEye(w, 1);
				cdd.setWeights(w);
				
				this.networkDefined = true;

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


		@Override
		public float getFitnessVal() { return -1; }

	}

	/**
	 * Runs the simulation normally, without testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		QLambdaInterLayerTest t = new QLambdaInterLayerTest();

		System.out.println("instantiating the simulator");

		QLambdaTestSim sim = t.new QLambdaTestSim();

		System.out.println("loading nodes..");

		sim.defineNetwork();
		System.out.println("starting the simulation now");

		sim.run(0, 10000);
		System.out.println("all done, reset, waiting");
		sim.reset(false);
		try {
			Thread.sleep(200000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("ending the simulation");

		sim.cleanup();
	}
}

