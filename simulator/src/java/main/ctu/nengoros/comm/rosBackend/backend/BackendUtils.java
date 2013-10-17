package ctu.nengoros.comm.rosBackend.backend;

import java.util.List;

import org.ros.node.ConnectedNode;

import ctu.nengoros.comm.rosBackend.backend.impl.BooleanBackend;
import ctu.nengoros.comm.rosBackend.backend.impl.FloatBackend;
import ctu.nengoros.comm.rosBackend.backend.impl.IntBackend;
import ctu.nengoros.comm.rosBackend.backend.impl.turtle.ColorBackend;
import ctu.nengoros.comm.rosBackend.backend.impl.turtle.PoseBackend;
import ctu.nengoros.comm.rosBackend.backend.impl.turtle.VelocityBackend;
import ctu.nengoros.exceptions.MessageFormatException;
import ctu.nengoros.exceptions.UnsupportedMessageFormatExc;
import std_msgs.MultiArrayDimension;

public class BackendUtils {

	/**
	 * Here are backends which operate over the message types with 
	 * user-definable data dimensinality.
	 *  
	 * @param topic
	 * @param messageType
	 * @param dimensionSizes
	 * @param myRosNode
	 * @param publish
	 * @return
	 * @throws MessageFormatException
	 * @throws UnsupportedMessageFormatExc
	 */
	public static Backend select(String topic, String messageType, 
			int[] dimensionSizes, ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException, UnsupportedMessageFormatExc{
		
		String type = DataTypesMap.getType(messageType);
		
		/**
		 * Add your ROS backend (which deals with own ROS data type here.
		 */
		if(type.equalsIgnoreCase(FloatBackend.MYTYPE))
			return new FloatBackend(topic, type, dimensionSizes, myRosNode, publish);
		
		if(type.equalsIgnoreCase(IntBackend.MYTYPE))
			return new IntBackend(topic, type, dimensionSizes, myRosNode, publish);
		
		if(type.equalsIgnoreCase(BooleanBackend.MYTYPE))
			return new BooleanBackend(topic, type, dimensionSizes, myRosNode, publish);
		
		throw new MessageFormatException("BackendUtils "+topic, 
				"Could not find corresponding Backend for this type of message "+type);
	}
	
	/**
	 * Here are message types which have predefined number of dimensions (e.g. color, velocity..)
	 * 
	 * @param topic
	 * @param messageType
	 * @param myRosNode
	 * @param publish
	 * @return
	 * @throws MessageFormatException
	 * @throws UnsupportedMessageFormatExc
	 */
	public static Backend select(String topic, String messageType,ConnectedNode myRosNode, boolean publish) 
					throws MessageFormatException, UnsupportedMessageFormatExc{
		
		String type = DataTypesMap.getType(messageType);
		
		if(type.equalsIgnoreCase(VelocityBackend.MYTYPE))
			return new VelocityBackend(topic, type, myRosNode, publish);
		
		if(type.equalsIgnoreCase(PoseBackend.MYTYPE))
			return new PoseBackend(topic, type, myRosNode, publish);
		
		if(type.equalsIgnoreCase(ColorBackend.MYTYPE))
			return new ColorBackend(topic, type, myRosNode, publish);
		
		
		throw new MessageFormatException("BackendUtils "+topic, 
				"Could not find corresponding Backend for this type of message "+type);
	}

	
	/**
	 * This converts (Jython) description of dimension sizes into length of 
	 * single vector for Nengo.  
	 * 
	 * @param dimensionSizes each number represents size of a dimension (e.g. 640x480)
	 * @return number of elements in data 
	 */
	public static int countNengoDimension(int[] dimensionSizes) throws MessageFormatException{
		if(dimensionSizes.length == 0)
			throw new MessageFormatException("countNengoDimension","Cannot parse dimensionSizes of length 0! " +
					"check your Jython script probably.."); 
		int out=dimensionSizes[0];
		for(int i=1; i<dimensionSizes.length; i++)
			out = out*dimensionSizes[1];
		return out;
	}
	
	/**
	 * This is experimental and it should set the layout of ROS message
	 * according to dimensionSizes parsed from Jython script.
	 * Until now, it has not been tested enough. 
	 * @param d list of dimensionSizes obtained bt rosMessage.getLayout(), it is empty..
	 * @param dimensionSizes information parsed from Jython
	 * @return layout of ROS message filled with data from Jython
	 */
	public static List<MultiArrayDimension> setRosDimensions(){
			//TODO: later.. 
			return null;
	}
	
	

	
}
