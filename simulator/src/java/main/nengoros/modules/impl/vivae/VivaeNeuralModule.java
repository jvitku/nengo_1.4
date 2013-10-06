package nengoros.modules.impl.vivae;

import java.util.HashMap;

import ca.nengo.model.Node;
import ca.nengo.model.StructuralException;
import ca.nengo.util.ScriptGenException;
import nengoros.comm.nodeFactory.modem.ModemContainer;
import nengoros.exceptions.ConnectionException;
import nengoros.modules.AbsNeuralModule;
import nengoros.modules.impl.vivae.impl.SimulationControls;

/**
 * This module represents ViVae simulator in the Nengo
 * Original ViVae simulator implemented by:
 * http://cig.felk.cvut.cz/projects/robo/ 
 * 
 * This has no IO by default (except simulation controls), origins/terminaitons
 * are added by adding vivae agent.
 * 
 * @author Jaroslav Vitku
 *
 */
public class VivaeNeuralModule extends AbsNeuralModule{

	private static final long serialVersionUID = 1L;
	public static final String me = "[VivaeNeuralModule] ";

	private SimulationControls sc;

	/**
	 * Initialize complete smart neuron, that means 
	 * modem and a corresponding ros node. 
	 * Ros node can be aither rosjava node or native C++ node.
	 * 
	 * @param name name of smart neuron
	 */
	public VivaeNeuralModule(String name, ModemContainer modContainer){
		super(name, modContainer);

		// connect the simulation controller to the ROS network
		try {
			sc = new SimulationControls(this, super.mc.getModem().getConnectedNode());
		} catch (ConnectionException e) {
			sc=null;
			System.err.println(me+"my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}
	}

	/**
	 * This is how we can control the simulation run
	 * @return
	 */
	public SimulationControls getControls(){
		return sc;
	}

	public void addAgent(String name) throws StructuralException{
		sc.addAgent(name);
	}
	
	public VivaeAgent getAgent(String name){
		if(sc.getAgents().containsKey(name)){
			return sc.getAgents().get(name);
		}
		System.err.println("VivaeNeuron: error: agent named: "+name+
				" is not found in this neuron !!");
		return null;
	}

	public void removeAgent(String name){
		sc.removeAgent(name);
	}

	/**
	 * is called from Nengo while opening the simulation window
	 */
	@Override
	public void reset(boolean randomize) {
		//sc.stop();
		//sc.start();
		
		// TODO: Vivae has to be reloaded in order to work correctly with Nengo..
		sc.reset(); 
		mc.resetModem();
	}

	/**
	 * being deleted from Nengo GUI?
	 */
	@Override
	public void notifyAboutDeletion() {
		System.out.print(me+"OK, I am being deleted, will close ROS componnets and Vivae"+getName());
		sc.stop();
		sc.destroy();
		mc.stop(); 
	}

	@Override
	public Node[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toScript(HashMap<String, Object> scriptData)
			throws ScriptGenException {
		// TODO Auto-generated method stub
		System.err.println("TODO: toScript not implemented so far!");
		return null;
	}


}
