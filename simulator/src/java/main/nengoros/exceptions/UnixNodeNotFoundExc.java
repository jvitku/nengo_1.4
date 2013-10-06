
package nengoros.exceptions;

import ca.nengo.NengoException;

/**
 * Is thrown if unix (non-java) node that should be launched is not found.
 * @author Jaroslav Vitku
 */
public class UnixNodeNotFoundExc extends NengoException {

	private static final long serialVersionUID = 1207471167026778293L;

	/**
	 * Just say where it happened and why..
	 * @param myName
	 * @param cause
	 */
	public UnixNodeNotFoundExc(String myName, String cause){
		super("Unix was not found! : "+myName+" complete name of node to launch is: "+cause);
	}
	
	/**
	 * @param message Text explanation of the exception. 
	 */
	public UnixNodeNotFoundExc(String message) {
		super(message);
	}

	/**
	 * @param cause Another throwable that indicates a problem underlying this 
	 * 		exception.  
	 */
	public UnixNodeNotFoundExc(Throwable cause) {
		super(cause); 
	}

	/**
	 * @param message Text explanation of the exception. 
	 * @param cause Another throwable that indicates a problem underlying this 
	 * 		exception.  
	 */
	public UnixNodeNotFoundExc(String message, Throwable cause) {
		super(message, cause);
	}
	 
}
