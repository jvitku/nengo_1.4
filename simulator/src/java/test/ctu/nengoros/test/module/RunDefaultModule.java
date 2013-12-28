package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import org.junit.Test;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;

/**
 * Tests how the NeuralModule can be launched, how the DefaultModem is auto-started.
 * Tests include also starting and stopping the group of nodes and RosUtils.
 * 
 * Note that the RosCommunicationTest does the testing faster.
 * 
 * @author Jaroslav Vitku
 *
 */
public class RunDefaultModule /*extends RosCommunicationTest*/ {

	public static String minimax = "resender.mpt.F2IPubSub";
	public static String modem = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";
	@Test
	public void runDemo(){

		// use JRoscore if also roscore found
		RosUtils.prefferJroscore(true);

		NodeGroup g = new NodeGroup("pubsub",false);

		g.addNode(minimax,"minimaxNode","java");
		g.addNode(modem,"modemNode","modem");

		NeuralModule smartOne = new DefaultNeuralModule("SmartNeuron",g);

		smartOne.createDecoder("ros2annFloatArr", "int", 2);
		smartOne.createEncoder("ann2rosFloatArr", "float", 4);

		System.out.println("Names of running nodes are: " +
				Mess.toAr(NodeFactory.np.namesOfRunningNodes()));
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) { e.printStackTrace(); }

		g.stopGroup(); 

		// called during exiting of the Nengo application
		RosUtils.utilsShallStop();
	}

	/**
	 * Newer version also checks whether the Modem was added, 
	 * if not, it automatically ads the Default modem.
	 */
	@Test
	public void runDemoNoModem(){

		RosUtils.prefferJroscore(true);

		NodeGroup g = new NodeGroup("pubsub",false);
		g.addNode(minimax,"minimaxNode","java");
		
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule smartOne = new DefaultNeuralModule("SmartNeuron",g);
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
		
		// called during exiting of the Nengo application
		RosUtils.utilsShallStop();
	}
	


}