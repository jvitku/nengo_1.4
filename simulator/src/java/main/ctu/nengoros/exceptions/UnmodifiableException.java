package ctu.nengoros.exceptions;

import ca.nengo.NengoException;

/**
 * Raised in attempt to change value of some final component.
 * 
 * @author Jaroslav Vitku
 *
 */
public class UnmodifiableException extends NengoException {

	private static final long serialVersionUID = 1207471167026778293L;

	/**
	 * Just say where it happened and why..
	 * @param myName
	 * @param cause
	 */
	public UnmodifiableException(String myName, String cause){
		super("UnmodifiableException! Cannot change component of: "+myName+", cause: "+cause);
	}
	
	/**
	 * @param message Text explanation of the exception. 
	 */
	public UnmodifiableException(String message) {
		super(message);
	}

	/**
	 * @param cause Another throwable that indicates a problem underlying this 
	 * 		exception.  
	 */
	public UnmodifiableException(Throwable cause) {
		super(cause); 
	}

	/**
	 * @param message Text explanation of the exception. 
	 * @param cause Another throwable that indicates a problem underlying this 
	 * 		exception.  
	 */
	public UnmodifiableException(String message, Throwable cause) {
		super(message, cause);
	}
	 
}
