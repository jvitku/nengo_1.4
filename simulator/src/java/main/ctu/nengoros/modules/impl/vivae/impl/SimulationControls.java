package ctu.nengoros.modules.impl.vivae.impl;

import java.util.HashMap;

import ctu.nengoros.comm.rosutils.communication.SynchronousService;
import ctu.nengoros.modules.AbsNeuralModule;
import ctu.nengoros.modules.impl.vivae.Sim;
import ctu.nengoros.modules.impl.vivae.VivaeAgent;
import ctu.nengoros.modules.impl.vivae.VivaeNeuralModule;

import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;

import ctu.nengoros.modules.impl.vivae.Controls;
import ca.nengo.model.StructuralException;

/**
 * This handles the Vivae simulation over the ROS network. 
 * The important things are:
 * 	-loadMap
 * 	-start
 * 	-stop
 *  -destroy
 *  
 *  -addAgent	 (adds Origin and Termination to the parent Neuron)
 *  -removeAgent (removes Origins and Terminations)
 *  
 * @author Jaroslav Vitku
 * 
 */
public class SimulationControls implements Controls{

	public static final String NAME = "SimulationControls";
	public final String me = "["+NAME+"] ";

	// map of agents in the network, do not try to add duplicate names please...
	private final HashMap<String,VivaeAgent> agents;

	SynchronousService<vivae.LoadMapRequest,vivae.LoadMapResponse> map;
	SynchronousService<vivae.SimControllerRequest,vivae.SimControllerResponse> controls;
	SynchronousService<vivae.SpawnRequest, vivae.SpawnResponse> spawn;

	// we need this in order to use ROS network
	protected final ConnectedNode cn;

	// add decoders and encoders to this neuron
	private final AbsNeuralModule an;
	
	public SimulationControls(VivaeNeuralModule vivaeNeuralModule, ConnectedNode cn){
		this.cn = cn;
		an =vivaeNeuralModule;
		agents = new HashMap<String, VivaeAgent>(3);

		// initialize the services in the ROS network
		try {

			// service for map loading
			ServiceClient<vivae.LoadMapRequest, vivae.LoadMapResponse> mapServiceClient =
					cn.newServiceClient(Sim.Msg.LOAD, vivae.LoadMap._TYPE);
			// service for controlling the simulation
			ServiceClient<vivae.SimControllerRequest, vivae.SimControllerResponse> simServiceClient =
					cn.newServiceClient(Sim.Msg.CONTROL, vivae.SimController._TYPE);

			// service for spawning the agents
			ServiceClient<vivae.SpawnRequest, vivae.SpawnResponse> serviceClient = 
					cn.newServiceClient(Sim.Msg.SPAWN, vivae.Spawn._TYPE);

			// make them synchronous
			controls = new SynchronousService<vivae.SimControllerRequest, vivae.SimControllerResponse>(simServiceClient);
			map      = new SynchronousService<vivae.LoadMapRequest, vivae.LoadMapResponse>(mapServiceClient);
			spawn 	 = new SynchronousService<vivae.SpawnRequest, vivae.SpawnResponse>(serviceClient);


		} catch (ServiceNotFoundException e) {
			throw new RosRuntimeException(e);
		}
	}

	public HashMap<String, VivaeAgent> getAgents(){
		return agents;
	}
	
	@Override
	public void start(){
		vivae.SimControllerRequest req = controls.getRequest();
		req.setWhat(Sim.Cmd.START);
		controls.callService(req);
	}

	@Override
	public void stop(){
		vivae.SimControllerRequest req = controls.getRequest();
		req.setWhat(Sim.Cmd.STOP);
		controls.callService(req);
	}

	@Override
	public void destroy(){
		vivae.SimControllerRequest req = controls.getRequest();
		req.setWhat(Sim.Cmd.DESTROY);
		controls.callService(req);
	}

	@Override
	public boolean init() {
		vivae.SimControllerRequest req = controls.getRequest();
		req.setWhat(Sim.Cmd.INIT);
		vivae.SimControllerResponse resp = controls.callService(req);
		return resp.getOk();
	}
	
	@Override
	public boolean reset() {
		vivae.SimControllerRequest req = controls.getRequest();
		req.setWhat(Sim.Cmd.RESET);
		vivae.SimControllerResponse resp = controls.callService(req);
		return resp.getOk();
	}

	@Override
	public boolean setVisible(boolean visible) {
		vivae.SimControllerRequest req = controls.getRequest();
		if(visible){
			req.setWhat(Sim.Cmd.SETVISIBLE);
			vivae.SimControllerResponse resp = controls.callService(req);
			return resp.getOk();
		}else{
			req.setWhat(Sim.Cmd.SETINVISIBLE);
			vivae.SimControllerResponse resp = controls.callService(req);
			return resp.getOk();
		}
		
	}
	
	
	@Override
	public boolean loadMap(String path) {
		vivae.LoadMapRequest req = map.getRequest();
		req.setName(path);
		vivae.LoadMapResponse resp = map.callService(req);
		return resp.getLoadedOK();
	}

	/**
	 * Send the request for spawning the agent, values -1 mean default setting.
	 * @throws StructuralException 
	 */
	@Override
	public void addAgent(String name, int numSensors, double maxDistance, double frictionSensor) 
			throws StructuralException{
		if(agents.containsKey(name)){
			System.err.println(me+"already have agent with this name, names must be unique! "+name);
			return;
		}
		//System.out.println(me+"Sending request for the agent named: "+name);
		vivae.SpawnRequest req = spawn.getRequest();
		req.setName(name);
		req.setNumSensors(numSensors);
		req.setMaxDistance((float)maxDistance);
		req.setFrictionDistance((float)frictionSensor);
		vivae.SpawnResponse resp = spawn.callService(req);

		if(resp.getSpawnedOK()){
			if(agents.containsKey(resp.getName())){
				System.err.println(me+"local list of agents already contains this name, ignoring! "+resp.getName());
				return;
			}
			String n = resp.getName();
			String p = resp.getPubTopicName();
			String s = resp.getSubTopicName();
			
			int sensoryDataLength = resp.getNumSensors();			
			// build the agent, register encoders and decoders, add it to the list of agents, we are done
			SimpleControlledAgent a = new SimpleControlledAgent(an, n, p, s, sensoryDataLength);
			agents.put(name, a);			
			System.out.println(me+"agent named: "+n+" successfuly connected to both networks");
			
		}else{
			System.out.println(me+"error spawning agent");
		}
	}

	@Override
	public void addAgent(String name, int numSensors) throws StructuralException{
		addAgent(name, numSensors, -1, -1);
	}

	@Override
	public void addAgent(String name) throws StructuralException {
		addAgent(name, -1, -1, -1);
	}

	@Override
	public void removeAgent(String name) {
		if(!agents.containsKey(name)){
			System.err.println(me+"cannot find this agent in the map: "+name);
			return;
		}
		System.out.println(me+"removing agent named: "+name);
		//TODO delete terminations and origins..
		agents.remove(name);
	}


}

