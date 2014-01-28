package ctu.nengoros.test.module;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import ca.nengo.model.SimulationException;

import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.util.sync.impl.SyncedUnit;

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

	private static int maxWait = 3000;

	/**
	 * Simulate the simulation step of the Nengo: call run() and wait for 
	 * Module to be ready. 
	 * @param start start time (not important here)
	 * @param end (not important here)
	 * @param module Module that is waited to
	 */
	public static void makeSimStep(float start, float end, NeuralModule module){
		try {
			module.run(0, 1); // make the simulation step
		} catch (SimulationException e) {
			System.out.println("failed to run the module");
			e.printStackTrace();
			fail();
		}
		// after this, the values on all Origins (with synchronous Decoders) will be available
		waitForModuleReady(module);
	}


	/**
	 * The NeuralModule is synchronous by default, this does the modified Nengo simulator core
	 * for all NeuralModules (waits for they to be ready, for all their Decoders to be ready).
	 * Decoder is ready when it receives the ROS message (each time step, the ready state of
	 * all decoders is discarded).
	 */
	private static void waitForModuleReady(NeuralModule module){
		int waited = 0;
		System.out.print("\n waiting for the ROS message to be received");
		while(!((SyncedUnit)module).isReady()){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace(); }
			System.out.print(".");

			waited +=10;
			if(waited > maxWait){
				System.err.println("\n\nNeuralModule not ready fast enough! ROS communication probably broken!");
				fail();
			}
		}
		System.out.println("");
	}

}
