package ctu.nengoros.comm.rosBackend.encoders.impl;

import org.apache.log4j.Logger;
import org.ros.node.ConnectedNode;

import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.encoders.Encoder;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.AsynNeuralModule;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.CanonicalModel;
import ca.nengo.dynamics.impl.LTISystem;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SpikeOutput;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.model.impl.BasicTermination;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;

public class BasicEncoder implements Encoder{

		private static final long serialVersionUID = 1L;

		private static Logger ourLogger = Logger.getLogger(BasicTermination.class);

		private Node myNode;
		private DynamicalSystem myDynamics;
		private Integrator myIntegrator;
		private String myName;
		private InstantaneousOutput myInput;
		private TimeSeries myOutput;
		private boolean myModulatory;
		protected ConnectedNode myRosNode;	// factory for subscriber
		
		public /*final */Backend ros; 

		/**
		 * Create BasicEncoder with dimension sizes determined by the ROS message type
		 * 
		 * @param node
		 * @param dynamics
		 * @param integrator
		 * @param name
		 * @param dataType
		 * @param u
		 * @param modem
		 * @param ros
		 * @throws StructuralException
		 */
		public BasicEncoder(Node node, DynamicalSystem dynamics, Integrator integrator,
				String name, String dataType, Units u, ModemContainer modem, Backend ros) throws StructuralException{
			cosntruct(node, dynamics, integrator, name, new int[]{ros.gedNumOfDimensions()}, dataType, u,modem,ros);
		}
		
		
		/**
		 * Create BesicEncoder with given dimension sizes.
		 * 
		 * @param node
		 * @param dynamics
		 * @param integrator
		 * @param name
		 * @param dimensionSizes
		 * @param dataType
		 * @param u
		 * @param modem
		 * @param ros
		 * @throws StructuralException
		 */
		public BasicEncoder(Node node, DynamicalSystem dynamics, Integrator integrator,
				String name, int[] dimensionSizes, String dataType, Units u, ModemContainer modem, Backend ros) throws StructuralException 
						{
			cosntruct(node, dynamics, integrator, name, dimensionSizes, dataType, u,modem,ros);
		}
		
		
		private void cosntruct(Node node, DynamicalSystem dynamics, Integrator integrator,
				String name, int[] dimensionSizes, String dataType, Units u, 
				ModemContainer modem, Backend ros) throws StructuralException{
			myNode = node;
			myDynamics = dynamics;
			myIntegrator = integrator;
			myName = name;
			myModulatory = false;
			try {
				myRosNode = modem.getConnectedNode();
			} catch (ConnectionException e) {
				System.err.println("BasicEncoder: my modem was not connected. Probably ROS communication error!!");
				e.printStackTrace();
			}	// possibly wait for Modem to init

			this.ros = ros;	// get my ROS backend

			// Nengo stuff
			((AsynNeuralModule)myNode).createTermination(myName, this);
		}
		
		/**
		 * @see ca.nengo.model.Termination#getDimensions()
		 */
		public int getDimensions() {
			return myDynamics.getInputDimension();
		}

		/**
		 * @see ca.nengo.model.Termination#getName()
		 */
		public String getName() {
			return myName;
		}

		/**
		 * @see ca.nengo.model.Termination#setValues(ca.nengo.model.InstantaneousOutput)
		 */
		public void setValues(InstantaneousOutput values) throws SimulationException {
			myInput = values;
		}

		/**
		 * My: creates part of data series (now two data samples)
		 * this method publishes all 
		 * 
		 * Runs the Termination, making a TimeSeries of output from this Termination
		 * available from getOutput().
		 *
		 * @param startTime simulation time at which running starts (s)
		 * @param endTime simulation time at which running ends (s)
		 * @throws SimulationException if a problem is encountered while trying to run
		 *
		public void run(float startTime, float endTime) throws SimulationException {
			
			float[] input = null;
			if (myInput instanceof RealOutput) {
				input = ((RealOutput) myInput).getValues();
			} else if (myInput instanceof SpikeOutput) {
				boolean[] spikes = ((SpikeOutput) myInput).getValues();
				input = new float[spikes.length];
				float amplitude = 1f / (endTime - startTime);
				for (int i = 0; i < spikes.length; i++) {
					if (spikes[i]) {
	                    input[i] = amplitude;
	                }
				}
			}else{
//				System.out.println("BasicEncoder: input seems to be not connected");
				// here it throws null pointer if input is not initialized
				return;
			}

			///////////////////////////////////// here it is!!
			// so this will create Events of type ANN2ROS_sendMessage
			// ROS side then publishes a message to a given topic 
			// nice
			// so far only realOutputs..
			TimeSeries inSeries = new TimeSeriesImpl(new float[]{startTime, endTime}, 
					new float[][]{input, input}, Units.uniform(Units.UNK, input.length));
			myOutput = myIntegrator.integrate(myDynamics, inSeries);
			
			
			// so: just do the same as basicTermination, myOutput then send to ROS..
			
			// array n*dt, where n is dimension and dt is num of time samples
			float[][] ff_series = myOutput.getValues();
			int numSamples = myOutput.getTimes().length;
			System.out.println(" " + numSamples);
			System.out.println(MU.toString(ff_series, 2));
			
			for(int i=0; i<ff_series[0].length; i++){
				if(ff_series[0][i] != ff_series[1][i])
					System.out.println("ffffffffffffffffffffffffffffff ");
			}
			
			for(int i=0; i<numSamples; i++){
				ros.publish(ff_series[i]);
			}
		}
		*/
		public void run(float startTime, float endTime) throws SimulationException {
			
			float[] input = null;
			if (myInput instanceof RealOutput) {
				input = ((RealOutput) myInput).getValues();
			} else if (myInput instanceof SpikeOutput) {
				boolean[] spikes = ((SpikeOutput) myInput).getValues();
				input = new float[spikes.length];
				float amplitude = 1f / (endTime - startTime);
				for (int i = 0; i < spikes.length; i++) {
					if (spikes[i]) {
	                    input[i] = amplitude;
	                }
				}
			}else{
				return;
			}
			TimeSeries inSeries = new TimeSeriesImpl(new float[]{endTime}, 
					new float[][]{input}, Units.uniform(Units.UNK, input.length));
			myOutput = myIntegrator.integrate(myDynamics, inSeries);
			// array n*dt, where n is dimension and dt is num of time samples
			float[][] ff_series = myOutput.getValues();
			// publish as a ROS message
			ros.publish(ff_series[0]);
		}
		
		/**
		 * Note: typically called by the Node to which the Termination belongs.
		 *
		 * @return The most recent input multiplied
		 */
		public TimeSeries getOutput() {
			return myOutput;
		}

		/**
		 * @see ca.nengo.model.Termination#getNode()
		 */
		public Node getNode() {
			return myNode;
		}

		/**
		 * @see ca.nengo.model.Resettable#reset(boolean)
		 */
		public void reset(boolean randomize) {
			myInput = null;
		}

		/**
		 * @see ca.nengo.model.Termination#getModulatory()
		 */
		public boolean getModulatory() {
			return myModulatory;
		}

		/**
		 * @see ca.nengo.model.Termination#getTau()
		 */
		public float getTau() {
			if (myDynamics instanceof LTISystem) {
				return CanonicalModel.getDominantTimeConstant((LTISystem) myDynamics);
			} else {
				ourLogger.warn("Can't get time constant for non-LTI dynamics. Returning 0.");
				return 0;
			}
		}

		/**
		 * @see ca.nengo.model.Termination#setModulatory(boolean)
		 */
		public void setModulatory(boolean modulatory) {
			myModulatory = modulatory;
		}

		/**
		 * @see ca.nengo.model.Termination#setTau(float)
		 */
		public void setTau(float tau) throws StructuralException {
			if (myDynamics instanceof LTISystem) {
				CanonicalModel.changeTimeConstant((LTISystem) myDynamics, tau);
			} else {
				throw new StructuralException("Can't set time constant of non-LTI dynamics");
			}
		}
		
		/**
		 * @return Extract the input to the termination.
		 */
		public InstantaneousOutput getInput(){
			return myInput;
		}

		@Override
		public Encoder clone() throws CloneNotSupportedException {
			BasicEncoder result = (BasicEncoder) super.clone();
			result.myDynamics = myDynamics.clone();
			result.myIntegrator = myIntegrator.clone();
			result.myInput = myInput.clone();
			result.myOutput = myOutput.clone();
			return result;
		}


		@Override
		public Termination clone(Node node) throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return null;
		}



}
