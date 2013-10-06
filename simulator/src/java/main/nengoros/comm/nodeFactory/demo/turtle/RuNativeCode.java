package nengoros.comm.nodeFactory.demo.turtle;

import java.io.IOException;

import nengoros.comm.rosutils.Mess;

import org.jdesktop.swingx.util.OS;



/**
 * This is old demo, shows how to launch external process..
 * 
 * Here is the demo how we are able to launch arbitrary native application on Unix platform. 
 * 
 * Note that this demo uses installation of ROS fuerte and compiled source code of turtlesim 
 * (which is also (a copied) part of ROS installation). So this demo will work correctly
 * only on Unix system with ROS installed.
 * 
 * @author Jaroslav Vitku
 *
 */
public class RuNativeCode {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String[] turtle = new String[]{"./../testnodes/turtlesim/turtlesim_node"};
		String[] nativeCore = new String[]{"roscore"};
		String[] rxgrpah = new String[]{"rxgraph"};
		
	
		Process a = launchProcess(nativeCore);
		Process b = launchProcess(rxgrpah);
		Process c = launchProcess(turtle);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c.destroy();
		b.destroy();
		a.destroy();
	}

	
	public static Process launchProcess(String[] comm) {

		if(OS.isLinux()){
			System.out.println("LaunchProcess: OK, this is Linux, hope you have ROS installed here..");
		}else if(OS.isMacOSX()){
			System.out.println("LaunchPRocess: Hmm, this is OS X, you probably do not have ROS installed, this could be problem..");
		}else{
			System.err.println("LaunchProcess: Windows support of native code: this is still TODO..");
		}
		try {
			Process ls_proc = Runtime.getRuntime().exec(comm);
			System.out.println("LaunchProcess: "+Mess.toAr(comm)+" ....started OK");
			return ls_proc;
			
		} catch (IOException e1) {
			System.err.println(e1);
			System.exit(1);
		}
		System.err.println("LaunchProcess: "+Mess.toAr(comm)+" ....could not be started!");
		return null;
	}
}
