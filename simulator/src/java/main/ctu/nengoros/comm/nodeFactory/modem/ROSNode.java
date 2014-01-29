package ctu.nengoros.comm.nodeFactory.modem;

import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

/**
 * This Interface just reminds of what ROS node should be able to do.
 * It is an exact analogue of AbstractNodeMain (from rosjava)
 * 
 * @author Jaroslav Vitku
 *
 */
public interface ROSNode extends /*ObjectReady,*/ NodeMain{ // TODO synch here

	@Override
	public GraphName getDefaultNodeName();

	@Override
	public void onStart(ConnectedNode connectedNode);

	@Override
	public void onShutdown(Node node);

	@Override
	public void onShutdownComplete(Node node);

	@Override
	public void onError(Node node, Throwable throwable);
}
