package ctu.nengorosHeadless.simulator.test;

import org.hanns.environments.discrete.ros.GridWorldNode;
import org.hanns.physiology.statespace.ros.BasicMotivation;
import org.hanns.rl.discrete.ros.srp.QLambda;

import ca.nengo.model.StructuralException;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.model.transformMultiTermination.impl.BasicWeights;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.simulator.NodeBuilder;
import ctu.nengorosHeadless.simulator.impl.AbstractSimulator;

public class QLambdaTest{

	public class QLambdaTestSim extends AbstractSimulator{

		public static final int log = 1; 
		public static final boolean file = false;

		@Override
		public void defineNetwork() {

			try {

				// Motivation source
				NeuralModule ms = NodeBuilder.basicMotivationSource("motSource", 1, 0.1f, log);
				this.nodes.add(ms);

				// Q-Learning
				NeuralModule ql = NodeBuilder.qlambdaASM("qLambda", 2, 4, 10, log, 1, 3);
				this.nodes.add(ql);

				// GridWorld
				int[] size = new int[]{10,10};
				int[] pos = new int[]{4,4};
				int[] obstacles = new int[]{1,1,2,2,3,3,5,5,6,6,7,7,8,8};
				NeuralModule gw = NodeBuilder.gridWorld("world", log, file, size, 4, pos, obstacles);
				this.nodes.add(gw);
				
				float[][] w;

				// motivation [R+mot] ~> importance [i] 
				Connection c = this.connect(
						ms.getOrigin(BasicMotivation.topicDataOut),
						ql.getTermination(QLambda.topicImportance));

				w = c.getWeights();
				w[1][0] = 1;

				// Q-Learing [actions] ~> world [actions]
				Connection cd = this.connect(
						ql.getOrigin(QLambda.topicDataOut),
						gw.getTermination(GridWorldNode.topicDataOut));
				w = cd.getWeights();
				BasicWeights.pseudoEye(w,1);
				
				// world [r, state] ~> Q-learning [state]
				Connection cdd = this.connect(
						gw.getOrigin(GridWorldNode.topicDataIn),
						ql.getTermination(QLambda.topicDataIn));
				w = cdd.getWeights();
				BasicWeights.pseudoEye(w,1);
				
				// world [r,state] ~> motivation [r]
				Connection cddd = this.connect(
						gw.getOrigin(GridWorldNode.topicDataIn),
						ms.getTermination(BasicMotivation.topicDataIn));
				w = cddd.getWeights();
				BasicWeights.pseudoEye(w,1);

			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (StartupDelayException e) {
				e.printStackTrace();
			} catch (StructuralException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		QLambdaTest t = new QLambdaTest();

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
