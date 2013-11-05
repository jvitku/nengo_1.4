package ctu.nengoros.comm.rosBackend.backend;

import ctu.nengoros.exceptions.MessageFormatException;
import ctu.nengoros.exceptions.UnsupportedMessageFormatExc;

/**
 * This stores short versions of supported ROS data types. 
 * When you define origins/terminations of SmartNeuron in Jython, 
 * you will be able to use either exact names of ROS data types
 * (e.g. std_msgs.Float32MultiArray) or their shortened versions
 * (e.g. float), which will be mapped to ROS format of messages.
 *  
 * @author Jaroslav Vitku
 *
 */
public class DataTypesMap {

	/**
	 * These arrays map short versions of data types to ROS versions, 
	 * e.g. float -> std_msgs.Float32MultiArray
	 */
	private static final String[] shortNames = new String[]{
		"float",
		"int",
		"velocity",
		"pose",
		"color",
		"bool",
		"twist"
	};
	
	private static final String[] rosNames = new String[]{
		"std_msgs/Float32MultiArray",
		"std_msgs/Int32MultiArray",
		"turtlesim/Velocity",
		"turtlesim/Pose",
		"turtlesim/Color",
		"std_msgs/Bool",
		"geometry/Twist"
	};
	
	
	/**
	 * Lookup if given data format is of supported ROS type, if not,
	 * check if it is shortened version of supported data type, if 
	 * not, throw exception. 
	 *  
	 * @param what
	 * @throws MessageFormatException
	 */
	public static String getType(String what) throws UnsupportedMessageFormatExc{
		if(isLongName(what))
			return what;
		if(!isShortName(what))
			throw new UnsupportedMessageFormatExc("DataTypesMap",what);
		return getLongName(what);
	}
	
	private static boolean isLongName(String what){
		for(int i=0; i<rosNames.length; i++)
			if(rosNames[i].equalsIgnoreCase(what))
				return true;
		return false;
	}
	
	private static boolean isShortName(String what){
		for(int i=0; i<shortNames.length; i++)
			if(shortNames[i].equalsIgnoreCase(what))
				return true;
		return false;
	}
	
	private static String getLongName(String what){
		for(int i=0; i<shortNames.length; i++)
			if(shortNames[i].equalsIgnoreCase(what))
				return rosNames[i];
		return null;
	}
}
