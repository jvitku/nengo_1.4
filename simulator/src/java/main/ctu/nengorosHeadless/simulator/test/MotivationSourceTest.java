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

		public static final int log = 100; 

		@Override
		public void defineNetwork() {

			try {
				
				NeuralModule ms = NodeBuilder.buildBasicMotivationSource("motSource", 2, 0.1f, 10, true);
				this.nodes.add(ms);

				NeuralModule mr = NodeBuilder.buildBasicMotivationReceiver("motReceiver", true);
				this.nodes.add(mr);

				
				Connection c = new BasicConnection(
						ms.getOrigin(BasicMotivation.topicDataOut),
						mr.getTermination(BasicMotivation.topicDataIn));

				this.connections.add(c);

				Connection cc = new BasicConnection(
						mr.getOrigin(BasicMotivation.topicDataOut),
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
		
		System.out.println("all done, simulaiton here.. :-)");
	}
}
