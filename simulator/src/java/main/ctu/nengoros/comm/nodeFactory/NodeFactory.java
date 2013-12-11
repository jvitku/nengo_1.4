package ctu.nengoros.comm.nodeFactory;

import java.util.ArrayList;
import java.util.List;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeMainExecutor;
import ctu.nengoros.comm.nodeFactory.NodeGroup.MyNodeConfig;
import ctu.nengoros.comm.nodeFactory.javanode.JavaNodeContainer;
import ctu.nengoros.comm.nodeFactory.javanode.JavaNodeLauncher;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.nodeFactory.nativenode.NativeNodeContainer;
import ctu.nengoros.comm.nodeFactory.nativenode.impl.UNodeContainer;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.ProcessLauncher;
import ctu.nengoros.comm.rosutils.RosUtils;

/**
 * Give me name and namespace, I will create your node and prepare it for launching. 
 * 
 * @author Jaroslav Vitku
 * 
 */
public class NodeFactory{
	// whether to print what I am launching
	public static boolean printCommands = true;	
	
	public static final boolean printOut = false;
	
	protected static final String me="[NodeFactory] ";

	// thing which launches JavaNodes
	public static final NodeMainExecutor nme = DefaultNodeMainExecutor.newDefault();

	// thing that provides unique names for Nodes
	public static final NameProvider np = new NameProvider();

	private static final List<String> command = new ArrayList<String>(7);
	// http://www.ros.org/wiki/Nodes#Remapping_Arguments.A.22Pushing_Down.22
	private static final String NS 	= "__ns:=";
	private static final String NAME = "__name:="; 

	
	/**
	 * Create group of external nodes, modify their namespace or names so 
	 * that each node in the ROS network has unique (complete) name
	 * @param g group of node configurations, where are nodes also stored
	 */
	public static void initGroupOfNodes(NodeGroup g){
		
		// start ROS utilities (roscore, rxgrpah...)
		RosUtils.utilsShallStart();
		
		g.nodes = new ExternalNodeContainer[g.nodeConfigs.size()];
		MyNodeConfig mnc;
		
		if(g.independent){
			// if group can be pushed into own namespace, do it
			g.namespace = np.findNamespace(g.getNames(), g.groupName);
			g.setUniqueNames();		// compose unique names (namespace+nodeName)
		}else{
			// if cannot be pushed (this breaks communication with other ROS nodes)
			// modify names of all nodes so that each name is unique in the current net
			g.setNames(np.modifyNames(g.namespace, g.getNames(), g.groupName));
			g.setUniqueNames();
		}
		// start all nodes in the group
		for(int i=0; i<g.nodeConfigs.size(); i++){
			mnc = g.nodeConfigs.get(i);
			if(mnc.ismodem)
				g.nodes[i] = createModem(mnc, g);
			else if(mnc.isNative)
				g.nodes[i] = createUnixNode(mnc, g);
			else
				g.nodes[i] = createJavaNode(mnc, g);
		}
		RosUtils.addGroup(g);
	}
	
	private static ModemContainer createModem(MyNodeConfig mnc, NodeGroup g){
		command.clear();
		// java nodes have only one part of command: complete class name
		// with packages separated by dots.
		command.add(mnc.command[0]);
		// if name-space is set, add it to the command
		if(g.namespace !=null && g.namespace.length() > 0){
			command.add(NS+g.namespace);	
		}
		command.add(NAME+mnc.name);
		inform(command, "modem");
		// use the command to launch java node
		ModemContainer j = JavaNodeLauncher.launchModem(command, mnc.uniqueName, nme, g);
		// TODO support this:
		//j.useLogging(mnc.printToConsole);
		return (ModemContainer)j;
	}
	
	private static ExternalNodeContainer createJavaNode(MyNodeConfig mnc, NodeGroup g){
		command.clear();
		// java nodes have only one part of command: complete class name
		// with packages separated by dots.
		command.add(mnc.command[0]);
		// if name-space is set, add it to the command
		if(g.namespace !=null && g.namespace.length() > 0){
			command.add(NS+g.namespace);	
		}
		command.add(NAME+mnc.name);	
		inform(command, "java");
		// use the command to launch java node
		JavaNodeContainer j = JavaNodeLauncher.launchNode(command, mnc.uniqueName, nme);
		// TODO support this:
		//j.useLogging(mnc.printToConsole);
		return (ExternalNodeContainer)j;
	}
		
	private static ExternalNodeContainer createUnixNode(MyNodeConfig mnc, NodeGroup g){
		command.clear();
		// add all parts of original launch command
		for(int i=0; i<mnc.command.length; i++){
			command.add(mnc.command[i]);
		}
		// if name-space is set, add it to the command
		if(g.namespace !=null && g.namespace.length() > 0){
			command.add(NS+g.namespace);	
		}
		command.add(NAME+mnc.name);	
		
		ProcessLauncher.checkOS(command);
		inform(command, "native");
		UNodeContainer n = new UNodeContainer(command, mnc.uniqueName);
		n.useLogging(mnc.printToConsole);
		return (ExternalNodeContainer)n;
	}

	protected static void inform(List<String> command, String type){
		if(printCommands)
			System.out.println(me+"launching "+type+" node with the following command: "+Mess.toAr(command));
	}
	
	public static int numOfRunningNodes() { return np.numOfRunningNodes(); }

	public static String[] namesOfRunningNodes() { return np.namesOfRunningNodes(); }

	/**
	 * Tells the NameProvider that node is being closed
	 * @param j
	 */
	public static void killNode(JavaNodeContainer j){
		np.shutDown(j.getName());			// remove from list of names
	}

	/**
	 * This just tells the NameProvider that node is no longer running
	 * @param n node which is about to be closed
	 */
	public static void killNode(NativeNodeContainer n){
		np.shutDown(n.getName());
	}
	
	/**
	 * Shut down the modem, tell the NameProvider to remove the modem
	 * name from the list
	 * @param m modem container itself
	 */
	public static void killModem(ModemContainer m){
		np.shutDown(m.getName());
	}
}
