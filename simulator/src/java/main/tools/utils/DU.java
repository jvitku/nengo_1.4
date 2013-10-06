package tools.utils;

import java.io.IOException;

/**
 * Just simple debug utils
 * 
 * @author Jaroslav Vitku
 *
 */
public class DU {
	
	public static void p(String what){
		System.out.print(what);
	}
	
	public static void pl(String what){
		System.out.println(what);
	}
	
	public static void read(){
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
