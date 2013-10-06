package nengoros.comm.rosutils;

import java.util.ArrayList;
import java.util.List;

/**
 * Just some debugging (mainly printing out arrays) utilities
 * 
 * @author Jaroslav Vitku
 *
 */
public class Mess {

	public static String toAr(String[] what){
		String out = "";
		for(String s:what){
			out = out+s+" ";
		}
		return out;
	}
	
	public static String toAr(List<String> what){
		String out = "";
		
		for(int i=0; i<what.size(); i++){
			out = out+what.get(i)+" ";
		}
		return out;
	}
	
	public static String toAr(ArrayList<String> what){
		String out = "";
		
		for(int i=0; i<what.size(); i++){
			out = out+what.get(i)+" ";
		}
		return out;
	}
	
	public static void wait(int sec){
		try {
			Thread.sleep(sec*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void waitForKey(){
		System.out.println("Hey.. Press any key to continue");
		try{
			System.in.read();
		}catch(Exception e){}
	}
}
