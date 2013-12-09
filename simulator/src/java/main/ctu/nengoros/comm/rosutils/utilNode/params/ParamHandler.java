package ctu.nengoros.comm.rosutils.utilNode.params;

import ctu.nengoros.rosparam.Rosparam;

/**
 * Allows Nengo to set/read parameters from the parameter server (ROS master).
 * 
 * This could be implemented as one ROS node, but due to problems with timeProvider
 * ( @see: ctu.nengoros.time.AbstractTimeNode , @see http://code.google.com/p/rosjava/issues/detail?id=148 )
 * both, Rosparam and TimeHandler will be implemented as separate nodes, where TimeHandler 
 * may not bee launched at all.  
 * 
 * @author Jaroslav Vitku
 *
 */
public class ParamHandler extends Rosparam{
	
	

}
