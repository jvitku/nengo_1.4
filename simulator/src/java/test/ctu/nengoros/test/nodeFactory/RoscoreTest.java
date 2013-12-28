package ctu.nengoros.test.nodeFactory;

import static org.junit.Assert.*;

import org.junit.Test;
//import org.ros.MyRoscore;

import ctu.nengoros.comm.rosutils.Jroscore;
import ctu.nengoros.comm.rosutils.Mess;

public class RoscoreTest {
	
	@Test
	public void startStop(){
		
		assertFalse(Jroscore.running());
		
		Jroscore.start();
		
		Mess.wait(1);
		assertTrue(Jroscore.running());
		
		Jroscore.stop();
		Mess.wait(1);
		
		assertFalse(Jroscore.running());
	}

}
