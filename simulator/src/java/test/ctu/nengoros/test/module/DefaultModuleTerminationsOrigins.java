package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import org.junit.*;

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Origin;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.nodes.RosCommunicationTest;
import ctu.nengoros.testsuit.demo.nodes.gate.OR;
import ctu.nengoros.util.sync.impl.SyncedUnit;
import ca.nengo.model.impl.RealOutputImpl;

/**
 * Simulate the basic use case for NeuralModule, that is:
 * <ul>
 *  <li>Create group of nodes with a ROS node</li>
 *  <li>Create NeuralModule for the ROS node and connect its Encoders/Decoders, so that
 * the values on its Terminations are sent to the ROS node and decoded messages received from the 
 * ROS node are available on its Origins.</li>
 *  <li>Set values on NeuralModules terminations</li>
 *  <li>Simulate the Nengo simulation step</li>
 *  <li>Check it the values on Origin(s) are correct (e.g. the result of computation from the ROS node)</li>
 * </ul> 
 * 
 * @author Jaroslav Vitku
 *
 */
public class DefaultModuleTerminationsOrigins extends RosCommunicationTest {

	public static String minimax = "resender.mpt.F2IPubSub";
	public static String modem = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";

	public static String ORR 		= "ctu.nengoros.testsuit.demo.nodes.gate.OR";

	/**
	 * Called before any unit @Test
	 */
	@BeforeClass
	public static void startCore(){

		//RosUtils.setRqtAutorun(false);
		RosUtils.setAutorun(true);
		RosUtils.utilsShallStart();
	}

	/**
	 * Called after all unit @Test s
	 */
	@AfterClass
	public static void stopCore(){
		RosUtils.utilsShallStop();
	}

	/**
	 * Test how to ROsCommunicationTest can be used
	 */
	//@Ignore
	@Test
	public void disableRosUtils(){


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

		g.stopGroup();	// not necessary

		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

	}
	
	//@Ignore
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

	//@Ignore
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

		// Run ROS node computing logical OR (taken from the project logic/gates)
		String name = "myName";
		NodeGroup g = new NodeGroup("ORGROUP", true);
		g.addNode(ORR, "ORNODE", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_OR", g);

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
		
		System.out.println("origin named: "+OR.outAT+"found");
		
		
		// compute the OR truth table over the ROS newtork by means of Neural module
		assertFalse(this.makeSimulationStep(false, false, module, inTA, inTB, outA));
		assertTrue(this.makeSimulationStep(false, true, module, inTA, inTB, outA));
		assertTrue(this.makeSimulationStep(true, false, module, inTA, inTB, outA));
		assertTrue(this.makeSimulationStep(true, true, module, inTA, inTB, outA));
		
		assertFalse(this.makeSimulationStep(false, false, module, inTA, inTB, outA));
		
	}

	/**
	 * Simulate the Nengo simulation step. 
	 * @param a logic value to be sent to ROS OR node
	 * @param b logic value to be sent to ROS OR node
	 * @param module NeuralModule to be simulated
	 * @param inTA first Termination of the Module
	 * @param inTB second Termination of the Module
	 * @param outA Origin of the Module
	 * @return if everything worked good, return the binary value responded by the ROS node
	 */
	private boolean makeSimulationStep(boolean a, boolean b, 
			NeuralModule module, Termination inTA, Termination inTB, Origin outA){
		
		InstantaneousOutput aa, bb;
		
		if(a){
			aa = new RealOutputImpl(new float[]{1}, Units.ACU, 0);
		}else{
			aa = new RealOutputImpl(new float[]{0}, Units.ACU, 0);
		}
		if(b){
			bb = new RealOutputImpl(new float[]{1}, Units.ACU, 0);
		}else{
			bb = new RealOutputImpl(new float[]{0}, Units.ACU, 0);
		}

		// set values on both Terminations
		try {
			inTA.setValues(aa);
			inTB.setValues(bb);
		} catch (SimulationException e) {
			System.err.println("could not set values on terminations");
			e.printStackTrace();
			fail();
		}
		
		try {
			module.run(0, 1); // make the simulation step
		} catch (SimulationException e) {
			System.out.println("failed to run the module");
			e.printStackTrace();
			fail();
		}
		
		// after this, the values on all Origins (with synchronous Decoders) will be available
		this.waitForModuleReady(module);
		
		try {
			if(!(outA.getValues() instanceof RealOutputImpl)){
				System.err.println("Origin returned something different than RealOutputImpl");
				fail();
			}
				
			RealOutputImpl out = (RealOutputImpl) outA.getValues();
			float[]values = out.getValues();
			System.out.println("dimension of the output values is: "+values.length);
			System.out.println("Computed value (the value of this origin) is : \t"+values[0]);
			
			assertEquals(values.length,1);	// should return one float value representing true/false (1/0)
			
			assertTrue(values[0]==1.0 || values[0]==0.0);
			
			return (values[0]==1);	// return boolean value of the Origin
			
		} catch (SimulationException e) {
			System.out.println("could not read Origin vale");
			e.printStackTrace();
			fail();
		}
		System.err.println("ERROR! this should not be reached!");
		return false;
	}
	
	private int maxWait = 3000;
	
	/**
	 * The NeuralModule is synchronous by default, this does the modified Nengo simulator core
	 * for all NeuralModules (waits for they to be ready, for all their Decoders to be ready).
	 * Decoder is ready when it receives the ROS message (each time step, the ready state of
	 * all decoders is discarded).
	 */
	private void waitForModuleReady(NeuralModule module){
		int waited = 0;
		System.out.print("\n waiting for the ROS message to be received");
		while(!((SyncedUnit)module).isReady()){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			System.out.print(".");
			
			waited +=10;
			if(waited > maxWait){
				System.err.println("NeuralModule not ready fast enough! ROS communication probably broken!");
				fail();
			}
		}
		System.out.println("");
	}
	

}
