package ctu.nengoros.comm.nodeFactory.nativenode.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import ctu.nengoros.comm.nodeFactory.nativenode.NativeNodeContainer;
import ctu.nengoros.comm.rosutils.RosUtils;

/**
 * Class that is able to run external process, read both streams 
 * and automatically kill process when a corresponding static 
 * variable indicates it.
 * 
 * Note: autokiller refers to static variable in the class NengoGrahics 
 * 
 * @author Jaroslav Vitku
 *
 */
public class RunnableNode implements NativeNodeContainer{

	//public static volatile boolean shouldStop = false;
	
	protected final String me = "RunnableNode: ";
	protected final int autokillerSleepTime = 500;
	protected ProcessBuilder prb;
	protected Process p;
	protected final String[] comm;
	private StreamGobbler o, e;
	protected final String name;
	private boolean merg;		//streams are merged?
	private boolean printOut=false;
	
	protected boolean running = false;
	
	/**
	 * Prepare external runnable node to start.
	 * @param command command to launch the node
	 * @param nodeName name of the node (just for identification)
	 */
	public RunnableNode(List<String> command, String nodeName){
		this(command.toArray(new String[command.size()]), nodeName);
	}

	public RunnableNode(String[] command, String nodeName){
		// do not merge streams by default
		this(command, nodeName, false);
	}

	public RunnableNode(List<String> command, String nodeName, boolean merge){
		this(command.toArray(new String[command.size()]), nodeName, merge);
	}
	
	public RunnableNode(String[] command, String nodeName, boolean merge){
		merg = merge;
		comm = command;
		prb = new ProcessBuilder(command);
		if(merg)
			prb.redirectErrorStream(true);
		name = nodeName; 
	}
	
	/**
	 * Try to start external process, start stream consumers.
	 * @throws IOException
	 */
	@Override
	public void start() {
		try {
			p = prb.start();

			if(merg){
				o = new StreamGobbler(true, false, p, name);
			}else{
				o = new StreamGobbler(false, false, p, name);
				e = new StreamGobbler(false, true, p, name);
				e.setPublishing(printOut);
				e.start ();
			}
			o.setPublishing(printOut);
			o.start ();
			running = true;
		} catch (IOException e1) {
			System.err.println("["+me+"] "+name+" Could not start a process" +
					" using given command: '"+toStr(comm)+"'");
			e1.printStackTrace();
		}
	}

	/**
	 * Start the autokiller: a thread that checks static variable
	 * which notifies that processes should exit. If process exits, 
	 * the autokiller thread exits too. 
	 */
	@Override
	public void startAutoKiller(){
		if(!running){
			System.err.println(me+" Error: will not start autokiller before starting the process!");
			return;
		}
		ProcessAutoKiller pak = new ProcessAutoKiller(
				autokillerSleepTime, name+"-autokiller", p);
		pak.start();
	}
	
	/**
	 * This stops the process, so the StreamGlobbers will exit themselves too.
	 */
	@Override
	public void stop(){
		System.out.println(name+" OK, closing my process now..");
		p.destroy();
		running=false;
	}

	@Override
	public String getName() { return name; }

	@Override
	public boolean isRunning() { return running; }

	@Override
	public String[] getLauchCommand() { return comm; }

	@Override
	public void useLogging(boolean use) {
		printOut=use;
		if(o==null)
			return;
		if(!merg)
			e.setPublishing(use);
		o.setPublishing(use);
	}
	
	private String toStr(String[] command){
		String out = command[0];
		for(int i=1; i<command.length; i++)
			out = out +" "+ command[i];
		return out;
	}
	
	/**
	 * This thread periodically checks whether the external process should be 
	 * destroyed. This is specified by some static external variable.
	 * 
	 * @author Jaroslav Vitku
	 *
	 */
	private class ProcessAutoKiller implements Runnable{

		private final String me;
		private final int sleep;
		private final Process p;
		private Thread thread;
		
		public ProcessAutoKiller(int sleepTime, String name, Process process){
			me=name;
			sleep = sleepTime;
			p = process;
		}
		
		public void start(){
			thread = new Thread(this);
			thread.start();
		}
		
		@Override
		public void run() {
			while(true){
				try{
					// check whether the process is still running, if not, exit
					p.exitValue();
					System.out.println("["+me+"] process exited, exiting too.");
					return;
				}catch(IllegalThreadStateException c){}
				
			//	System.out.println(me+" autokiller, checking now: ");
				//System.out.println(me+" nng: "+RosUtils.utilsShouldStop);
				if(RosUtils.nodesShouldStop){
					System.out.println("["+me+"] global variable says I should stop my " +
							"process, OK.");
					p.destroy();
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
	
	/**
	 * Thing that consumes (output, error or both) stream from an external process. 
	 * If streams are not consumer, the stream buffer fills and the process hangs.
	 * 
	 *  Based on this: 
	 *  http://www.javaworld.com/jw-12-2000/jw-1229-traps.html?page=4
	 * 
	 * @author Jaroslav Vitku
	 */
	private class StreamGobbler implements Runnable {
		private final String me;
		private final boolean errorStream;	// should consume output or errorStream?
		private final boolean mergeStreams;	// merge output with error stream?
		private boolean print = true;		// publish received data to console?
		private InputStream is;
		private Thread thread;				// me

		public StreamGobbler (boolean streamsMerged, boolean errorStream, Process p,
				String nodeName) {
			this.errorStream = errorStream;
			this.mergeStreams = streamsMerged;
			me = nodeName;

			if(streamsMerged)
				is = p.getInputStream();
			else if(errorStream)
				is = p.getErrorStream();
			else
				is = p.getInputStream();
		}

		/**
		 * should we publish received data to console?
		 * @param publish
		 */
		public void setPublishing(boolean publish){
			print = publish;
		}
		
		public void start() {
			thread = new Thread (this);
			thread.start ();
		}

		/**
		 * Starts the thread which consumes the stream, that is: 
		 * waits for each new line of data, if received, publishes it.
		 * Note that if process is closed (destroy() is called or process ends)
		 * this thread also ends and exits. So typically these threads are only started.
		 */
		@Override
		public void run () {
			try {
				InputStreamReader isr = new InputStreamReader (is);
				BufferedReader br = new BufferedReader (isr);

				// read each line of stream, and publish it
				while (true) {
					
					String s = br.readLine ();
					if (s == null)
						break;
					if(print){
						if(errorStream)
							System.err.println("["+me+"-STDERR]: "+s);
						else
							System.out.println("["+me+"] "+s);
					}
				}
				if(print){
					if(mergeStreams)
						System.out.println("["+me+"-globber] OK, closing stdout and stderr.");
					else if(errorStream)
						System.out.println("["+me+"-globber] OK, closing stderr.");
					else
						System.out.println("["+me+"-globber] OK, closing stdout.");
				}
				is.close ();
			} catch (Exception ex) {
				// this can be thrown if the stream is closed somehow incorrectly
				System.out.println ("["+me+"-globber] Closing stream.");
				//ex.printStackTrace ();
			}
		}
	}

	/**
	 * Called on each Nengo reset (open sim. window etc..)
	 */
	@Override
	public void reset() {
		//System.out.println(me+" unable to reset, could start/stop me though...");
	}
}


