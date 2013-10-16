package ctu.nengoros.comm.rosutils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;




import ctu.nengoros.comm.rosutils.Mess;

import org.jdesktop.swingx.util.OS;


//TODO: change this to its own thread which is subscribed to both streams..
public class ProcessLauncher {

	public static final String me = "[ProcessLauncher] ";

	private static final boolean chatty = true;

	/**
	 * checks OS type and prints out wise message, then launches Process and returns it
	 * @param launchCommand how and what to launch
	 * @return running Process if everything OK
	 */
	public static Process launchProcess(String[] launchCommand){

		checkOS(launchCommand);

		Process p = startProcess(launchCommand);
		return p;
	}

	public static void checkOS(List<String> launchCommand){
		checkOS(launchCommand.toArray(new String[launchCommand.size()]));
	}
	
	public static void checkOS(String[] launchCommand){
		if(OS.isMacOSX()){
			if(chatty)
				System.out.println(me+" OK, OS X found, this could work, but I bet you do " +
						"not have ROS installed, so turtlesim will not work here..");
		}else if(OS.isWindows()){
			System.err.println(me+" Oh.. Support of launching native processes on "
					+"Windows is TODO still.. this will not work, try to use java code instead");
		}else{
			if(chatty)
				System.out.println(me+" Linux found, OK..");
		}

		if(!appExists(launchCommand)){
			System.err.println(me+" It seems that this application does not exist! " +
					" Is the path right? My PWD is now: "+printPWD());
		}
	}
	
	public static Process launchProcess(List<String> launchCommand){
		return launchProcess(toStr(launchCommand));
	}

	/**
	 * Whether we are able to launch Unix processes
	 * @return
	 */
	public static boolean isUnix(){
		return (OS.isLinux() || OS.isMacOSX());
	}

	/**
	 * This should check (by means of Unix command "which") whether 
	 * the application probably exists. 
	 * @return true if which [name of app] returns non-empty string
	 */
	public static boolean appExists(String[] lc){
		String s;
		if(lc.length<1)
			return false;
		
		// TODO: no support for win so far..
		if(!isUnix())
			return false;
		
		s = getOutput(new String[]{"which",lc[0]});	//e.g. "which roscore" should return something

		if(chatty){
			if(s.length()>0){
				System.out.println(me+"application '"+lc[0]+"' found on this system.");
			}else{
				if(!lc[0].equalsIgnoreCase("rxgraph") && !lc[0].equalsIgnoreCase("roscore"))
					System.err.println(me+"application not found! Command 'which "+lc[0]+"' returned" +
						" empty string. My PWD is: "+printPWD());
				else
					System.err.println(me+lc[0]+" not found on this system.");
			}
		}
		return (s.length()>0);
	}
	
	/**
	 * User can specify command as array of strings or space-separated string
	 * @param appName command to be executed
	 * @return
	 */
	public static boolean appExists(String appName){
		return appExists(appName.split("\\s+"));
	}

	public static Process startProcess(String comm){
		return startProcess(comm.split(" "));
	}
	
	public static Process startProcess(String[] comm) {
		try {
			
			Process proc = Runtime.getRuntime().exec(comm);
			if(chatty)
				System.out.println(me+Mess.toAr(comm)+" ....started OK");
			return proc;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.err.println(me+Mess.toAr(comm)+" ....could not be started!");
		return null;
	}

	/**
	 * launch command and get commandline output
	 * @param comm command to launch
	 * @return output to commandline 
	 */
	@SuppressWarnings("deprecation")
	private static String getOutput(String[] comm){
		String out="";
		try {
			String ls_str;
			Process ls_proc = Runtime.getRuntime().exec(comm);
			DataInputStream ls_in = new DataInputStream(
					ls_proc.getInputStream());

			try {
				while ((ls_str = ls_in.readLine()) != null) {
					out = out+ls_str+"\n";
				}
			} catch (IOException e) {
				e.printStackTrace();
				return out;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return out;
		}
		return out;
	}

	public static String printPWD(){
		String out = getOutput(new String[]{"pwd"});
		if(out.length()>2)
			out = out.substring(0, out.length()-1);
		return out;
	}

	public static String[] toStr(List<String> com){
		String[] out = new String[com.size()];

		for(int i=0; i<com.size(); i++)
			out[i] = com.get(i);
		return out;
	}
}
