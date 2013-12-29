package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.nengo.model.Origin;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.model.impl.RealOutputImpl;
import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
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

	/**
	 * The same as 
	 * 
	 * {@link ctu.nengoros.test.module.components.MultiTermination#runSingleWeightedTermination()}
	 * 
	 * but this one uses the MultiTermination to send summed data on own 
	 * Terminations over the ROS network to the ROS node, the received 
	 * values (min/max of MulitTerminationOutput) are checked to be correct.
	 *  
	 */
	@Test
	public void MultiTerminationWithEncoder(){

		String name = "singleWaightedTermMinMax";

		/**
		 * setup the neural module, run the ROS MinMaxInt node
		 */

		NodeGroup g = new NodeGroup(name, false);
		g.addNode(minimax,"NameX","java");
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node

		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule(name,g);
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node

		// this is used in Jython script to crate Encoders/Decoders
		module.createDecoder(F2IPubSub.ros2ann, "int", 2);		// Origin
		module.createEncoder(F2IPubSub.ann2ros, "float", 4);	// Termination(s)

		/**
		 * setup the interface in the Nengo
		 */

		float weight = (float) 0.65;
		Float[] weights = new Float[]{(float) 0.1,(float) 1,(float) 10,(float) -101};

		Termination t0=null,t1=null,t2=null;
		Termination tDef = null;

		try {
			tDef = module.getTermination(F2IPubSub.ann2ros);	// created by default

			t0 = module.connectMultiTermination(F2IPubSub.ann2ros);
			t1 = module.connectMultiTermination(F2IPubSub.ann2ros,weight);	
			t2 = module.connectMultiTermination(F2IPubSub.ann2ros,weights);

		} catch (StructuralException e) {
			e.printStackTrace();
		}

		/**
		 * prepare data
		 */
		Mess.waitms(100);	// to ROS nodes to start

		// compute the result of computation:
		// min/max value of: weighted sum of Terminations
		float[] onevals = new float[]{1,1,1,1};
		RealOutputImpl io = new RealOutputImpl(onevals,Units.UNK,0);
		float[] result = new float[]{
				onevals[0]	+	onevals[0] + onevals[0]*weight	+ onevals[0]*weights[0],
				onevals[1]	+	onevals[1] + onevals[1]*weight	+ onevals[1]*weights[1],
				onevals[2]	+	onevals[2] + onevals[2]*weight	+ onevals[2]*weights[2],
				onevals[3]	+	onevals[3] + onevals[3]*weight	+ onevals[3]*weights[3],};

		// this should be retrieved from the ROS node
		int max = Math.round(this.max(result));
		int min = Math.round(this.min(result));

		/**
		 * Simulate
		 */
		try {
			// set data on inputs
			tDef.setValues(io);
			t0.setValues(io);
			t1.setValues(io);
			t2.setValues(io);

			// run and wait for module to be ready
			makeSimStep(0,1,module);	

			// read data from output
			Origin o = module.getOrigin(F2IPubSub.ros2ann);
			float[] vals = ((RealOutputImpl)o.getValues()).getValues();

			System.out.println("values are: "+vals[0]+" "+vals[1]);
			System.out.println("values should be "+min+" "+max);

			//check if the result is correct
			assertTrue(min==vals[0]);
			assertTrue(max==vals[1]);

		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}
		g.stopGroup();
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
}
