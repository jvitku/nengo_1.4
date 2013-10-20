package ctu.nengoros.modules.impl;

import java.util.HashMap;

import ctu.nengoros.comm.nodeFactory.modem.ModemContainer;
import ctu.nengoros.modules.AbsNeuralModule;
import ca.nengo.model.Node;
import ca.nengo.util.ScriptGenException;

/**
 * Synchronized version of DefaultAsynNeuralModule
 * 
 * @author Jaroslav Vitku
 *
 */
public class DefaultNeuralModule extends AbsNeuralModule{ 
	

	private static final long serialVersionUID = 1L;
	public static final String me = "[DefaultSynchNeuralModule] ";

	/**
	 * Initialize complete smart neuron, that means 
	 * modem and a corresponding ROS node. 
	 * ROS node can be either rosjava node or native C++ node.
	 * 
	 * @param name name of smart neuron
	 */
	public DefaultNeuralModule(String name, ModemContainer modContainer){
		super(name, modContainer);
	}

	@Override
	public Node[] getChildren() {
		// TODO Auto-generated method stub	
		return null;
	}

	@Override
	public String toScript(HashMap<String, Object> scriptData)
			throws ScriptGenException {
		System.err.println("TODO: toScript not implemented so far!");
		// TODO Auto-generated method stub
		return null;
	}


	
	
}
