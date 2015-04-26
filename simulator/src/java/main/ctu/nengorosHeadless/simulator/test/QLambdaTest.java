package ctu.nengorosHeadless.simulator.test;

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

				NeuralModule ms = NodeBuilder.basicMotivationSource("motSource", 1, 0.1f, log);
				this.nodes.add(ms);

				//NeuralModule mr = NodeBuilder.buildBasicMotivationReceiver("motReceiver", log, true);
				NeuralModule ql = NodeBuilder.qlambdaASM("qLambda", 2, 4, 10, log, 1, 3);
				this.nodes.add(ql);

				int[] size = new int[]{10,10};
				int[] pos = new int[]{4,4};
				int[] obstacles = new int[]{1,1,2,2,3,3,5,5,6,6,7,7,8,8};
				NeuralModule gw = NodeBuilder.gridWorld("world", log, file, size, 4, pos, obstacles);
				this.nodes.add(gw);
				
				// motivation ~> importance
				Connection c = this.connect(
						ms.getOrigin(BasicMotivation.topicDataOut),
						ql.getTermination(QLambda.topicImportance));

				float[][] w = c.getWeights();
				BasicWeights.pseudoEye(w,1);

				// actions ~> world
				Connection cd = this.connect(
						ql.getOrigin(QLambda.topicDataOut),
						gw.getTermination(QLambda.topicDataOut));
				w = cd.getWeights();
				BasicWeights.pseudoEye(w,1);
				
				// world ~> qlearning
				Connection cdd = this.connect(
						gw.getOrigin(QLambda.topicDataIn),
						ql.getTermination(QLambda.topicDataIn));
				w = cdd.getWeights();
				BasicWeights.pseudoEye(w,1);
				/*
				// world ~> motivation
				Connection cddd = this.connect(
						ql.getOrigin(GridWorldNode.topicDataOut),
						gw.getTermination(QLambda.topicDataIn));
				w = cddd.getWeights();
				BasicWeights.pseudoEye(w,1);
*/
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

		sim.run(0, 10);
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
