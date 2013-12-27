package ctu.nengoros.comm.rosBackend.encoders;

import org.apache.log4j.Logger;
import org.ros.node.ConnectedNode;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.encoders.Encoder;
import ctu.nengoros.exceptions.ConnectionException;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.CanonicalModel;
import ca.nengo.dynamics.impl.LTISystem;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.model.impl.BasicTermination;
import ca.nengo.util.TimeSeries;

/**
 * Encoder with 
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class AbstractEncoder implements Encoder{

	private static final long serialVersionUID = 1L;

	private static Logger ourLogger = Logger.getLogger(BasicTermination.class);

	protected Node myNode;
	protected DynamicalSystem myDynamics;
	protected Integrator myIntegrator;
	protected String myName;
	protected InstantaneousOutput myInput;
	protected TimeSeries myOutput;
	protected boolean myModulatory;
	protected ConnectedNode myRosNode;	// factory for subscriber

	public /*final */Backend ros; 


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
	public AbstractEncoder(Node node, DynamicalSystem dynamics, Integrator integrator,
			String name, String dataType, Units u, ModemContainer modem, Backend ros) throws StructuralException{

		init(node, dynamics, integrator, name, new int[]{ros.gedNumOfDimensions()}, dataType, u, modem, ros);
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
	public AbstractEncoder(Node node, DynamicalSystem dynamics, Integrator integrator,
			String name, int[] dimensionSizes, String dataType, Units u, ModemContainer modem, Backend ros) throws StructuralException{

		init(node, dynamics, integrator, name, dimensionSizes, dataType, u, modem, ros);
	}


	/**
	 * Initialize the Encoder, called from the constructor.
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
	private void init(Node node, DynamicalSystem dynamics, Integrator integrator,
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
			System.err.println("AbstractEncoder: my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}	// possibly wait for Modem to init

		this.ros = ros;	// get my ROS backend
	}

	/**
	 * Implement this to collect values on Termination(s) inputs, encode values 
	 * and send over the ROS network.
	 */
	public abstract void run(float startTime, float endTime) throws SimulationException;

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
	public abstract void reset(boolean randomize);
	/*{
			myInput = null;
		}*/

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
		AbstractEncoder result = (AbstractEncoder) super.clone();
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
