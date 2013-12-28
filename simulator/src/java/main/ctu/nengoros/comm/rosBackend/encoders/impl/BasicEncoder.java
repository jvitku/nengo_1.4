package ctu.nengoros.comm.rosBackend.encoders.impl;

import org.ros.node.ConnectedNode;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.encoders.Encoder;
import ctu.nengoros.comm.rosBackend.encoders.multiTermination.MultiTermination;
import ctu.nengoros.comm.rosBackend.encoders.multiTermination.impl.SumMultiTermination;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.NeuralModule;

public class BasicEncoder implements Encoder{

	public static final String me = "[BasicEncoder] ";

	// all my Terminations are here
	protected MultiTermination multiTermination;
	
	// common properties of my Terminations
	protected int dimensions;
	protected DynamicalSystem dynamics; 
	protected Integrator integrator;

	// variables for this Encoder
	protected NeuralModule parent;				
	protected String name;		
	protected ConnectedNode myRosNode;	// factory for subscriber
	protected MultiTermination mt;		// this combines values on all Terminations
	
	public Backend ros;					// Nengo interfaced with ROS here
	
	/**
	 * Constructor of Basic Encoder which can hold one or more Terminations. Create MultiTermination, 
	 * register one own Termination by default and wait for running. 
	 *  
	 * @param parent NeuralModule to which this belongs (will register Terminations to it) 
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
	public BasicEncoder(NeuralModule parent, DynamicalSystem dynamics, Integrator integrator, 
			String name, String dataType, Units u, ModemContainer modem, Backend ros) 
					throws StructuralException, ConnectionException{
		
		init(parent, new int[]{ros.gedNumOfDimensions()}, dynamics, integrator, name, dataType, u, modem, ros);	
	}
	
	/**
	 * Constructor of Basic Encoder which can hold one or more Terminations. Create MultiTermination, 
	 * register one own Termination by default and wait for running. 
	 * 
	 * Here, the sizes of particular dimensions are defined by integer array.
	 * 
	 * 
	 * @param parent NeuralModule to which this belongs (will register Terminations to it)
	 * @param dimensionsizes sizes of each dimension, TODO probably
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
	public BasicEncoder(NeuralModule parent, int[] dimensionsizes, DynamicalSystem dynamics, Integrator integrator, 
			String name, String dataType, Units u, ModemContainer modem, Backend ros) 
					throws StructuralException, ConnectionException{
		
		init(parent, dimensionsizes, dynamics, integrator, name, dataType, u, modem, ros);
	}
	
	private void init(NeuralModule parent, int[] dimensionSizes, DynamicalSystem dynamics, Integrator integrator, 
			String name, String dataType, Units u, ModemContainer modem, Backend ros) 
					throws ConnectionException, StructuralException{
		
		this.parent = parent;
		this.name = name;
		this.dynamics = dynamics;
		this.integrator = integrator;
		
		dimensions = ros.gedNumOfDimensions();
		
		// connect to its part which was launched as a ROS node
		try {
			myRosNode = modem.getConnectedNode();
		} catch (ConnectionException e) {
			String mess = "my modem was not connected. Probably ROS communication error!!";
			System.err.println(me+mess);
			throw new ConnectionException(me+mess, e);
		}
/*
 * TODO check this
		if(dimensions>1){
			// TODO this is not necessary here
			String mess = me+"ERROR: currently, only one dimensinal vectors of values are supported!";
			System.err.println(mess);
			throw new StructuralException(mess);
			}
			*/
		
		
		this.ros = ros;	// get my ROS backend

		// Here, add new MultiTermination which sums inputs on all own Terminations together.
		multiTermination  = new SumMultiTermination(
				this.parent, this.name, dimensions, this.integrator, this.dynamics);
		
		System.out.println(me+"adding new Termination to my multitermination");
		// add one termination on the start (usable by GUI, has default weight of 1)
		multiTermination.addTerminaton();
	}
	
	@Override
	public String addTermination() throws StructuralException {
		// ad Termination to my MultiTermination and return its name
		return mt.addTerminaton();
	}

	@Override
	public void run(float startTime, float endTime) throws SimulationException {
		
		// collect data on all my Terminations
		mt.run(startTime, endTime);
		float[][] ff_series = mt.getOutput().getValues();
		
		// publish as a ROS message
		// TODO: send entire TimeSeries over the ROS network, not just one time sample
		ros.publish(ff_series[0]);
	}
	
	@Override
	public void reset(boolean randomize) {
		multiTermination.reset(randomize);
	}

	@Override
	public NeuralModule getParent() { return parent; }

	@Override
	public String getName() { return this.name; }

	@Override
	public int getDimensions() { return dimensions; }

}
