package ctu.nengoros.comm.nodeFactory.modem.impl;

/**
 * This is TODO: during the call of {@link #reset(boolean)} method, 
 * the modem should be able to stop and start all own ROS nodes.
 * 
 *  This will be support for ROS nodes which do not support reset topic. 
 * 
 * @author Jaroslav Vitku
 *
 */
public class StartStopResettingModem extends DefaultModem{
	
	@Override
	public void reset(boolean randomize){
		System.out.println("TODO: here, all external nodes should be " +
				"stopped and started.");
	}

}
