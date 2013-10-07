package service.sercli;

import org.ros.exception.ServiceException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceResponseBuilder;
import service.simulator.Simulator;
import service.simulator.SimulatorController;
import vivae.LoadMapRequest;
import vivae.LoadMapResponse;

/**
 * 
 * @author Jaroslav Vitku
 */
public class ControlsServer extends AbstractNodeMain implements Simulator{


	public final String me = "ControlsServer ";

	public static final java.lang.String loadSrv = "loadMapSerice";
	public static final java.lang.String v2n = "vivae2nengo";
	public static final java.lang.String n2v = "nengo2vivae";


	/**
	 * this just has to be here 
	 */
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("rosjava_tutorial_services/server");
	}

	/**
	 * Is called when the node starts (for more methods, @see AbstractNodeMain class )
	 */
	@Override
	public void onStart(ConnectedNode connectedNode) {

		// loading the maps to vivae
		LoadMapServiceResponseBuilder mapSrb = new LoadMapServiceResponseBuilder(this);
		connectedNode.newServiceServer("load_map", vivae.LoadMap._TYPE, mapSrb);

		// simulation controller over the ROS network
		SimControlServiceResponseBuilder srb = new SimControlServiceResponseBuilder(this);
		connectedNode.newServiceServer("add_two_ints", test_ros.AddTwoInts._TYPE,srb);



	}


	private class SimControlServiceResponseBuilder implements 
	ServiceResponseBuilder<test_ros.AddTwoIntsRequest, test_ros.AddTwoIntsResponse>{

		public SimControlServiceResponseBuilder(Simulator s){
			//	this.s = s;
		}

		@Override
		public void build(test_ros.AddTwoIntsRequest request, test_ros.AddTwoIntsResponse response) {
			System.out.println("server: setting the sum: "+request.getA()+" +"+request.getB());
			response.setSum(request.getA() + request.getB());
		}
	}


	private class LoadMapServiceResponseBuilder implements 
	ServiceResponseBuilder<vivae.LoadMapRequest, vivae.LoadMapResponse>{

		private final Simulator sim;

		public LoadMapServiceResponseBuilder(Simulator sim){
			this.sim = sim;
		}

		@Override
		public void build(LoadMapRequest req, LoadMapResponse resp)
				throws ServiceException {
			
			java.lang.String name = req.getName();	// get name of map
			System.out.println("Getting request to load this: "+name+" loading the map");

			if(sim.loadMap(name)){
				System.out.println("cannot load the file, waiting for another request");
				resp.setLoadedOK(false);
			}else{
				resp.setLoadedOK(true);
			}
			return;
		}
	}


	@Override
	public void startSimulation() {

	}

	@Override
	public void stopSimulation() {

	}

	@Override
	public void init() {
	}

	@Override
	public SimulatorController getController() {
		return null;
	}

	@Override
	public boolean loadMap(String pathToMap) {
		// TODO Auto-generated method stub
		return false;
	}
}
