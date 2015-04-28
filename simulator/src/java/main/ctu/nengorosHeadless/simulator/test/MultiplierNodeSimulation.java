package ctu.nengorosHeadless.simulator.test;


import java.util.Random;

import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.util.SL;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.impl.DefaultNeuralModule;
import ctu.nengorosHeadless.simulator.impl.AbstractSimulator;
import ctu.nengorosHeadless.simulator.test.nodes.MultiplierNode;

public class MultiplierNodeSimulation {

	public static final boolean SYNC= true;
	public static final float multiplyBy = 3;
	public static final int NOINPUTS = 3;

	public static NeuralModule multiplierNode(String name, int noInputs, int logPeriod)
			throws ConnectionException, StartupDelayException{

		String className = "ctu.nengorosHeadless.simulator.test.nodes.MultiplierNode";
		String[] command = new String[]{className, "_"+MultiplierNode.noInputsConf+ ":=" + noInputs, 
				"_"+MultiplierNode.logPeriodConf+":="+logPeriod};

		NodeGroup g = new NodeGroup("MultiplierGroup", true);
		g.addNode(command, "MultiplierNode", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_Multiplier", g, SYNC);

		// connect the decay parameter to the Nengoros network (changed online)
		module.createConfigEncoder(MultiplierNode.topicMultiplier,"float", multiplyBy); 			

		module.createDecoder(MultiplierNode.topicDataOut, "float", noInputs);       
		module.createEncoder(MultiplierNode.topicDataIn, "float", noInputs); 		

		//module.createDecoder(MultiplierNode.topicProsperity,"float", 1);			//# float[]{prosperity}  = MSD from the limbo area
		return module;
	}

	public class MultiplierNodeSimulationX extends AbstractSimulator{

		public static final int log = 1; 

		private NeuralModule ms;

		/**
		 * Connect the motivation source into the loop
		 */
		@Override
		public void defineNetwork() {

			try {

				ms = multiplierNode("mul", NOINPUTS, log);
				this.nodes.add(ms);
				/*
				Connection c = this.connect(
						ms.getOrigin(MultiplierNode.topicDataOut),
						ms.getTermination(MultiplierNode.topicDataIn));

				float[][] w = c.getWeights();
				//BasicWeights.pseudoEye(w,0);	// motivation goes up as no reward comes 
				//w[1][0] = 2;
				w[0][0] = 12;
				 */

				//ms.getTermination(MultiplierNode.topicDataIn).sendValue(10, 0);
				//ms.getTermination(MultiplierNode.topicDataIn).setValues(new float[]{11});

				// ---- data:

			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (StartupDelayException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run(float from, float to){

			this.prepareForSimulaiton();
			float[] source, expected, result;

			t = from;
			int step = 0;

			while(t<=to){
				try {

					source = this.generateVector(NOINPUTS);
					expected = this.multiplyVector(source, multiplyBy);

					ms.getTermination(MultiplierNode.topicDataIn).setValues(source);

					this.makeStep();
					t += dt;

					result = ms.getOrigin(MultiplierNode.topicDataOut).getValues();
					System.out.println("//// received this value "+ SL.toStr(result));

					if(!expected(result, expected) && step++>0){
						System.err.println("fasdfadsfads \n\n\n error ");
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				} catch (StructuralException e) {
					e.printStackTrace();
				} catch (SimulationException e) {
					e.printStackTrace();
				}	
			}
		}

		private float[] multiplyVector(float[] source, float by){
			float[] out = new float[source.length];
			for(int i=0; i<source.length; i++){
				out[i] = source[i]*by;
			}
			return out;
		}

		private float[] generateVector(int len){
			Random r = new Random();
			float[] out = new float[len];
			for(int i=0; i<out.length; i++){
				out[i] = r.nextFloat();
			}
			return out;
		}

		private boolean expected(float[] a1, float[] a2){
			if(a1.length != a2.length){
				return false;
			}
			for(int i=0; i<a1.length; i++){
				if(a1[i] != a2[i]){
					return false;
				}
			}
			return true;
		}
	}


	public static void main(String[] args) {
		MultiplierNodeSimulation t = new MultiplierNodeSimulation();

		System.out.println("instantiating the simulator");

		MultiplierNodeSimulationX sim = t.new MultiplierNodeSimulationX();

		System.out.println("loading nodes..");

		sim.defineNetwork();
		System.out.println("starting the simulation now");

		sim.run(0, 100);
		System.out.println("all done, reset, waiting");
		sim.reset(false);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("ending the simulation");

		sim.cleanup();
	}
}
