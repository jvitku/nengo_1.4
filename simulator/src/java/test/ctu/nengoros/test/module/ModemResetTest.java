package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ctu.nengoros.RosRunner;
import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.network.node.infrastructure.simulation.testnodes.ConfigurableHannsNode;

/**
 * Tests how the NeuralModule can use the modem to send message to ROS nodes
 * to reset itself.
 * 
 * @author Jaroslav Vitku
 *
 */
public class ModemResetTest extends NengorosTest{

	
	public static String tst = "ctu.nengoros.network.node.infrastructure.simulation.testnodes.ConfigurableHannsNode";
	
	//public static String modem = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";
	
	@BeforeClass
	public static void before(){
		// use JRoscore if also roscore found
		RosUtils.prefferJroscore(true);		
	}
	
	@AfterClass
	public static void after(){
		// called during exiting of the Nengo application
		RosUtils.utilsShallStop();
	}
	
	//@Ignore
	@Test
	public void testReset(){

		runners = new ArrayList<RosRunner>();
		
		NodeGroup g = new NodeGroup("resetTest",false);
		// just a dummy ROS node in a namespace
		g.addNode(tst,"resettableTestNode","java");

		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule("NeuralModule",g);
			module.awaitStarted();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node
		
		RosRunner rr = this.runNode(new String[]{tst});// default namespace is none, turns out..
		assertTrue(rr.isRunning());
		
		assertTrue(rr.getNode() instanceof ConfigurableHannsNode); // TODO multiple definitions of this?
		ConfigurableHannsNode node = (ConfigurableHannsNode)rr.getNode();
		
		// wait for rr to be started and ROS communication initialized (will send messages immediately)
		sleep(300);	
		
		assertFalse(node.softResetted);
		assertFalse(node.hardResetted);
		
		module.setShouldResetNodes(true);	// should be true by default anyway..
		module.reset(true);					// publish the reset command
		
		sleep(300);							// wait for deliver
		assertFalse(node.softResetted);	
		assertTrue(node.hardResetted);		// by default, the hardReset is called
		
		rr.stop();							// stop the ROS node
		g.stopGroup();						// stop the module
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
	}
	
	@Test
	public void testResetFail(){

		runners = new ArrayList<RosRunner>();
		
		NodeGroup g = new NodeGroup("resetTest",false);
		// just a dummy ROS node in a namespace
		g.addNode(tst,"resettableTestNode","java");

		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule("NeuralModule",g);
			module.awaitStarted();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node
		
		RosRunner rr = this.runNode(new String[]{tst,"__ns:=resetTestWrongNS"}); // "_ns:=resetTestWrongNS"		
		assertTrue(rr.isRunning());
		
		assertTrue(rr.getNode() instanceof ConfigurableHannsNode); // TODO multiple definitions of this?
		ConfigurableHannsNode node = (ConfigurableHannsNode)rr.getNode();
		
		// wait for rr to be started and ROS communication initialized (will send messages immediately)
		sleep(300);	
		
		assertFalse(node.softResetted);
		assertFalse(node.hardResetted);
		
		module.setShouldResetNodes(true);	// should be true by default anyway..
		module.reset(true);					// publish the reset command
		
		sleep(300);							// wait for deliver
		assertFalse(node.softResetted);	
		// here, the ROS node should be in different namespace
		//assertTrue(node.hardResetted);
		assertFalse(node.hardResetted);		
		
		rr.stop();							// stop the ROS node
		g.stopGroup();						// stop the module
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
	}
	
	private static ArrayList<RosRunner> runners;
	
	/**
	 * Run a given node and check if it is running.
	 * 
	 * @param command complete node name and command line parameters
	 * @return RosRunner instance with running node
	 */
	public RosRunner runNode(String[] command){
		RosRunner rr = null;

		try {
			rr = new RosRunner(command);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Node named: "+command[0]+" could not be launched..");
		}
		assertFalse(rr.isRunning());
		rr.start();
		assertTrue(rr.isRunning());
		runners.add(rr);			// add runner to the list of runners
		return rr;
	}
	
	public RosRunner runNode(String which){
		return runNode(new String[]{which});
	}

	public void sleep(int howlong){
		try {
			Thread.sleep(howlong);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("could not sleep");
		}
	}
	


}
