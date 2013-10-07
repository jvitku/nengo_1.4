package nengoros.nodeFactory;

import static org.junit.Assert.*;

import nengoros.comm.rosutils.Mess;

import org.junit.Test;
//import org.ros.MyRoscore;
import nengoros.comm.rosutils.Jroscore;

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
