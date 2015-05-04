package ctu.nengorosHeadless.network.modules.io;

/**
 * Origin or Termination.
 * 
 * @author Jaroslav Vitku
 *
 */
public interface IO {

	/**
	 * Can be used for unique addressing in the layered connections
	 * @return name composed of parents name and my name
	 */
	public String getUniqueName();
	
	/**
	 * Should correspond to the owned ROS topic.
	 * @return name of this origin
	 */
	public String getName();
	
	/**
	 * @return dimension of this input/output
	 */
	public int getSize();
}
