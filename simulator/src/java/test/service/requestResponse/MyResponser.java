package service.requestResponse;

import org.ros.exception.ServiceException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceResponseBuilder;
import org.ros.node.service.ServiceServer;
import vivae.LoadMapRequest;
import vivae.LoadMapResponse;

/**
 * The main ROS node for Vivae simulator. 
 * This node should be launched first, it is able to start the 
 * simulator, load custom map and spawn new robots in the map.
 * 
 * This is a simple example of {@link ServiceServer} {@link NodeMain}.
 * 
 * @author Jaroslav Vitku
 *
 */
public class MyResponser extends AbstractNodeMain{

	public final String me = "VivaeSimulator ";
	
	public static final java.lang.String loadSrv = "loadMapSerice";
	public static final java.lang.String v2n = "vivae2nengo";
	public static final java.lang.String n2v = "nengo2vivae";
/*
	public final SimulatorController sc;
	
	public VivaeSimulator(){
		sc = new SimulatorController();
		System.out.println(me+"constructor loaded");
	}
	
	*/
	
	
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("VivaeSimulator");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {

		//System.out.println("launching vivae from ROS node");
		//FRNNExperiment.main(null);

		loadMapService(connectedNode);
		System.out.println("xxxxxxxxxxxxxxxxxx Ready");
	}

	private void loadMapService(ConnectedNode connectedNode){

		connectedNode.newServiceServer(loadSrv, vivae.LoadMap._TYPE,
				new ServiceResponseBuilder<vivae.LoadMapRequest, vivae.LoadMapResponse>() {

			@Override
			public void build(LoadMapRequest req, LoadMapResponse resp)
					throws ServiceException {
				java.lang.String name = req.getName();	// get name of map
				System.out.println("Getting request to load this: "+name);
				
				try{
					System.out.println("loading map eeeeer");
				//	FRNNExperiment.main(new String[]{name});
				}catch(Exception e){
					System.out.println("cannot load the file, waiting for another request");
					resp.setLoadedOK(false);
					return;
				}
				resp.setLoadedOK(true);
			}
		});
	}

}
