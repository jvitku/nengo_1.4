package ctu.nengorosHeadless.simulator.test;

import org.hanns.physiology.statespace.ros.BasicMotivation;

import ca.nengo.model.StructuralException;

import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.network.modules.io.impl.BasicConnection;
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

				System.out.println("origiin null? "+(ms.getOrigin(BasicMotivation.topicDataOut)==null));
				System.out.println("term null? "+(mr.getTermination(BasicMotivation.topicDataOut)==null));
				Connection c;
					c = new BasicConnection(
							ms.getOrigin(BasicMotivation.topicDataOut),
							mr.getTermination(BasicMotivation.topicDataOut));

				this.connections.add(c);

				Connection cc = new BasicConnection(
						mr.getOrigin(BasicMotivation.topicDataIn),
						ms.getTermination(BasicMotivation.topicDataIn));

				this.connections.add(cc);

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
		System.out.println("all done, simulaiton here.. :-)");
		sim.cleanup();
	}
}
