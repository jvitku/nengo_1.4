package ctu.nengoros.test.module.components;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.*;

import ca.nengo.dynamics.Integrator;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.model.impl.BasicTermination;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.util.TimeSeries;
import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.dynamics.NoIntegrator;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.model.transformMultiTermination.impl.SumMultiTermination;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.test.module.*;
import ctu.nengoros.util.SL;

public class MultiTermination extends NengorosTest{

	public static String minimax = "ctu.nengoros.testsuit.demo.nodes.minmax.F2IPubSub";
	public static String ORR 		= "ctu.nengoros.testsuit.demo.nodes.gate.OR";

	//@Ignore
	@Test
	public void runSingleTerminationTimeTest(){

		String name = "singleTerm";
		NodeGroup g = new NodeGroup(name, false);
		g.addNode(minimax,"nameA","java");
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule(name,g);
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail();
		} catch (StartupDelayException e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node

		IdentityLTISystem noLTI = new IdentityLTISystem(4);
		Integrator noInt = new NoIntegrator();
		Termination t1=null,t2=null;

		//SumMultiTermination mt = new SumMultiTermination(module, name, noInt, noLTI);
		SumMultiTermination mt = new SumMultiTermination(module, name, noInt, noLTI.getInputDimension());

		assertTrue(mt.getDimension()==4);
		// one Termination added by default
		assertTrue(mt.getTerminations().size()==1); 
		assertTrue(mt.getTerminations().get(name)!=null);

		// add some new terminations
		try {
			t1 = mt.addTermination();
			assertTrue(mt.getTerminations().size()==2);
			System.out.println("added this one: "+t1.getName());
			assertTrue(t1.getName().equalsIgnoreCase(name+"_0"));

			t2 = mt.addTermination();
			System.out.println("added this one: "+t2.getName());
			assertTrue(mt.getTerminations().size()==3);
			assertTrue(t2.getName().equalsIgnoreCase(name+"_1"));
		} catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}
		/**
		 * setup done
		 */

		float[] zerovals = new float[]{1,1,1,1};
		RealOutputImpl io = new RealOutputImpl(zerovals,Units.UNK,0);

		/**
		 * SImulation step
		 */
		try {
			//t0.setValues(io); // not necessary
			t1.setValues(io);
			//t2.setValues(io);
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		BasicTermination t = (BasicTermination) t1;
		System.out.println("simulating step of BasicTrermination "+t.getName());

		try {
			t.run(0,1);
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		TimeSeries out = t.getOutput();
		float[][] data = out.getValues();
		this.checkTimeandDim(out, 2,4,t,0,1);

		System.out.println("number of time samples is: "+data.length);
		System.out.println("dimensionality of data is "+data[0].length);

		// check whether we received the same as was set to the Termination (no integration,dynamics..)
		for(int i=0; i<zerovals.length; i++){
			System.out.println("\t\tvalues are: t0: "+data[0][i]+" t1: "+data[1][i]);
			// no integration, derivation?
			assertTrue(data[1][i]==zerovals[i]);
			assertTrue(data[0][i]==zerovals[i]);
		}

		/**
		 * Another step
		 */
		System.out.println("simulating step of BasicTrermination "+t.getName());
		try {
			t.run(1,2);
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		out = t.getOutput();
		data = out.getValues();
		this.checkTimeandDim(out, 2,4,t,1,2);

		System.out.println("number of time samples is: "+data.length);
		System.out.println("dimensionality of data is "+data[0].length);

		// check whether we received the same as was set to the Termination
		for(int i=0; i<zerovals.length; i++){
			System.out.println("\t\tvalues are: t0: "+data[0][i]+" t1: "+data[1][i]);
			// no integration, derivation?
			assertTrue(data[1][i]==zerovals[i]);
			assertTrue(data[0][i]==zerovals[i]);
		}

		/**/
		try {
			Thread.sleep(100); // TODO: really get rid of this..
		} catch (InterruptedException e) {
			e.printStackTrace();
		}/**/
		System.out.println("ending the test, number of nodes: "+NodeFactory.np.numOfRunningNodes());
		
		g.stopGroup();
	}


	/**
	 * The same as the above, but this termination is weighted (check data)
	 */
	//@Ignore
	@Test
	public void runSingleWeightedTermination(){

		String name = "singleWaightedTerm";

		/**
		 * setup three differently weighted terminations
		 */

		NodeGroup g = new NodeGroup(name, false);
		g.addNode(minimax,"nameA","java");
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule(name,g);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node

		IdentityLTISystem noLTI = new IdentityLTISystem(4);
		Integrator noInt = new NoIntegrator();
		Termination t0=null, t1=null,t2=null, t3=null;

		//SumMultiTermination mt = new SumMultiTermination(module, name, noInt, noLTI);
		SumMultiTermination mt = new SumMultiTermination(module, name, noInt, noLTI.getInputDimension());
		
		/**
		 * setup done
		 */

		//float weight = (float) 1;
		//Float[] weights = new Float[]{(float) 1,(float)1,(float)1,(float)1};

		float weight = 0.65f;
		//float[] weights = new Float[]{0.1,(float) 1,(float) 10,(float) -101};
		float[] weights = new float[]{0.1f,1f,10f,-101f};// place on diagonal
		
		float[][] diag = new float[weights.length][weights.length];
		for(int i=0; i<weights.length; i++){
			diag[i][i] = weights[i];
		}
		System.out.println("The weights are: \n"+SL.toStr(diag));
		
		assertTrue(mt.getDimension()==4);
		assertTrue(mt.getTerminations().size()==1); 
		assertTrue(mt.getTerminations().get(name)!=null);

		// add some new terminations
		try {
			// need to set values for all terminations!			
			t0 = (BasicTermination) mt.getTerminations().get(name);

			t1 = mt.addTermination();					// t1 has default weight=1
			assertTrue(mt.getTerminations().size()==2);
			System.out.println("added this one: "+t1.getName());
			assertTrue(t1.getName().equalsIgnoreCase(name+"_0"));
			assertTrue(t1.getDimensions()==4);

			t2 = mt.addTermination(weight);				// t2 is weighted by single value
			System.out.println("added this one: "+t2.getName());
			assertTrue(mt.getTerminations().size()==3);
			assertTrue(t2.getDimensions()==4);
			assertTrue(t2.getName().equalsIgnoreCase(name+"_1"));

			t3 = mt.addTermination(diag);			// t3 is weighted by weight matrix
			System.out.println("added this one: "+t3.getName());
			assertTrue(mt.getTerminations().size()==4);
			assertTrue(t3.getDimensions()==4);
			assertTrue(t3.getName().equalsIgnoreCase(name+"_2"));

		} catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}

		/**
		 * run
		 */
		Mess.waitms(100);	// to ROS nodes to start

		float[] zerovals = new float[]{0,0,0,0};
		float[] result = new float[]{0,0,0,0};

		// compute weighted zeros
		this.checkWeighting(zerovals, result,t0,t1,t2,t3,module,mt);

		// compute weighted ones
		float[] onevals = new float[]{1,1,1,1};
		result = new float[]{
				onevals[0]	+	onevals[0] + onevals[0]*weight	+ onevals[0]*weights[0],
				onevals[1]	+	onevals[1] + onevals[1]*weight	+ onevals[1]*weights[1],
				onevals[2]	+	onevals[2] + onevals[2]*weight	+ onevals[2]*weights[2],
				onevals[3]	+	onevals[3] + onevals[3]*weight	+ onevals[3]*weights[3],};

		System.out.println("trying to compute this one: \n"+toStr(onevals)+"\nand the reult should be: \n"+toStr(result));
		this.checkWeighting(onevals, result,t0,t1,t2,t3,module,mt);

		g.stopGroup();
	}


	/**
	 * 
	 * @param zeros input to all terminations
	 * @param result value resulting of weighted sum of particular Terminations
	 */
	private void checkWeighting(float[] zerovals, float[] result, 
			Termination t0, Termination t1, Termination t2,
			Termination t3, NeuralModule module, SumMultiTermination mt){

		RealOutputImpl zeros = new RealOutputImpl(zerovals,Units.UNK,0);

		try {
			t0.setValues(zeros);	// all zeros
			t1.setValues(zeros);
			t2.setValues(zeros);
			t3.setValues(zeros);
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		System.out.println("Simulating simulation step of entire NeuralModule");
		try {
			module.run(0, 1);
			// MultiTermination is not registered as Encoder here, so must be ran manually
			mt.run(0, 1);		
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		TimeSeries out = ((BasicTermination)t0).getOutput();
		this.checkTimeandDim(out, 2, 4,(BasicTermination)t0,0,1); // check dimensions of all

		/* inconistent with the current implementation of MultiTermination (wighting is done on terminations)
		// check whether the Module has zeros on particular Terminations and on MultiTermination
		float[][] d = ((BasicTermination)t0).getOutput().getValues();
		for(int i=0; i<zerovals.length; i++){
			assertTrue(d[1][i] == zerovals[i]);
		}
		d = ((BasicTermination)t1).getOutput().getValues();
		for(int i=0; i<zerovals.length; i++){
			assertTrue(d[1][i] == zerovals[i]);
		}
		d = ((BasicTermination)t2).getOutput().getValues();
		for(int i=0; i<zerovals.length; i++){
			assertTrue(d[1][i] == zerovals[i]);
		}
		d = ((BasicTermination)t3).getOutput().getValues();
		for(int i=0; i<zerovals.length; i++){
			assertTrue(d[1][i] == zerovals[i]);
		}*/
		// MultiTermination has summed weighted zeros?
		float[][] d = mt.getOutput().getValues();
		System.out.println("result received is \n"+toStr(d)+" and should be \n"+toStr(result));
		for(int i=0; i<zerovals.length; i++){
			assertTrue(d[1][i]==result[i]);
		}
	}

	private void checkTimeandDim(TimeSeries ts, int noSamples, int dim, BasicTermination t,
			float startTime, float endTime){

		float[][] data = ts.getValues();

		assertTrue(data[0].length == t.getDimensions());	// dimension OK?
		assertTrue(data.length == 2);	// simulated form 0 to 1 sec. , OK?
		float[] times = ts.getTimes();	// this contains simulation step times
		assertTrue(times[0]==startTime);
		assertTrue(times[1]==endTime);
	}

	//@Ignore
	@Test
	public void runEntireModule(){
		String name = "fullModule";

		NodeGroup g = new NodeGroup(name, false);
		g.addNode(minimax,"nameA","java");
		assertTrue(NodeFactory.np.numOfRunningNodes() == 0); // one modem and one ROS node
		NeuralModule module = null;
		try {
			module = new DefaultNeuralModule(name,g);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(NodeFactory.np.numOfRunningNodes() == 2); // one modem and one ROS node

		IdentityLTISystem noLTI = new IdentityLTISystem(4);
		Integrator noInt = new NoIntegrator();
		Termination t0=null, t1=null,t2=null;

		//SumMultiTermination mt = new SumMultiTermination(module, name, noInt, noLTI);
		SumMultiTermination mt = new SumMultiTermination(module, name, noInt, noLTI.getInputDimension());

		t0 = mt.getTerminations().get(name);
		// add some new terminations
		try {
			t1 = mt.addTermination();
			assertTrue(mt.getTerminations().size()==2);
			System.out.println("added this one: "+t1.getName());
			assertTrue(t1.getName().equalsIgnoreCase(name+"_0"));

			t2 = mt.addTermination();
			System.out.println("added this one: "+t2.getName());
			assertTrue(mt.getTerminations().size()==3);
			assertTrue(t2.getName().equalsIgnoreCase(name+"_1"));
		} catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}

		float[] vals = new float[]{0,0,0,0};
		RealOutputImpl io = new RealOutputImpl(vals,Units.UNK,0);
		try {
			t0.setValues(io);
			t1.setValues(io);
			t2.setValues(io);
		} catch (SimulationException e) {
			e.printStackTrace();
			fail();
		}

		try {
			System.out.println("stepping");
			module.run(0, 1);
			System.out.println("\nstep done");
		} catch (SimulationException e) {
			System.out.println("\nstep failed");
			e.printStackTrace();
		}
		g.stopGroup();
	}

	public String toStr(TimeSeries ts){
		float[] vals = ts.getValues()[0];
		return toStr(vals);
	}

	public String toStr(float[][] f){
		String out = "";

		for(int i=0; i<f.length; i++){
			for(int j=0; j<f[0].length; j++){
				out = out+" "+f[i][j];
			}
			out = out+" | ";
		}
		return out;
	}

	public String toStr(float[] f){
		String out = "";
		for(int i=0; i<f.length; i++)
			out = out+" "+f[i];
		return out;
	}

}
