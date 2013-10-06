
/*
 * Created on May 23, 2006
 */
package nengoros.exceptions;

import ca.nengo.NengoException;

/**
 * A problem encountered while trying to establish some connection 
 * between Nengo (some Modem, launching the Node..) and ROS (rosjava core, external node..).
 *    
 * @author Jaroslav Vitku (based on nengo.ca.model.SimulationException)
 */
public class ConnectionException extends NengoException {

	private static final long serialVersionUID = 1207471167026778293L;

	/**
	 * Just say where it happened and why..
	 * @param myName
	 * @param cause
	 */
	public ConnectionException(String myName, String cause){
		super("Connection problem!! My name is: "+myName+" problem is: "+cause);
	}
	
	/**
	 * @param message Text explanation of the exception. 
	 */
	public ConnectionException(String message) {
		super(message);
	}

	/**
	 * @param cause Another throwable that indicates a problem underlying this 
	 * 		exception.  
	 */
	public ConnectionException(Throwable cause) {
		super(cause); 
	}

	/**
	 * @param message Text explanation of the exception. 
	 * @param cause Another throwable that indicates a problem underlying this 
	 * 		exception.  
	 */
	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
	 
}
