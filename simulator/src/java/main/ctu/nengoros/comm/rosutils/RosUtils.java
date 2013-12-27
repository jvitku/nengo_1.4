package ctu.nengoros.comm.rosutils;

import java.util.ArrayList;

import org.ros.node.NodeMain;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.nodeFactory.javanode.JavaNodeContainer;
import ctu.nengoros.comm.nodeFactory.nativenode.impl.RunnableNode;
import ctu.nengoros.comm.rosutils.Jroscore;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.ProcessLauncher;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.comm.rosutils.utilNode.time.RosTimeUtil;
import ctu.nengoros.comm.rosutils.utilNode.time.RosTimeUtilFactory;
import ctu.nengoros.rosparam.node.RosparamNode;

/**
 * Here are utilities for simple launching and stopping (some) roscore 
 * and rqt utility, if available. 
 * 
 * -UtilsAutorun says whether to start anything from this
 * -these situations can occur:
 * 		-rqt and c++ roscore found, so they will be used
 * 		-rqt and c++ roscore found, but jroscore is preffered: running jroscore+rqt
 * 
 * 
 * 		-ROS installation not foud, running just jroscore
 * 
 * @see <a href="http://www.ros.org/wiki/">ROS wiki</a>
 * @see <a href="http://docs.rosjava.googlecode.com/hg/rosjava_core/html/index.html">
 * ROSjava documentation</a>
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
	private static boolean rqtFound = false;

	// whether to start available utilities (roscore and rxgraph now..)
	private static boolean utilsAutorun = true;

	private static RunnableNode rqtNode, roscoreNode;

	// list of utility nodes which are shutdown after the app exits
	private static ArrayList<JavaNodeContainer> utilNodes = new ArrayList<JavaNodeContainer>();
	// choose the time handling: 0=TimeMaster, 1=IgnoreTime, 2=TimeSlave
	private static int previousTimeUnit = 0;
	private static int selectedTimeUnit = 0;
	// this handles time synchronization between ROS and Nengo
	private static RosTimeUtil timeUnit;

	// enables Nengo to set parameters of ROS network
	// these parameters can modify how the nodes are launched, e.g. use_sim_time:=true
	private static RosparamNode paramHandler;

	/**
	 * Start core and other utilities automatically?
	 *  
	 * @param autorun
	 */
	public static void setAutorun(boolean autorun){
		utilsAutorun = autorun;
	}

	public static boolean autorun(){
		return utilsAutorun;
	}

	/**
	 * By default, if any ROS node is launched, this is called and tries
	 * to launch ROS Core, RQT, TimeMaster and ParamServer.
	 *
	 * THese should be launched only once and terminated when the Nengo app exits.
	 */
	public static void utilsShallStart(){
		if(!utilsAutorun)
			return;
		checkInit();

		if(!coreRunning())
			coreStart();

		Mess.wait(1);	// just to give roscore time to start..

		if(!rqtRunning() && rqtFound)
			rqtStart();

		// launch the parameter handler?
		if(paramHandler == null){
			paramHandler = RosTimeUtilFactory.startParamHandler(utilNodes);
		}else if(previousTimeUnit != selectedTimeUnit){
			// if switched to the ignoreTime during application run, we have to delete param.: use_sim_time
			if(selectedTimeUnit ==1){
				paramHandler.delete(RosTimeUtilFactory.time);
			}
		}

		// if timeUnit is running and user selected some other one, restart
		if(timeUnit!=null){
			if(previousTimeUnit != selectedTimeUnit){
				RosTimeUtilFactory.nme.shutdownNodeMain((NodeMain)timeUnit);
				timeUnit = getTimeUnit();
				previousTimeUnit = selectedTimeUnit;
			}else{
				// in case of reuse of simulationUnit, indicate that simulation is stopped
				timeUnit.simulationStopped();
			}
		}else{
			timeUnit = getTimeUnit();
			previousTimeUnit = selectedTimeUnit;
		}

	}

	/**
	 * Select what to do with time: publish, ignore, receive.
	 * 
	 * @return unit which does this @see: see: ca.nengo.util.impl.NodeThreadPool.step()
	 */
	private static RosTimeUtil getTimeUnit(){

		RosTimeUtil tu;

		switch(selectedTimeUnit){
		case 1:
			tu = RosTimeUtilFactory.getTimeIgnoringUtil();
			// do not mention simulation time here, nodes will use WallTime
			break;
		case 2:
			tu = RosTimeUtilFactory.startDefaultTimeSlave(utilNodes);
			// TODO
			System.err.println(me+"Note that use of timeSlave mode is experimental so far!");
			// launch all ROS nodes to listen the time by default, note that you have to 
			// launch time provider with this set to false!
			paramHandler.set(RosTimeUtilFactory.time, true);
			break;
		default:
			tu = RosTimeUtilFactory.startDefaultTimeMaster(utilNodes);
			// set all other launched ROS nodes to listen clock provided by Nengo 
			paramHandler.set(RosTimeUtilFactory.time, true);
		}
		return tu;
	}

	/**
	 * Stop every ROSjava node which is launhced (timeHandler and ParamHandler).
	 */
	private static void stopAllUtilNodes(){
		JavaNodeContainer tmp = null;

		while(!utilNodes.isEmpty()){
			tmp = utilNodes.remove(0);
			System.out.println(me+"stopping UtilNode called: "+tmp.getName());
			tmp.stop();
		}

		System.out.println(me+"Shutting down NodeMainExecutor for UtilNodes");
		RosTimeUtilFactory.stopNME();
	}

	/**
	 * sets the NengoROS as a time master (selected by default).
	 * Use these in python scripts.
	 */
	public static void setTimeMaster(){ selectedTimeUnit=0; }
	public static void setTimeSlave(){  selectedTimeUnit=2; } 
	public static void setTimeIgnore(){ selectedTimeUnit=1; }

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

	/**
	 * When the Nengo application stops, do this:
	 */
	public synchronized static void utilsShallStop(){

		// TODO this should not be necessary..?
		RosUtils.nodesShouldStop = true;	// notify everything about stopping..

		if(rqtNode != null && rqtNode.isRunning()){
			System.out.println(me+"Stopping the rqt now");
			rqtNode.stop();
		}

		System.out.println(me+"Stopping all "+getNumOfGroups()+" group(s) of ROS nodes");
		RosUtils.stopAllNodes();			// manually stop all known node containers (all)	

		Mess.wait(timeBeforeCoreShutdown);	// give'em time..

		NodeFactory.nme.shutdown();			// TODO: do this automatically, and this does not work?

		stopAllUtilNodes();					// stop the parameter server and time handler

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
		if(rqtFound)
			rqtStart();
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

	private static void rqtStart(){
		checkInit();

		if(rqtRunning())
			return;

		if(!rqtFound){
			System.err.println(me+"rqt was not found on this system..");
			return;
		}
		rqtNode = new RunnableNode(new String[]{"rqt"}, "rqt");
		rqtNode.start();
		//rxgraphNode.startAutoKiller();
	}

	public static boolean rqtRunning(){
		if(rqtNode==null)
			return false;
		return rqtNode.isRunning();
	}

	public static boolean coreRunning(){
		if(roscoreNode != null && roscoreNode.isRunning())
			return true;
		return (Jroscore.running());
	}

	/**
	 * try to locate C++ roscore and rqt
	 */
	private static void checkInit(){
		if(inited)
			return;

		roscoreFound = ProcessLauncher.appExists("roscore");
		rqtFound = ProcessLauncher.appExists("rqt");

		inited = true;
	}

	public static RosTimeUtil getTimeUtil(){
		if(timeUnit==null){
			System.err.println(me+"error! TimeUtil not initialized yet!");
		}
		return timeUnit; 
	}
}
