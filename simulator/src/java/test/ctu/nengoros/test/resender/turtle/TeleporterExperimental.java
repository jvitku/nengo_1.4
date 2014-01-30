package ctu.nengoros.test.resender.turtle;


import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

/**
 * 
 * @author Jaroslav Vitku
 * TODO 
 */
public class TeleporterExperimental extends AbstractNodeMain {

	//private final java.lang.String ann2rrs = turtlesim.TeleportAbsolute._TYPE;
	//	private final java.lang.String ann2ros = "turtle1/command_velocity";
	private final java.lang.String me = "TurtleCommander";

	@Override
	public GraphName getDefaultNodeName() { return GraphName.of(me); }

	@Override
	public void onStart(final ConnectedNode connectedNode) {
		/*
		final Publisher<turtlesim.TeleportAbsoluteRequest> publisher =
				connectedNode.newPublisher(ann2rrs, turtlesim.TeleportAbsolute._TYPE);

		ServiceClient<turtlesim.TeleportAbsoluteRequest, turtlesim.TeleportAbsoluteResponse> sc;
		try {
			sc = connectedNode.newServiceClient("teleport", turtlesim.TeleportAbsolute._TYPE);
		} catch (ServiceNotFoundException e) {
			throw new RosRuntimeException(e);
		}*/

		// this does not work, turtlesim_node does not provide teleporting service..
	}

}