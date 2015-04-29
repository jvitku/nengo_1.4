package ctu.nengorosHeadless.rosBackend.encoders.impl;

import org.ros.node.ConnectedNode;

import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.impl.BasicTerm;
import ctu.nengorosHeadless.rosBackend.encoders.Encoder;

public class BasicEnc extends BasicTerm implements Encoder {

	protected NeuralModule parent;
	protected ConnectedNode myRosNode;			// factory for subscriber
	public Backend ros;							// Nengo interfaced with ROS here

	/**
	 * Constructor of Basic Encoder which can hold one or more Terminations. Create MultiTermination, 
	 * register one own Termination by default and wait for running. 
	 * 
	 * Here, the sizes of particular dimensions are defined by integer array.
	 * 
	 * @param parent NeuralModule to which this belongs (will register Terminations to it)
	 * @param dimensionsizes sizes of each dimension
	 * @param dynamics defines dynamics of all Terminations registered and used by me
	 * @param integrator defines dynamics of all my Terminations
	 * @param name name of this Encoder (also name of my ROS topic and base name for my Terminations) 
	 * @param dataType type of data to decode
	 * @param u Nengo units used here
	 * @param modem my modem, should be already running
	 * @param ros ROS backend which defines how Nengo RealOutput is converted into ROS messages
	 * @throws StructuralException e.g. problem with registering the Termination 
	 * @throws ConnectionException ROS network problem
	 * 
	 * @see ctu.nengoros.comm.rosBackend.backend.impl
	 */
	public BasicEnc(NeuralModule parent, int size, String name, String dataType, ModemContainer modem, Backend ros) 
					throws StructuralException, ConnectionException, StartupDelayException{

		super(size, name);
		init(parent, new int[]{size}, name, dataType, modem, ros);
	}
	
	public BasicEnc(NeuralModule parent, String name, String dataType, ModemContainer modem, Backend ros) 
			throws StructuralException, ConnectionException, StartupDelayException{
		
		super(ros.gedNumOfDimensions(), name);
		init(parent, new int[]{super.getSize()}, name, dataType, modem, ros);	
	}
	
	public BasicEnc(NeuralModule parent, String name, String dataType, ModemContainer modem, Backend ros, float defVal) 
			throws StructuralException, ConnectionException, StartupDelayException{
		
		super(ros.gedNumOfDimensions(), name, defVal);
		init(parent, new int[]{super.getSize()}, name, dataType, modem, ros);	
	}

	private void init(NeuralModule parent, int[] dimensionSizes, String name, String dataType, ModemContainer modem, Backend ros) 
					throws ConnectionException, StructuralException, StartupDelayException{

		this.parent = parent;

		// connect to its part which was launched as a ROS node
		try {
			myRosNode = modem.getConnectedNode();
		} catch (ConnectionException e) {
			String mess = "my modem was not connected. Probably ROS communication error!!";
			System.err.println(super.getName()+" "+mess);
			throw new ConnectionException(super.getName()+" "+mess, e);
		}

		this.ros = ros;	// get my ROS backend
		parent.addEncoder(this);
	}

	@Override
	public void run(float startTime, float endTime) throws SimulationException {

		//System.out.println("OK I am "+this.getName()+" and publishing this: "+SL.toStr(super.getValues()));
		
		// publish as a ROS message with the last data sample available
		ros.publish(super.getValues());
	}

	@Override
	public NeuralModule getParent() { return parent; }

}
