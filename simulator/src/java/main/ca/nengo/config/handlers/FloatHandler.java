/*
 * Created on 17-Dec-07
 */
package ca.nengo.config.handlers;

/**
 * ConfigurationHandler for Float values. 
 * 
 * @author Bryan Tripp
 */
public class FloatHandler extends BaseHandler {

	public FloatHandler() {
		super(Float.class);
	}

	/**
	 * @see ca.nengo.config.ConfigurationHandler#getDefaultValue(java.lang.Class)
	 */
	public Object getDefaultValue(Class c) {
		return new Float(0);
	}

}