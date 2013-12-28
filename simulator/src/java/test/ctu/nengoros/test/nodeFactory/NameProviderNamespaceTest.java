package ctu.nengoros.test.nodeFactory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;

public class NameProviderNamespaceTest {
	
	public static String talker = "ctu.nengoros.testsuit.demo.nodes.pubsub.DemoPublisher";		
	public static String receiveer = "ctu.nengoros.testsuit.demo.nodes.pubsub.DemoSubscriber";
	
	/**
	 * start dependent group of nodes wihtout namespace specified
	 */
	@Test
	public void dependentGroup(){
		
		assertEquals(0,RosUtils.getNumOfGroups());
		
		// group called pubsub which is independent?
		NodeGroup g = new NodeGroup("pubsub",false);
		g.addNode(talker, "talker", "java");
		assertEquals(1,g.getNames().length);
		
		g.addNode(receiveer, "receiver", "java");		
		assertEquals(2, g.getNames().length);
		
		g.startGroup();
		System.out.println("no of modes is :"+g.nodes.length);
		assertEquals(3, g.nodes.length);	// modem container added?	
		
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
		
		// group called pubsub which is independent?
		NodeGroup g = new NodeGroup("pubsub",true);
		g.addNode(talker, "talker", "java");
		assertEquals(1,g.getNames().length);
		
		g.addNode(receiveer, "receiver", "java");		
		assertEquals(2,g.getNames().length);
		
		g.startGroup();
		assertEquals(3,g.nodes.length);	
		
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
		
		// group called pubsub which is independent?
		NodeGroup g = new NodeGroup("pubsub","namespaceString");
		g.addNode(talker, "talker", "java");
		assertEquals(1,g.getNames().length);
		
		g.addNode(receiveer, "receiver", "java");		
		assertEquals(2,g.getNames().length);
		
		g.startGroup();
		assertEquals(3,g.nodes.length);	
		
		assertEquals(1,RosUtils.getNumOfGroups());
		
		Mess.wait(1);
		
		g.stopGroup();
		assertEquals(0,RosUtils.getNumOfGroups());

		// nodes (if some are running) should stop, so as roscore..
		RosUtils.utilsShallStop();		
	}
}
