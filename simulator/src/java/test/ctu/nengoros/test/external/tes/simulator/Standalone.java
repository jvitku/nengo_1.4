package ctu.nengoros.test.external.tes.simulator;

import ctu.nengoros.test.external.service.simulator.SimulatorController;
import ctu.nengoros.test.external.service.simulator.SimulatorX;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests the starting and stopping the simulation..
 * 
 * @author Jaroslav Vitku
 *
 */
public class Standalone {
	
	public static void main(String[] args){
		
		SimulatorController sc = new SimulatorController();
		SimulatorX sim = new SimulatorX("testSim",sc);
		sc.setMySimulator(sim);

		System.out.println("! initing.. ");
		assertFalse(sc.isInited());
		sc.init();
		System.out.println("! inited..starting");
		assertTrue(sc.isInited());
		assertFalse(sc.isRunning());
		sc.start();
		assertTrue(sc.isRunning());
		
		System.out.println("! started...waiting.. ");
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) { e.printStackTrace();	}
		
		System.out.println("! stoppping");
		
		sc.stop();
		assertFalse(sc.isRunning());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("! startning again ");
		sc.start();
		assertTrue(sc.isRunning());

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sc.stop();
		assertFalse(sc.isRunning());

	}

}
