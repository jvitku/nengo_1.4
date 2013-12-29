package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;

/**
 * Tests how the NeuralModule can be launched, how the DefaultModem is auto-started.
 * Tests include also starting and stopping the group of nodes and RosUtils.
 * 
 * @author Jaroslav Vitku
 *
 */
public class RunDefaultModule{

	public static String minimax = "ctu.nengoros.testsuit.demo.nodes.minmax.F2IPubSub";
	public static String modem = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";
	
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
	
	
	@Test
	public void runDemo(){

		NodeGroup g = new NodeGroup("pubsub",false);

		g.addNode(minimax,"minimaxNode","java");
		g.addNode(modem,"modemNode","modem");

		NeuralModule smartOne = null;
		try {
			smartOne = new DefaultNeuralModule("SmartNeuron",g);
		} catch (ConnectionException e1) {
			e1.printStackTrace();
			fail();
		}

		smartOne.createDecoder("ros2annFloatArr", "int", 2);
		smartOne.createEncoder("ann2rosFloatArr", "float", 4);

		System.out.println("Names of running nodes are: " +
				Mess.toAr(NodeFactory.np.namesOfRunningNodes()));
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) { e.printStackTrace(); }

		g.stopGroup(); 

	}

	/**
	 * Newer version also checks whether the Modem was added, 
	 * if not, it automatically ads the Default modem.
	 */
	@Test
	public void runDemoNoModem(){

		NodeGroup g = new NodeGroup("pubsub",false);
		g.addNode(minimax,"minimaxNode","java");
		
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule smartOne = null;
		try {
			smartOne = new DefaultNeuralModule("SmartNeuron",g);
		} catch (ConnectionException e1) {
			e1.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node
		
		smartOne.createDecoder("ros2annFloatArr", "int", 2);
		smartOne.createEncoder("ann2rosFloatArr", "float", 4);

		System.out.println("Names of running nodes are: " +
				Mess.toAr(NodeFactory.np.namesOfRunningNodes()));
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) { e.printStackTrace(); }
		
		g.stopGroup();
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
		
	}
	


}
