package ctu.nengoros.test.module.components;

import static org.junit.Assert.*;

import org.junit.Test;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.test.module.NengorosTest;

public class SyncedStartFail extends NengorosTest {
	
	public static String minimax = "ctu.nengoros.testsuit.demo.nodes.minmax.F2IPubSub";
	/**
	 * Core is not running -> NeuralModule.awaitStarted() should throw..
	 */
	@Test
	public void coreNotRunningSimple(){
		
		RosUtils.setAutorun(false); 		// do not start the core automatically
		
		// Run ROS node computing logical OR (taken from the project logic/gates)
		String name = "myName";
		NodeGroup g = new NodeGroup("MINMAXGROUP", true);
		g.addNode(minimax, "MINMAX", "java");
		try {
			@SuppressWarnings("unused")
			NeuralModule module = new DefaultNeuralModule(name+"_MM", g);
			fail();
		} catch (ConnectionException e1) {
			System.out.println("\n\n\n\n\n ===================================== connection exception\n\n");
			e1.printStackTrace();
			fail();
		} catch (StartupDelayException e) {
			System.out.println("\n\n\n\n\n ===================================== delay exception\n\n");
			e.printStackTrace();
			System.out.println("waited too long to module to start, probably modem not inited");
		}
	}

}
