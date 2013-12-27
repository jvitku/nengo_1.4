package ctu.nengoros.comm.rosBackend.encoders.impl;

import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.encoders.AbstractEncoder;
import ctu.nengoros.comm.rosBackend.encoders.Encoder;
import ctu.nengoros.modules.AsynNeuralModule;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SpikeOutput;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * <p>Each NeuralModule can have one or more Encoders and Decoders.</p>
 * 
 * <p>This is basic implementation of Encoder, which implement a Termination. 
 * Values received on this Termination are encoded into ROS messages and 
 * sent over the ROS network on the topic generated from the Termination name.</p> 
 * 
 * @author Jaroslav Vitku
 *
 */
public class BasicEncoder extends AbstractEncoder{

	private static final long serialVersionUID = 2694704062527736159L;

	/**
	 * Create BasicEncoder with dimension sizes determined by the ROS message type.
	 * 
	 * @param node parent of this Termination
	 * @param dynamics DynamicalSystem defining properties of the input
	 * @param integrator Integrator defining properties of the input
	 * @param name name of this Encoder
	 * @param dataType determines type of data to be encoded to the ROS network
	 * @param u Nengo Units 
	 * @param modem a ROS node which registers ROS Publishers/Subscribers for the ROS communication 
	 * @param ros Backend that is used for direct communication with ROS infrastructure (e.g. encode/send)
	 * @throws StructuralException
	 */
	public BasicEncoder(Node node, DynamicalSystem dynamics, Integrator integrator,
			String name, String dataType, Units u, ModemContainer modem, Backend ros) throws StructuralException{

		super(node, dynamics, integrator, name, dataType, u, modem, ros);
		
		// crate one Termination, which will be used to receive data which will be sent over the ROS network. 
		((AsynNeuralModule)myNode).createTermination(myName, this);
	}

	/**
	 * Create BesicEncoder with given dimension sizes.
	 * 
	 * @param node parent of this Termination
	 * @param dynamics DynamicalSystem defining properties of the input
	 * @param integrator Integrator defining properties of the input
	 * @param name name of this Encoder
	 * @param dimensionSizes array defining sizes of ROS message dimensions (to be translated into vector of floats for Nengo) 
	 * @param dataType determines type of data to be encoded to the ROS network
	 * @param u Nengo Units 
	 * @param modem a ROS node which registers ROS Publishers/Subscribers for the ROS communication 
	 * @param ros Backend that is used for direct communication with ROS infrastructure (e.g. encode/send)
	 */
	public BasicEncoder(Node node, DynamicalSystem dynamics, Integrator integrator,
			String name, int[] dimensionSizes, String dataType, Units u, ModemContainer modem, Backend ros) throws StructuralException{
		
		super(node, dynamics, integrator, name, dimensionSizes, dataType, u, modem, ros);

		// crate one Termination, which will be used to receive data which will be sent over the ROS network.
		((AsynNeuralModule)myNode).createTermination(myName, this);
	}


	/**
	 * @see ca.nengo.model.Termination#setValues(ca.nengo.model.InstantaneousOutput)
	 */
	public void setValues(InstantaneousOutput values) throws SimulationException {
		myInput = values;
	}

	/**
	 * Collect data on my input (this implements Termination), encode them and 
	 * publish over the ROS network.
	 * 
	 * @param startTime simulation time at which running starts (s)
	 * @param endTime simulation time at which running ends (s)
	 * @throws SimulationException if a problem is encountered while trying to run
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
		// TODO: send entire TimeSeries over the ROS network
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
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		myInput = null;
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
		return (Termination)this.clone();
	}

}
