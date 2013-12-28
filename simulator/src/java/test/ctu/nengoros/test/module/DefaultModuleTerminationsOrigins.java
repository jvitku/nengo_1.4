package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import org.junit.*;

import ca.nengo.model.Network;
import ca.nengo.model.Origin;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.impl.NetworkImpl;
import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.nodes.RosCommunicationTest;

/**
 * Tests whether the DefaultNeuralModule registers Terminations/Origins and communicate 
 * correctly. 
 * 
 * @author Jaroslav Vitku
 *
 */
public class DefaultModuleTerminationsOrigins extends RosCommunicationTest {

	public static String minimax = "resender.mpt.F2IPubSub";
	public static String modem = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";

	public static String AND 		= "org.hanns.logic.crisp.gates.impl.AND";
	public static String XOR 		= "org.hanns.logic.crisp.gates.impl.XOR";
	public static String OR 			= "org.hanns.logic.crisp.gates.impl.OR";
	public static String NAND 		= "org.hanns.logic.crisp.gates.impl.NAND";
	public static String NOT 		= "org.hanns.logic.crisp.gates.impl.NOT";


	/**
	 * Test how to ROsCommunicationTest can be used
	 */
	@Test
	public void disableRosUtils(){

		RosUtils.setAutorun(false); // disable Nengoros core autorun, the @BeforeClass is used

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
	}

	@Test
	public void registersOrigins(){
		RosUtils.setAutorun(false); // disable Nengoros core autorun, the @BeforeClass is used

		NodeGroup g = new NodeGroup("pubsub",false);
		g.addNode(minimax,"minimaxNode","java");

		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule smartOne = new DefaultNeuralModule("SmartNeuron",g);
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node

		smartOne.createDecoder("ros2annFloatArr", "int", 2);
		smartOne.createEncoder("ann2rosFloatArr", "float", 4);

		try {
			Origin o = smartOne.getOrigin("ros2annFloatArr");
			System.out.println("OK, correct origin named: "+o.getName()+" found");
		} catch (StructuralException e1) {
			fail("The Origin `ros2annFloatArr` was not registered by the DefaultNeuralModule");
			e1.printStackTrace();
		}
		g.stopGroup();
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
	}

	@Test
	public void registersTerminations(){
		RosUtils.setAutorun(false); // disable Nengoros core autorun, the @BeforeClass is used

		NodeGroup g = new NodeGroup("pubsub",false);
		g.addNode(minimax,"minimaxNode","java");

		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule smartOne = new DefaultNeuralModule("SmartNeuron",g);
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node

		smartOne.createDecoder("ros2annFloatArr", "int", 2);
		smartOne.createEncoder("ann2rosFloatArr", "float", 4);

		try {
			Termination t = smartOne.getTermination("ann2rosFloatArr");

			System.out.println("Dimensionality of this Termination is: "+t.getDimensions());
			assertTrue(t.getDimensions()==4);	// encoder encodes four float values

		} catch (StructuralException e1) {
			smartOne.printTerminationNames();
			fail("The termination `ros2annFloatArr` was not registerer");
			e1.printStackTrace();
		}

		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) { e.printStackTrace(); }

		g.stopGroup();
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
	}

	/**
	 * Test whether values are actually passed by the components - simple example.
	 */
	@Test
	public void communicationWorks(){
		RosUtils.setAutorun(false); // disable Nengoros core autorun, the @BeforeClass is used

		String name = "myName";
		NodeGroup g = new NodeGroup("OR", true);
		g.addNode(OR, "OR", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_OR", g);
		module.createEncoder("logic/gates/ina", "bool", 1);
		module.createEncoder("logic/gates/inb", "bool", 1);
		module.createDecoder("logic/gates/outa", "bool", 1);		

		// TODO: find out how to test network parts
		Network net = new NetworkImpl();
		//net.ad
	}




}
