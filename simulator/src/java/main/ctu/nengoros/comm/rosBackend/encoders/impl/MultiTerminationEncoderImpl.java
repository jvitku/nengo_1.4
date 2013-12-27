package ctu.nengoros.comm.rosBackend.encoders.impl;

import org.apache.log4j.Logger;
import org.ros.node.ConnectedNode;

import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.encoders.MultiTerminationEncoder;
import ctu.nengoros.comm.rosBackend.multiTermination.MultiTermination;
import ctu.nengoros.comm.rosBackend.multiTermination.impl.SumMultiTermination;
import ctu.nengoros.exceptions.ConnectionException;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.CanonicalModel;
import ca.nengo.dynamics.impl.LTISystem;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.BasicTermination;
import ca.nengo.util.TimeSeries;

/**
 * The same as BasicEncoder, but this one can have multiple Terminations (inputs), 
 * where the inputs on these Terminations are combined together. The resulting
 * value is encoded and sent over the ROS network immediately, but can be 
 * also obtained by the getOutput() methid. 
 * 
 * @author Jaroslav Vitku
 *
 */
public class MultiTerminationEncoderImpl implements MultiTerminationEncoder{

	public static final String me = "[MultiTerminationEncoderImpl] ";
	private static Logger ourLogger = Logger.getLogger(BasicTermination.class);

	// all my Terminations are here
	protected MultiTermination multiTermination;
	
	// common properties of my Terminations
	public final int DIM = 1; 	// dimension.. TODO
	protected DynamicalSystem myDynamics; 
	protected Integrator myIntegrator;

	protected Node myNode;				// my parent
	protected String myName;		
	protected TimeSeries myOutput;		// my current output
	protected ConnectedNode myRosNode;	// factory for subscriber

	public Backend ros;					// Nengo interfaced with ROS here
	

	/**
	 * <p>Create MultiEncoder with dimension sizes determined by the ROS message type (currently 
	 * only 1 dimension is supported).</p>
	 * 
	 * <p>The constructor adds one Termination automatically by default, this is left unconnected.</p>
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
	public MultiTerminationEncoderImpl(Node node, DynamicalSystem dynamics, Integrator integrator,
			String name, String dataType, Units u, ModemContainer modem, Backend ros) throws StructuralException{

		init(node, dynamics, integrator, name, new int[]{ros.gedNumOfDimensions()}, dataType, u, modem, ros);
	}

	/**
	 * Create BesicEncoder with given dimension sizes. Currently only one dimension is supported.
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
	public MultiTerminationEncoderImpl(Node node, DynamicalSystem dynamics, Integrator integrator,
			String name, int[] dimensionSizes, String dataType, Units u, ModemContainer modem, Backend ros) throws StructuralException{

		init(node, dynamics, integrator, name, dimensionSizes, dataType, u, modem, ros);
	}


	private void init(Node node, DynamicalSystem dynamics, Integrator integrator,
			String name, int[] dimensionSizes, String dataType, Units u, 
			ModemContainer modem, Backend ros) throws StructuralException{
		myNode = node;
		myDynamics = dynamics;
		myIntegrator = integrator;
		myName = name;
		
		// connect to its part which was launched as a ROS node
		try {
			myRosNode = modem.getConnectedNode();
		} catch (ConnectionException e) {
			System.err.println(me+" my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}	// possibly wait for Modem to init

		if(dimensionSizes.length>1 || dimensionSizes[0]>1){
			String mess = me+"ERROR: multidimensional terminations are not supported so far!";
			System.err.println(mess);
			throw new StructuralException(mess);
		}
		
		this.ros = ros;	// get my ROS backend
		
		// Here, add new MultiTermination which sums inputs on all own Terminations together.
		multiTermination  = new SumMultiTermination(myNode, name, DIM, myIntegrator, myDynamics);
		// add one termination on the start (usable by GUI, has default weight of 1)
		multiTermination.addTerminaton();
	}


	/**
	 * Collect data on my input (on all my Terminations), encode data and publish over the ROS network.
	 * 
	 * @param startTime simulation time at which running starts
	 * @param endTime simulation time at which running ends
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime) throws SimulationException {
		
		multiTermination.run(startTime, endTime);

		myOutput = multiTermination.getOutput();
		float[][] ff_series = myOutput.getValues();

		// publish as a ROS message
		// TODO: send entire TimeSeries over the ROS network
		ros.publish(ff_series[0]);
	}

	/**
	 * Typically called by the Node to which the Termination belongs, 
	 * here this method is probably unused, everything is handled just 
	 * in the method run(). The received values are encoded there and
	 * sent over the ROS network.
	 *
	 * @return The most recent input on Terminations
	 */
	public TimeSeries getOutput() { return myOutput; }

	public int getDimensions() { return myDynamics.getInputDimension(); }
	
	public String getName() { return myName; }

	public Node getNode() { return myNode; }
	
	public boolean getModulatory() { return false; }

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize){
		multiTermination.reset(randomize);
	}

	public float getTau() {
		if (myDynamics instanceof LTISystem) {
			return CanonicalModel.getDominantTimeConstant((LTISystem) myDynamics);
		} else {
			ourLogger.warn("Can't get time constant for non-LTI dynamics. Returning 0.");
			return 0;
		}
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


	/*
	@Override
	public Encoder clone() throws CloneNotSupportedException {
		AbstractEncoder result = (AbstractEncoder) super.clone();
		result.myDynamics = myDynamics.clone();
		result.myIntegrator = myIntegrator.clone();
		result.myInput = myInput.clone();
		result.myOutput = myOutput.clone();
		return result;
	}*/



}

