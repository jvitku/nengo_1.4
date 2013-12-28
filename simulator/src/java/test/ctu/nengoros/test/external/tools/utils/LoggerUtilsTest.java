package ctu.nengoros.test.external.tools.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ctu.nengoros.tools.utils.LU;

public class LoggerUtilsTest {

	@Test
	public void vector(){

		float[] a = new float[]{0,1,2,3,3};
		String s = LU.toStr(a);
		assertTrue(s.equalsIgnoreCase("[0.0 1.0 2.0 3.0 3.0]"));
	}

	@Test
	public void twoDimMatrix(){
		float[][] a = new float[][]{{0,1,2,3,3},{1,2,3,4,5}};
		String s = LU.toStr(a);
		assertTrue(s.equalsIgnoreCase("[0.0 1.0 2.0 3.0 3.0;\n 1.0 2.0 3.0 4.0 5.0]"));
	}
	
	public static void main(String[] args){
		LoggerUtilsTest t = new LoggerUtilsTest();
		t.twoDimMatrix();
		t.vector();
		/*
		Logger l = new Logger("",true,true);
		*/
	}
	
}
