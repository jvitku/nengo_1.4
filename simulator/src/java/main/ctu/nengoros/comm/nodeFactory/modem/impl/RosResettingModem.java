package ctu.nengoros.comm.nodeFactory.modem.impl;

import org.ros.node.ConnectedNode;

import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.network.node.infrastructure.simulation.SimulationController;

/**
 * The same as the {@link DefaultModem}, but this supports resetting 
 * of own ROS nodes by means of sending the reset request over the ROS 
 * network.
 * 
 * The downside is that this modem expects ROS nodes to support the
 * reset requests.
 * 
 * @see ctu.nengoros.network.node.ConfigurableHannsNode#registerSimulatorCommunication(ConnectedNode connectedNode)
 * 
 * @author Jaroslav Vitku
 *
 */
public class RosResettingModem extends DefaultModem{

	private SimulationController controller;

	@Override
	public void onStart(ConnectedNode connectedNode) { 
		log = connectedNode.getLog();
		myRosSide = connectedNode;

		// register the ROS simulation controllers publisher
		controller = new SimulationController(myName, log, connectedNode);

		System.out.println(me+" starting..!");
		log.info(me+" starting..!");
	}

	/**
	 * Send a ROS message which tells the ROS nodes to be reseted.
	 * The message is published on the 
	 * {@link ctu.nengoros.network.node.infrastructure.simulation.Messages#SIMULATOR_TOPIC}, 
	 * topic, but the topic name is pushed into the groups namespace, 
	 * so nodes in other groups will not get reseted.
	 * 
	 *   TODO: so far, there is no synchronization/waiting for the reset 
	 */
	@Override
	public void reset(boolean randomize) {
		try {
			this.getStartupManager().awaitStarted();
		} catch (StartupDelayException e) {
			System.err.println(me+" Reset error: waited too long for ROS node " +
					"to initialize! not resetting!");
			return;
		}
		//System.out.println(myName+" resetting my nodes now.");
		this.controller.callHardReset(randomize);
	}
	

	@Override
	public boolean isStarted() {
		return (this.myRosSide != null && this.log != null && this.controller!=null);
	}

}
