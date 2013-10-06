package tools.utils;

/**
 * If you want to format something for printing, use this
 * 
 * @author Jaroslav Vitku
 *
 */
public class LU {
	
	public static String toStr(float[] arr){
		String out = "[";
		for(int i=0;i<arr.length; i++){
			if(i==arr.length-1)
				out += arr[i]+"]";
			else
				out += arr[i]+" ";
		}
		return out;
	}
	
	public static String toStr(int[] arr){
		String out = "[";
		for(int i=0;i<arr.length; i++){
			if(i==arr.length-1)
				out += arr[i]+"]";
			else
				out += arr[i]+" ";
		}
		return out;
	}
	
	public static String toStr(float[][] arr){
		String out = "[";
		for(int i=0; i<arr.length; i++){
			
			for(int j=0; j<arr[0].length; j++){
				if(j < arr[0].length-1)
					out+=arr[i][j]+" ";
				else if(i < arr.length-1)
					out+=arr[i][j]+";\n ";
				else
					out+=arr[i][j]+"]";
			}
		}
		return out;
	}
}
