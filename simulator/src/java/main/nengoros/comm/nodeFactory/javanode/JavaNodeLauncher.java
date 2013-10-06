package nengoros.comm.nodeFactory.javanode;

import java.util.List;

import nengoros.comm.nodeFactory.NodeGroup;
import nengoros.comm.nodeFactory.javanode.impl.JNodeContainer;
import nengoros.comm.nodeFactory.modem.Modem;
import nengoros.comm.nodeFactory.modem.ModemContainer;
import nengoros.comm.nodeFactory.modem.impl.ModContainer;

import org.ros.exception.RosRuntimeException;
import org.ros.internal.loader.CommandLineLoader;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class JavaNodeLauncher {

	public static ModemContainer launchModem(List<String> launchCommand, final String name, 
			NodeMainExecutor nme, NodeGroup g){

		NodeMain launchedOne = null;	
		CommandLineLoader loader = new CommandLineLoader(launchCommand);
		//CommandLineLoader loader = new CommandLineLoader(Lists.newArrayList(launchCommand));
		
		String nodeClassName = loader.getNodeClassName();
		NodeConfiguration nc = loader.build();
		
		try {
			launchedOne = (NodeMain) loader.loadClass(nodeClassName);
		} catch (ClassNotFoundException e) {
			throw new RosRuntimeException("Unable to locate node: " + nodeClassName, e);
		} catch (InstantiationException e) {
			throw new RosRuntimeException("Unable to instantiate node: " + nodeClassName, e);
		} catch (IllegalAccessException e) {
			throw new RosRuntimeException("Unable to instantiate node: " + nodeClassName, e);
		}

		Preconditions.checkState(launchedOne != null);
		ModemContainer mod = new ModContainer(nc, name, nme,g);
		mod.setModem((Modem)launchedOne);
		
		return mod;
	}
	
	/**
	 * Return java implementation of ROS node registered into ROS
	 * network and waiting for start.
	 * @param nc parsed custom configuration of node to be launched
	 * @return registered and waiting node if everything OK
	 */
	
	public static JavaNodeContainer launchNode(List<String> launchCommand, final String name, 
			NodeMainExecutor nme){

		NodeMain launchedOne = null;	
		CommandLineLoader loader = new CommandLineLoader(launchCommand);
		//CommandLineLoader loader = new CommandLineLoader(Lists.newArrayList(launchCommand));
		
		String nodeClassName = loader.getNodeClassName();
		NodeConfiguration nc = loader.build();
		
		try {
			launchedOne = (NodeMain) loader.loadClass(nodeClassName);
		} catch (ClassNotFoundException e) {
			throw new RosRuntimeException("Unable to locate node: " + nodeClassName, e);
		} catch (InstantiationException e) {
			throw new RosRuntimeException("Unable to instantiate node: " + nodeClassName, e);
		} catch (IllegalAccessException e) {
			throw new RosRuntimeException("Unable to instantiate node: " + nodeClassName, e);
		}

		Preconditions.checkState(launchedOne != null);
		JavaNodeContainer jn = new JNodeContainer(nc, name, nme);
		jn.setNodeMain(launchedOne);
		
		return jn;
	}
	
	public static JavaNodeContainer launchNode(String[] launchCommand, final String name,
			NodeMainExecutor nme){

		NodeMain launchedOne = null;	
		CommandLineLoader loader = new CommandLineLoader(Lists.newArrayList(launchCommand));
		
		String nodeClassName = loader.getNodeClassName();
		NodeConfiguration nc = loader.build();
		
		try {
			launchedOne = (NodeMain) loader.loadClass(nodeClassName);
		} catch (ClassNotFoundException e) {
			throw new RosRuntimeException("Unable to locate node: " + nodeClassName, e);
		} catch (InstantiationException e) {
			throw new RosRuntimeException("Unable to instantiate node: " + nodeClassName, e);
		} catch (IllegalAccessException e) {
			throw new RosRuntimeException("Unable to instantiate node: " + nodeClassName, e);
		}

		Preconditions.checkState(launchedOne != null);
		
		JavaNodeContainer jn = new JNodeContainer(nc, name, nme);
		jn.setNodeMain(launchedOne);
		
		return jn;
	}
}


