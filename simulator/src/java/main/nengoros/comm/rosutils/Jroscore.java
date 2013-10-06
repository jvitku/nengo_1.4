package nengoros.comm.rosutils;

import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import java.net.URI;
import java.net.URISyntaxException;

import nengoros.comm.nodeFactory.NodeFactory;

import org.ros.RosCore;


/**
 * Is able to launch java implementation of roscore. 
 * The thread should be periodically checking the variable NengoGraphics.utilsShouldStop
 * if it is true, the core shuts down itself.
 * 
 * @author Jaroslav Vitku
 *
 */
public class Jroscore {
	
	private static final String s = "http://localhost:11311/";
	private static RosCore rosCore;
	private static URI u;
	private static final String me = "[jroscore] ";
	private static boolean running=false;
	
	protected static final int autokillerSleepTime = 300;
	private static boolean startAutoKiller = false;

	
	/**
	 * Tries to read the URI address of the roscore from command line
	 * Launches the master server (roscore) and waits 
	 * @param args
	 */
	public static void main(String[] args) {
		String uri;
		
		if(args.length > 1){
			pu();
			return;
		}else if(args.length < 1){
			uri=s;
			warn();
			pu();
		}else{
			uri=args[0];
		}
		startAutoKiller = false;	// if it runs externally, do not autoshutdown it
		start(uri);
	}
	
	public static void start(String uri){
		u=getUri(uri);
		
		/*
		if(u!= null)
			System.out.println(me+"Launching master server now, uri: "+u);
		else
			System.out.print(me+"Launching master server now, uri will be generated");
		*/
		u = launchCore(u);
		if(u!=null){
			System.out.println(me+"Server successfully launched on address: "+u);

			if(startAutoKiller){
				AutoKiller a = new AutoKiller(autokillerSleepTime);
				a.start();
			}
		}
	}
	
	public static void start(boolean startAutoKiller){
		Jroscore.startAutoKiller = startAutoKiller;
		start(s);
	}
	
	public static void start(){
		start(s);
	}
	
	public static void stop(){ 
		System.out.println(me+"OK, shutting down the server");
		tearDown();
	}
	
	public static boolean running(){ return running; }
	
	private static void pu(){
		System.out.println(me+"=============== Usage ===============");
		System.out.println("run this Class with one argument, which specifies "+
				"the URI address of the roscore, e.g.: "+s);
	}
	
	private static void warn(){
		System.err.println(me+"warning: could not parse URI, " +
		"will use default one, other nodes may not see the master\n" +
		"Correct format of URI is e.g.: "+s);
	}

	/**
	 * try to parse URI from the command line
	 * @param st
	 * @return valid uri or null in case of fail
	 */
	private static URI getUri(String st){
		try {
			URI ur= new URI(st);
		return ur;
		} catch (URISyntaxException e) {
			//e.printStackTrace();
			warn();
			return null;
		}
	}
  
	/**
	 * Launch the roscore on the specified uri
	 * @param ur new address of the roscore, if null, some will be generated
	 * @return uri of the new roscore
	 */
	private static URI launchCore(URI ur){
			
		// initialize new public roscore with parsed address
		if(ur==null)
			rosCore = RosCore.newPublic();	
		else
			rosCore = RosCore.newPublic(ur.getHost(),ur.getPort());
		
		rosCore.start();
		// wait for start..
		try {
			assertTrue(rosCore.awaitStart(1, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		running=true;
		return rosCore.getUri();
	}
	
	/**
	 * should be used for shutting down the server.. but is not
	 */
	private static void tearDown() {
		NodeFactory.nme.shutdown();		// this is TODO: it seems that it does not shutdown..
		running = false;
		rosCore.shutdown();
	}
	
	private static class AutoKiller implements Runnable{

		private final int sleep;
		private Thread thread;
		private final String me = "jroscore-autokiller";
		
		public AutoKiller(int sleepTime){
			sleep = sleepTime;
		}
		
		public void start(){
			thread = new Thread(this);
			thread.start();
		}
		
		@Override
		public void run() {
			while(true){
				if(!Jroscore.running()){
					System.out.println("["+me+"] Jroscore not running, exiting..");
					return;
				}
				
				if(RosUtils.nodesShouldStop){
					System.out.println("["+me+"] global variable says I should stop my " +
							"process, OK.");
					Jroscore.stop();
					return;
				}
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					System.err.println("["+me+"] error, could not sleep, retrying..");
				}
			}	
		}
	}
	

}
