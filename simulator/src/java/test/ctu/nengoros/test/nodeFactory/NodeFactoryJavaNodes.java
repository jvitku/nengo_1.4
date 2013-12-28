package ctu.nengoros.test.nodeFactory;

import static org.junit.Assert.*;

import org.junit.Test;

import ctu.nengoros.comm.nodeFactory.NodeFactory;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.RosUtils;

/**
 * Test basic properties of launching of Java nodes
 *  
 * @author Jaroslav Vitku
 *
 */
public class NodeFactoryJavaNodes {

	public static String className = "resender.onoff.Talker";
	
	@Test
	public void launchOneJavaNode(){
		
		assertEquals(NodeFactory.numOfRunningNodes(),0);

		NodeGroup n = new NodeGroup("test", true);
		n.addNode(className, "testNode", "java");
		n.startGroup();
		
		assertEquals(NodeFactory.numOfRunningNodes(),2);
		Mess.wait(2);
		n.stopGroup();
		assertEquals(NodeFactory.numOfRunningNodes(),0);

		RosUtils.utilsShallStop();
		Mess.wait(1);

	}

	
	@Test
	public void launchTwoNodes(){
		assertEquals(NodeFactory.numOfRunningNodes(),0);

		NodeGroup n = new NodeGroup("test", false);
		n.addNode(className, "testNode", "java");
		n.startGroup();
		
		assertEquals(NodeFactory.numOfRunningNodes(),2); // also modem
		
		NodeGroup n2 = new NodeGroup("test", false);
		n2.addNode(className, "testNode", "java");
		n2.startGroup();
		
		assertEquals(NodeFactory.numOfRunningNodes(),4);
		Mess.wait(2);
		n2.stopGroup();
		assertEquals(NodeFactory.numOfRunningNodes(),2);
		
		Mess.wait(1);
		n.stopGroup();
		assertEquals(NodeFactory.numOfRunningNodes(),0);
		
		RosUtils.utilsShallStop();
		Mess.wait(1);

	}
	
}
