package ctu.nengoros.test.module;

import static org.junit.Assert.fail;

import org.junit.Test;

import ca.nengo.model.Origin;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.testsuit.demo.nodes.gate.OR;

/**
 * Extension of the  ctu.nengoros.test.module.DefaultModuleTerminationsOrigins .
 * This tests how multiple Terminations can be simply added to one Encoder. 
 * This enables user to connect multiple components to one "Termination" Encoder.
 * 
 * @author Jaroslav Vitku
 *
 */
public class MultipleTerminationsOneEncoder extends NengorosTest{

	public static String ORR = "ctu.nengoros.testsuit.demo.nodes.gate.OR";
	
	@Test
	public void communicationWorks(){

		// Run ROS node computing logical OR (taken from the project logic/gates)
		String name = "myName";
		NodeGroup g = new NodeGroup("ORGROUP", true);
		g.addNode(ORR, "ORNODE", "java");
		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule(name+"_OR", g);
		} catch (ConnectionException e1) {
			e1.printStackTrace();
			fail();
		}

		// Connect Neural module to the node
		module.createEncoder(OR.inAT, "bool", 1);
		module.createEncoder(OR.inBT, "bool", 1);
		module.createDecoder(OR.outAT, "bool", 1);		

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// obtain connections from the Node (NeuralModule) (created by methods createEncoder/Decoder)
		Termination inTA=null, inTB=null;
		Origin outA=null;

		try {
			inTA = module.getTermination(OR.inAT);
			inTB = module.getTermination(OR.inBT);
		} catch (StructuralException e) {
			System.err.println("On of Terminations not found!");
			e.printStackTrace();
			fail();
		}
		System.out.println("termination called "+OR.inAT+" found");
		System.out.println("termination called "+OR.inBT+" found");

		try {
			outA = module.getOrigin(OR.outAT);
		} catch (StructuralException e) {
			System.err.println("Origin named "+OR.outAT+" not found!");
			e.printStackTrace();
			fail();
		}
		
		
	}

}
