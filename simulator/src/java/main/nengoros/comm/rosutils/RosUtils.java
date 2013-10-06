package nengoros.comm.rosutils;

import java.util.ArrayList;

import nengoros.comm.nodeFactory.NodeFactory;
import nengoros.comm.nodeFactory.NodeGroup;
import nengoros.comm.nodeFactory.nativenode.impl.RunnableNode;

/**
 * Here are utilities for simple launching and stopping (some) roscore 
 * and rxgraph, if available. 
 * 
 * -UtilsAutorun says whether to start anything from this
 * -these situations can occur:
 * 		-rxgraph and c++ roscore found, so they will be used
 * 		-rxgprah and c++ roscore found, but jroscore is preffered: running jroscore+rxgraph
 * 		-ROS installation not foud, running just jroscore
 * 
 * @author Jaroslav Vitku
 *
 */
public class RosUtils {

	private static final ArrayList<NodeGroup> groupList = new ArrayList<NodeGroup>();
	
	private static int timeBeforeCoreShutdown = 1;	// in seconds
	
	public static boolean nodesShouldStop = false;
	
	public static final String me = "[RosUtils]: ";

	// preferred roscore, if this is false, will launch C++ roscore if found
	private static boolean preferJroscore = true;

	private static boolean inited = false;
	private static boolean roscoreFound = false;
	private static boolean rxgraphFound = false;

	// whether to start available utilities (roscore and rxgraph now..)
	private static boolean utilsAutorun = true;

	private static RunnableNode rxgraphNode, roscoreNode;

	public RosUtils(){}
	
	public static void setAutorun(boolean autorun){
		utilsAutorun = autorun;
	}

	public static boolean autorun(){
		return utilsAutorun;
	}

	/**
	 * If autorun is true, as much utils as available
	 * will be launched here. 
	 */
	public static void utilsShallStart(){
		if(!utilsAutorun)
			return;
		checkInit();

		if(!coreRunning())
			coreStart();

		Mess.wait(1);	// just to give roscore time to start..

		if(!rxgraphRunning() && rxgraphFound)
			rxgraphStart();
	}

	public synchronized static void stopAllNodes(){
		// this is necessary, NodeGroup.stopGroup() removes itself from the list
		NodeGroup[] tmp = groupList.toArray(new NodeGroup[groupList.size()]);
		
		for(int i=0; i<tmp.length; i++)
			tmp[i].stopGroup();
		/*
		System.out.println(me+" number of groups is: "+groupList.size());
		for(int i=0; i<groupList.size(); i++){
			System.out.println(me+"stopping this "+i+" group: " +groupList.get(i).groupName);
			if(i>0)
			groupList.get(i).stopGroup();
		}*/
	}
	
	/**
	 * RosUtils should remember all currently running groups
	 * in order to be able to stop them if necessary.
	 * @param g
	 */
	public synchronized static void addGroup(NodeGroup g){
		groupList.add(g);
	}
	
	public synchronized static void removeGroup(NodeGroup g){
		if(!groupList.contains(g))
			System.err.println(me+" removeGroup(): list does not contain: "+g.groupName);
		else
			groupList.remove(g);
	}
	
	public synchronized static int getNumOfGroups(){ return groupList.size(); }
	
	
	public synchronized static void utilsShallStop(){
		// TODO this should not be necessary..?
		RosUtils.nodesShouldStop = true;	// notify everything about stopping..

		if(rxgraphNode != null && rxgraphNode.isRunning()){
			System.out.println(me+"Stopping the rxgraph now");
			rxgraphNode.stop();
		}
		
		System.out.println(me+"Stopping all "+getNumOfGroups()+" group(s) of ROS nodes");
		RosUtils.stopAllNodes();			// manually stop all known node containers (all)	
		
		Mess.wait(timeBeforeCoreShutdown);						// give'em time..
		
		NodeFactory.nme.shutdown();	// TODO: do this automatically, and this does not work?

		if(coreRunning()){
			System.out.println(me+"Stopping the core now");
			coreStop();
		}
	}
	
	/**
	 * if C++ roscore found, which one prefer?
	 * @param prefer
	 */
	public static void prefferJroscore(boolean prefer){
		if(coreRunning())
			System.err.println(me+" core is already running, " +
					"setting preffered core will have no effect until core restart");
		preferJroscore = prefer;
	}

	private static void coreStart(){
		if(coreRunning())
			return;
		checkInit();
		// start C++ roscore here?
		if(roscoreFound && !preferJroscore){
			roscoreNode = new RunnableNode(new String[]{"roscore"},"roscore");
			roscoreNode.start();
//			roscoreNode.startAutoKiller();// not necessary
		}
		// or start java roscore here?
		else{
			Jroscore.start();
		}
		if(rxgraphFound)
			rxgraphStart();
	}

	private static void coreStop(){
		if(!coreRunning())
			return;
		if(roscoreNode!= null && roscoreNode.isRunning()){
			roscoreNode.stop();
			return;
		}else if(Jroscore.running()){
			Jroscore.stop();
			return;
		}
	}

	private static void rxgraphStart(){
		checkInit();

		if(rxgraphRunning())
			return;

		if(!rxgraphFound){
			System.err.println(me+"rxgraph was not found on this system..");
			return;
		}
		rxgraphNode = new RunnableNode(new String[]{"rxgraph"}, "rxgraph");
		rxgraphNode.start();
		//rxgraphNode.startAutoKiller();
	}

	public static boolean rxgraphRunning(){
		if(rxgraphNode==null)
			return false;
		return rxgraphNode.isRunning();
	}

	public static boolean coreRunning(){
		if(roscoreNode != null && roscoreNode.isRunning())
			return true;
		return (Jroscore.running());
	}

	/**
	 * try to locate C++ roscore and rxgraph
	 */
	private static void checkInit(){
		if(inited)
			return;

		roscoreFound = ProcessLauncher.appExists("roscore");
		rxgraphFound = ProcessLauncher.appExists("rxgraph");
		
		inited = true;
	}
}
