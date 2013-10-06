package nengoros.exceptions;

import ca.nengo.NengoException;
/**
 * Should deal with bad formatting of ROS messages and their parsing 
 * by Nengoros Reshapers and Rescalers (generally modems). 
 * 
 * @author Jaroslav Vitku
 *
 */
public class MessageFormatException extends NengoException{

	private static final long serialVersionUID = -259808090703993191L;

	public MessageFormatException(String myName, String cause){
		super("Message has a bad format!! My name is: "+myName+" problem is: "+cause);
	}
	
}
