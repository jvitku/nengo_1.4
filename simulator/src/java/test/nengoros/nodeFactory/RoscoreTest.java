package nengoros.nodeFactory;

import static org.junit.Assert.*;

import nengoros.comm.rosutils.Mess;

import org.junit.Test;
import org.ros.MyRoscore;

public class RoscoreTest {
	
	@Test
	public void startStop(){
		assertFalse(MyRoscore.running());
		
		MyRoscore.start();
		
		Mess.wait(1);
		assertTrue(MyRoscore.running());
		
		MyRoscore.stop();
		Mess.wait(1);
		
		assertFalse(MyRoscore.running());
	}

}
