package ctu.nengoros.test.module;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import ctu.nengoros.comm.rosutils.RosUtils;

/**
 * This is how the ROS core is started/stopped during the use of Nengoros.
 * 
 * The utilsShallStart is called when some ROS component is started, utilsShallStop()
 * is then called during exiting the application, this also terminates all forgotten
 * ROS nodes.  
 * 
 * @author Jaroslav Vitku
 *
 */
public class NengorosTest {

	/**
	 * Called before any unit @Test
	 */
	@BeforeClass
	public static void startCore(){
		
		RosUtils.setAutorun(true);
		
		// the core is automatically launched during attempt to start any ROS component
		//RosUtils.utilsShallStart();
	}

	/**
	 * Called after all unit @Test s
	 */
	@AfterClass
	public static void stopCore(){
		RosUtils.utilsShallStop();
	}

}
