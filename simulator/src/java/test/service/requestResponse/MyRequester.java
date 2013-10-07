package service.requestResponse;

import java.io.IOException;
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import vivae.LoadMapResponse;

/**
 * Press enter and this thing will request loading vivae with selected map.
 * 
 * @author Jaroslav Vitku
 *
 */
public class MyRequester extends AbstractNodeMain {

	/**
	 * default name of the Node
	 */
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("myPublisher");
	}

	ServiceClient<vivae.LoadMapRequest, vivae.LoadMapResponse> serviceClient;

	String[] names = new String[]{"data/scenarios/arena1.svg", "data/scenarios/arena2.svg", "data/scenarios/ushape.svg" };

	@Override
	public void onStart(final ConnectedNode connectedNode) {

/*
		int poc = 0;
		while(true){
			System.out.println("press any key to request a map");
			try {
				System.in.read();
			} catch (IOException e) { e.printStackTrace(); }
			System.out.println("requesting this: "+names[poc]);
			requestMap(connectedNode, names[poc++]);
			if(poc>names.length-1)
				poc =0;
		}

*/
	}
/*
	private void requestMap(ConnectedNode connectedNode, String name){

		try {
			// try to subscribe to the service
			serviceClient = connectedNode.newServiceClient(VivaeSimulator.loadSrv, vivae.LoadMap._TYPE);
		} catch (ServiceNotFoundException e) {
			throw new RosRuntimeException(e);
		}
		final vivae.LoadMapRequest req = serviceClient.newMessage();
		req.setName(name);

		serviceClient.call(req, new ServiceResponseListener<vivae.LoadMapResponse>() {

			@Override
			public void onFailure(RemoteException e) {
				throw new RosRuntimeException(e);
			}

			@Override
			public void onSuccess(LoadMapResponse resp) {
				boolean ok = resp.getLoadedOK();
				System.out.println("vivae says that it was laoded " +ok);
			}
		});
	}*/

}
