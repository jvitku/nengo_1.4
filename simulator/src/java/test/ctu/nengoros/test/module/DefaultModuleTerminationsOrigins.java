package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import org.junit.*;

import ctu.nengoros.testsuit.demo.nodes.minmax.*;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Origin;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.testsuit.demo.nodes.gate.OR;
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
public class DefaultModuleTerminationsOrigins extends NengorosTest/*extends RosCommunicationTest */{


	public static String minimax = "ctu.nengoros.testsuit.demo.nodes.minmax.F2IPubSub";
	public static String ORR 		= "ctu.nengoros.testsuit.demo.nodes.gate.OR";

	//@Ignore
	@Test
	public void registersOrigins(){

		NodeGroup g = new NodeGroup("pubsub",false);
		g.addNode(minimax,"minimaxNode","java");

		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule("NeuralModule",g);
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node

		module.createDecoder("ros2annFloatArr", "int", 2);
		module.createEncoder("ann2rosFloatArr", "float", 4);

		try {
			Origin o = module.getOrigin("ros2annFloatArr");
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

		NodeGroup g = new NodeGroup("pubsub",false);
		g.addNode(minimax,"minimaxNode","java");

		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule("NeuralModule",g);
		} catch (ConnectionException e2) {
			e2.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node

		module.createDecoder("ros2annFloatArr", "int", 2);
		module.createEncoder("ann2rosFloatArr", "float", 4);

		try {
			Termination t = module.getTermination("ann2rosFloatArr");

			System.out.println("Dimensionality of this Termination is: "+t.getDimensions());
			assertTrue(t.getDimensions()==4);	// encoder encodes four float values

		} catch (StructuralException e1) {
			module.printTerminationNames();
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
	 * Test whether values are actually passed by the components - test with
	 * multiple-valued Terminations and Origins.
	 */
	//@Ignore
	@Test
	public void MIMOCommunication(){

		// Run ROS node computing logical OR (taken from the project logic/gates)
		String name = "myName";
		NodeGroup g = new NodeGroup("MINMAXGROUP", true);
		g.addNode(minimax, "MINMAX", "java");
		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule(name+"_MM", g);
		} catch (ConnectionException e1) {
			e1.printStackTrace();
			fail();
		}

		// Connect Neural module to the node
		module.createEncoder(F2IPubSub.ann2ros , "float", 4);//ROS input=4lfoats
		module.createDecoder(F2IPubSub.ros2ann, "int", 2);	 //ROS output=2	

		Termination t=null; Origin o=null;

		try {
			t = module.getTermination(F2IPubSub.ann2ros);
			System.out.println("termination called "+t.getName()+" found");

		} catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}
		try {
			o = module.getOrigin(F2IPubSub.ros2ann);
			System.out.println("origin named: "+o.getName()+" found");

		} catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}
		// note that it is not guaranteed that the external ROS node is started
		// already, so wait for it several milliseconds
		Mess.waitms(100);	 

		this.checkComputation(new float[]{-10,10,11,230}, 	module, t, o);
		this.checkComputation(new float[]{0,0,0,0}, 		module, t, o);
		this.checkComputation(new float[]{1000,0,1,10}, 	module, t, o);
		this.checkComputation(new float[]{-10,-10,-11,-230},module, t, o);

		g.stopGroup();
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
	}

	private void checkComputation(float[] vals, NeuralModule module, Termination t, Origin o){

		float[] out = this.makeSimulationStepMIMO(vals, module, t, o);
		System.out.println("-- received values are:  "+out[0]+" "+out[1]);
		assertTrue(out[0] == min(vals));
		assertTrue(out[1] == max(vals));
	}

	private float max(float[] vals){
		float out = vals[0];
		for(int i=1; i<vals.length; i++)
			if(vals[i]>out)
				out=vals[i];
		return out;
	}

	private float min(float[] vals){
		float out = vals[0];
		for(int i=1; i<vals.length; i++)
			if(vals[i]<out)
				out=vals[i];
		return out;
	}

	private float[] makeSimulationStepMIMO(float[] inputs, NeuralModule module, 
			Termination t, Origin o){

		InstantaneousOutput aa =
				new RealOutputImpl(inputs, Units.ACU, 0);

		// set values on both Terminations
		try {
			t.setValues(aa);
		} catch (SimulationException e) {
			System.err.println("could not set values on termination");
			e.printStackTrace();
			fail();
		}

		makeSimStep(0, 1, module);

		try {
			if(!(o.getValues() instanceof RealOutputImpl)){
				System.err.println("Origin returned something different than RealOutputImpl");
				fail();
			}

			RealOutputImpl out = (RealOutputImpl) o.getValues();
			float[]values = out.getValues();
			assertEquals(values.length,2);	// the Decoder returns only one time sample (2 dim output)

			System.out.println("Computed values are: \t"+values[0]+" "+values[1]);

			return values;

		} catch (SimulationException e) {
			System.out.println("could not read Origin vale");
			e.printStackTrace();
			fail();
		}
		System.err.println("ERROR! this should not be reached!");
		return new float[]{-1,-1};
	}

	/**
	 * Test whether values are actually passed by the components - simple test with
	 * single-valued Terminations and Origins.
	 */
	//@Ignore
	@Test
	public void SISOCommunication(){

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

		// note that it is not guaranteed that the external ROS node is started
		// already, so wait for it several milliseconds
		Mess.waitms(100); 

		// compute the OR truth table over the ROS network by means of Neural module
		assertFalse(this.makeSimulationStep(false, false, 	module, inTA, inTB, outA));
		assertTrue(this.makeSimulationStep(false, true, 	module, inTA, inTB, outA));
		assertTrue(this.makeSimulationStep(true, false, 	module, inTA, inTB, outA));
		assertTrue(this.makeSimulationStep(true, true, 		module, inTA, inTB, outA));

		assertFalse(this.makeSimulationStep(false, false, 	module, inTA, inTB, outA));

		g.stopGroup();
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
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

		makeSimStep(0, 1, module);

		try {
			if(!(outA.getValues() instanceof RealOutputImpl)){
				System.err.println("Origin returned something different than RealOutputImpl");
				fail();
			}

			RealOutputImpl out = (RealOutputImpl) outA.getValues();
			float[]values = out.getValues();
			assertEquals(values.length,1);	// should return one float value representing true/false (1/0)

			System.out.println("Computed value (the value of this origin) is : \t"+values[0]);

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



}
