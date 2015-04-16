package ctu.nengorosHeadless.simulator.test;

import org.hanns.physiology.statespace.ros.BasicMotivation;

import ca.nengo.model.StructuralException;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.model.transformMultiTermination.impl.BasicWeights;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.simulator.NodeBuilder;
import ctu.nengorosHeadless.simulator.impl.AbstractSimulator;

public class MotivationSourceTest{

	public class MotivationSourceTestSimulation extends AbstractSimulator{

		public static final int log = 1; 

		@Override
		public void defineNetwork() {

			try {
				
				NeuralModule ms = NodeBuilder.buildBasicMotivationSource("motSource", 1, 0.1f, log, true);
				this.nodes.add(ms);

				NeuralModule mr = NodeBuilder.buildBasicMotivationReceiver("motReceiver", log, true);
				this.nodes.add(mr);

				Connection c = this.connect(
							ms.getOrigin(BasicMotivation.topicDataOut),
							mr.getTermination(BasicMotivation.topicDataOut));
				
				float[][] w = c.getWeights();
				BasicWeights.pseudoEye(w,1);
				
				Connection cd = this.connect(
						mr.getOrigin(BasicMotivation.topicDataIn),
						ms.getTermination(BasicMotivation.topicDataIn));
				w = cd.getWeights();
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
    	MotivationSourceTest t = new MotivationSourceTest();
    	
		System.out.println("instantiating the simulator");
		
		MotivationSourceTestSimulation sim = t.new MotivationSourceTestSimulation();
		
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

