package nengoros.nodeFactory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;

public class NameProviderNamespaceTest {

	/**
	 * start dependent group of nodes wihtout namespace specified
	 */
	@Test
	public void dependentGroup(){
		
		assertEquals(0,RosUtils.getNumOfGroups());
		
		String talker = "resender.mt.IntSender";
		String receiveer = "resender.mt.IntReceiver";
		
		// group called pubsub which is independent?
		NodeGroup g = new NodeGroup("pubsub",false);
		g.addNode(talker, "talker", "java");
		assertEquals(1,g.getNames().length);
		
		g.addNode(receiveer, "receiver", "java");		
		assertEquals(2,g.getNames().length);
		
		g.startGroup();
		assertEquals(2,g.nodes.length);	
		
		assertEquals(1,RosUtils.getNumOfGroups());
		
		Mess.wait(1);
		
		g.stopGroup();
		assertEquals(0,RosUtils.getNumOfGroups());
		
		// nodes (if some are running) should stop, so as roscore..
		RosUtils.utilsShallStop();		
	}
	
	@Test
	public void independentGroup(){
		
		assertEquals(0,RosUtils.getNumOfGroups());
		
		String talker = "resender.mt.IntSender";
		String receiveer = "resender.mt.IntReceiver";
		
		// group called pubsub which is independent?
		NodeGroup g = new NodeGroup("pubsub",true);
		g.addNode(talker, "talker", "java");
		assertEquals(1,g.getNames().length);
		
		g.addNode(receiveer, "receiver", "java");		
		assertEquals(2,g.getNames().length);
		
		g.startGroup();
		assertEquals(2,g.nodes.length);	
		
		assertEquals(1,RosUtils.getNumOfGroups());
		
		Mess.wait(1);
		
		g.stopGroup();
		assertEquals(0,RosUtils.getNumOfGroups());

		// nodes (if some are running) should stop, so as roscore..
		RosUtils.utilsShallStop();		
	}
	
	@Test
	public void startNamespacedGroup(){
		
		assertEquals(0,RosUtils.getNumOfGroups());
		
		String talker = "resender.mt.IntSender";
		String receiveer = "resender.mt.IntReceiver";
		
		// group called pubsub which is independent?
		NodeGroup g = new NodeGroup("pubsub","namespaceString");
		g.addNode(talker, "talker", "java");
		assertEquals(1,g.getNames().length);
		
		g.addNode(receiveer, "receiver", "java");		
		assertEquals(2,g.getNames().length);
		
		g.startGroup();
		assertEquals(2,g.nodes.length);	
		
		assertEquals(1,RosUtils.getNumOfGroups());
		
		Mess.wait(1);
		
		g.stopGroup();
		assertEquals(0,RosUtils.getNumOfGroups());

		// nodes (if some are running) should stop, so as roscore..
		RosUtils.utilsShallStop();		
	}
}
