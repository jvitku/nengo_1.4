package ctu.nengoros.comm.nodeFactory;

import java.util.ArrayList;

import ctu.nengoros.comm.nodeFactory.externalNode.ExternalNodeContainer;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.network.node.synchedStart.StartupManager;
import ctu.nengoros.network.node.synchedStart.impl.BasicStartupManager;
import ctu.nengoros.network.node.synchedStart.impl.StartedObject;

/**
 * Group of nodes typically corresponds to one smart neuron. 
 * Communication in the group can be shielded by own, automatically 
 * generated, namespace, so that all complete node names are unique.
 * This is handled by NameProvider and it should block the communication
 * over the ROS network across the ROS nodes.
 * If you want to specify absolute path, set the variable independent
 * to false. The NameProvider then will change the names of nodes
 * in a way that they will be unique in the ROS network, namespace 
 * if the nodes will remain unchanged. 
 *  
 * Each group has min. 0, max. 1 modem, encoders and decoders (terminations
 * and origins on Nengo side) can be added to each modem.  
 *  
 * @author Jaroslav Vitku
 *
 */
public class NodeGroup implements StartedObject{
	
	public static final String NATIVE = "native";
	public static final String JAVA = "java";
	public static final String MODEM = "modem";
	public static final String DEF_MODEM = "defaultModem";
	//public static final String defaultModem = "ctu.nengoros.comm.nodeFactory.modem.impl.DefaultModem";
	public static final String defaultModem = "ctu.nengoros.comm.nodeFactory.modem.impl.RosResettingModem";
	
	// Every group of nodes can contain up to one modem 
	// The modem can be empty, but should belong to some Neural Module, 
	// in order to add possibility of deleting this group from GUI
	// If user tries to start group without modem, the default one is added. 
	private ModemContainer myModem;
	private MyNodeConfig modemConfig;
	private boolean modemIsSet = false;
	
	// Array of prepared external nodes created by the NodeFactory
	public ExternalNodeContainer[] nodes;

	// List of node configurations
	public final ArrayList<MyNodeConfig> nodeConfigs;

	// whether the entire group is independent
	// if so, all nodes in this group can be pushed into namespace
	public final boolean independent;

	// groupName is used in namespace or names of nodes which belong there
	public final String groupName;

	// group can have its own namespace, if is independent
	// if is not independent, then the namespace remains empty
	public String namespace="";

	private boolean groupRunning = false;
	
	private StartupManager startup = new BasicStartupManager(this);
	private boolean isStarted = false;

	/**
	 * Setup of non-independent group with predefned namespace.
	 * If namespace is not important, use another constructor. 
	 * 
	 * @param groupName name of a group
	 * @param namespace namespace of the group (provider changes only node names)
	 */
	public NodeGroup(String groupName, String namespace){
		this.namespace = namespace;
		this.groupName = groupName;
		this.independent = false;
		this.nodeConfigs = new ArrayList<MyNodeConfig>(3);
	}

	/**
	 * Group of nodes can be either independent, non-nedependent
	 * (namespace is generated automatically) or with predefined
	 * namespace, therefore not independent.
	 *  
	 * @param groupName name of the group
	 * @param independent whether group is independent
	 */
	public NodeGroup(String groupName, boolean independent){
		this.groupName = groupName;
		this.independent = independent;
		this.nodeConfigs = new ArrayList<MyNodeConfig>(3);
	}

	public String[] getNames(){
		String[] out = new String[nodeConfigs.size()]; 
		for(int i=0; i<nodeConfigs.size(); i++)
			out[i] = nodeConfigs.get(i).name;
		return out;
	}

	public void setNames(String[] newNames){
		if(newNames.length != nodeConfigs.size()){
			System.err.println("NodeGroup: setNames: list of new names has wrong size");
			return;
		}
		for(int i=0; i<newNames.length; i++){
			nodeConfigs.get(i).name = newNames[i];
		}
	}

	/**
	 * store the uniquenames, that is: if namespace not set, 
	 * just copy the old names (these are most likely modified
	 * by the nameprovider), if namespace is set, use the complete name
	 *  
	 * unique names are composed of: group__i + / + name
	 */
	public void setUniqueNames(){
		if(namespace ==null || namespace.length() == 0){
			for(int i=0; i<nodeConfigs.size(); i++)
				nodeConfigs.get(i).uniqueName = nodeConfigs.get(i).name;
		}else{
			for(int i=0; i<nodeConfigs.size(); i++)
				nodeConfigs.get(i).uniqueName = namespace+"/"+nodeConfigs.get(i).name;
		}
	}

	public boolean isRunning(){ return this.groupRunning; }
	
	public void stopGroup(){
		// mymodem should know where from was called his method stop()
		this.groupRunning = false;	
		for(int i=0; i<nodes.length; i++){
			if(nodes[i].isRunning())
				nodes[i].stop();
		}
		RosUtils.removeGroup(this);
	}

	public void startGroup(){
		if(!modemIsSet){
			this.addNode(defaultModem, DEF_MODEM, MODEM);
		}
		if(nodes == null){
			// OK, user probably forgot to init this group, so do it now..
			NodeFactory.initGroupOfNodes(this);
		}
		for(int i=0; i<nodes.length; i++){
			nodes[i].start();
			if(nodeConfigs.get(i).ismodem)
				myModem = (ModemContainer)nodes[i];
		}
		this.groupRunning = true;
		// indicate that the group of nodes run OK 
		// TODO: add checking all Publishers/subscribers of modem connected here
		// actually: add each pub/sub as a child and indicate if is connected
		this.isStarted = true;
	}
	
	/*
	 * // resetting of nodes is now done by means of modem
	@Override
	public void reset(boolean randomize){
		for(int i=0; i<nodes.length; i++){
			nodes[i].reset(randomize);
		}
	}*/

	public void addNode(String command, String name, String what){
		if(what.equalsIgnoreCase(NATIVE)){
			this.addNode(command, name, true, false);
		}else if(what.equalsIgnoreCase(JAVA)){
			this.addNode(command, name, false, false);
		}else if(what.equalsIgnoreCase(MODEM)){
			this.addNode(command, name, false, true);
		}else{
			System.err.println(groupName+" : addNC() node of unknown type, "+
					"supported possibilities are so far: 'native','java' and 'modem'");
		}
	}
	
	public void addNode(String[] command, String name, String what){
		if(what.equalsIgnoreCase(NATIVE)){
			this.addNode(command, name, true, false);
		}else if(what.equalsIgnoreCase(JAVA)){
			this.addNode(command, name, false, false);
		}else if(what.equalsIgnoreCase(MODEM)){
			this.addNode(command, name, false, true);
		}else{
			System.err.println(groupName+" : addNC() node of unknown type, "+
					"supported possibilities are so far: 'native','java' and 'modem'");
		}
	}
		
	public boolean getModemIsSet(){ return modemIsSet; }
	
	public void addNode(String command, String name, boolean isNative, boolean modem){
		if(modem){
			if(modemIsSet){
				System.err.println(groupName+" group can contain max one modem!!");
				return;
			}
			modemIsSet = true;
		}
		nodeConfigs.add(new MyNodeConfig(command.split(" "), name, isNative, modem));
	}

	public void addNode(String[] command, String name, boolean isNative, boolean modem){
		
		if(modem){
			if(modemIsSet){
				System.err.println(groupName+" group can contain max one modem!!");
				return;
			}
			modemIsSet = true;
		}
		nodeConfigs.add(new MyNodeConfig(command, name, isNative, modem));
	}
	
	public MyNodeConfig getModemConfig(){
		return modemConfig;
	}
	
	
	public void setModem(ModemContainer mc){
		if(myModem!= null){
			System.err.println("["+groupName+"] setModem(): each group should have only " +
					"one modem!!!, you are trying to add second one: "+mc.getName());
			return;
		}
		modemIsSet = true;
		myModem = mc;
	}
	
	public ModemContainer getModem(){
		if(!modemIsSet){
			System.err.println("["+groupName+"] getModem(): this group does not seem "
					+"to contain any modem !!");
			return null;
		}
		return myModem;
	}

	public class MyNodeConfig{

		// is one or more strings representing the class name or process name
		public final String[] command;
		public String name;	// note that this name is changed by the name provider later on
		public final boolean isNative;
		public boolean printToConsole = false;	// set this true if you want to see stdOUT and ERRs

		public boolean ismodem;	// whether the node is modem
		
		public String uniqueName;

		public MyNodeConfig(String command, String name, boolean isNative, boolean ismodem){
			this.name = name;
			this.isNative =isNative;
			this.command = new String[]{command};
			this.ismodem = ismodem;
		}

		public MyNodeConfig(String[] command, String name, boolean isNative, boolean ismodem){
			if(!isNative && command.length>1)
				System.err.println("MyNodeConfig: Java nodes class name should" +
						" consist of only one String!");
			this.name = name;
			this.isNative =isNative;
			this.command = command;
			this.ismodem = ismodem;
		}
	}

	@Override
	public String getFullName() { return this.groupName; }

	@Override
	public StartupManager getStartupManager() { return this.startup; }

	@Override
	public boolean isStarted() { return this.isStarted; }
}
