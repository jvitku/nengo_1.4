package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Origin;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.model.impl.RealOutputImpl;
import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosBackend.encoders.multiTermination.MultiTermination;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.testsuit.demo.nodes.minmax.F2IPubSub;

/**
 * Extension of the  ctu.nengoros.test.module.DefaultModuleTerminationsOrigins .
 * This tests how multiple Terminations can be simply added to one Encoder. 
 * This enables user to connect multiple components to one "Termination" Encoder.
 * 
 * @author Jaroslav Vitku
 *
 */
public class MultipleTerminationsOneEncoder extends NengorosTest{

	public static String minimax = "ctu.nengoros.testsuit.demo.nodes.minmax.F2IPubSub";
	public static String ORR = "ctu.nengoros.testsuit.demo.nodes.gate.OR";


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

		// Connect NeuralModule with one Encoder and one Decoder
		module.createEncoder(F2IPubSub.ann2ros , "float", 4);
		module.createDecoder(F2IPubSub.ros2ann, "int", 2);	 	

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
		Termination tt = null;
		try{
			MultiTermination mt = module.getMultiTermination(F2IPubSub.ann2ros);
			tt = mt.addTermination();

		} catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}

		// note that it is not guaranteed that the external ROS node is started
		// already, so wait for it several milliseconds
		Mess.waitms(100);	 

		this.checkComputation(new float[]{-10,10,11,230}, 	module, t, tt,o);
		this.checkComputation(new float[]{0,0,0,0}, 		module, t, tt,o);
		this.checkComputation(new float[]{1000,0,1,10}, 	module, t, tt,o);
		this.checkComputation(new float[]{-10,-10,-11,-230},module, t, tt,o);

		g.stopGroup();
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
	}

	private void checkComputation(float[] vals, NeuralModule module, Termination t, Termination tt, Origin o){

		float[] out = this.makeSimulationStepMIMO(vals, module, t, tt,o);
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
			Termination t, Termination tt, Origin o){

		InstantaneousOutput aa =
				new RealOutputImpl(inputs, Units.ACU, 0);

		try {
			t.setValues(aa);
			tt.setValues(aa);
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

}
