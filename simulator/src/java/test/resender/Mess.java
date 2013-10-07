package resender;

import java.util.ArrayList;
import java.util.List;

/**
 * Printing out common (error) messages
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
}
