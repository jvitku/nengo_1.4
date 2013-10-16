package ctu.nengoros.exceptions;

import ca.nengo.NengoException;
/**
 * Should deal with unsupported format of ROS messages.
 * 
 * 
 * @author Jaroslav Vitku
 *
 */
public class UnsupportedMessageFormatExc extends NengoException{

	private static final long serialVersionUID = -259808090703993191L;

	public UnsupportedMessageFormatExc(String myName, String format){
		super("Given message format is not supported so far!! " +
				"My name is: "+myName+" message format: "+format+
				" please, add the message format to DataTypesMap and " +
				"implement necessary transformations");
	}
	
}
