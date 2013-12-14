package ctu.nengoros.comm.nodeFactory.javanode;

import java.util.List;

import org.ros.exception.RosRuntimeException;
import org.ros.internal.loader.CommandLineLoaderII;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.nodeFactory.javanode.impl.JNodeContainer;
import ctu.nengoros.comm.nodeFactory.modem.Modem;
import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.comm.nodeFactory.modem.impl.ModContainer;

public class JavaNodeLauncher {

	public static ModemContainer launchModem(List<String> launchCommand, final String name, 
			NodeMainExecutor nme, NodeGroup g){

		NodeMain launchedOne = null;	
		CommandLineLoaderII loader = new CommandLineLoaderII(launchCommand);
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
		CommandLineLoaderII loader = new CommandLineLoaderII(launchCommand);
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
		CommandLineLoaderII loader = new CommandLineLoaderII(Lists.newArrayList(launchCommand));
		
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


