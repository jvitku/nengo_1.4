package ctu.nengoros.testsuit.demo;

import static org.junit.Assert.*;

import org.junit.Test;

import ctu.nengoros.RosRunner;
import ctu.nengoros.nodes.RosCommunicationTest;
import ctu.nengoros.testsuit.demo.nodeTesters.SubscriberTestNode;

/**
 * Example how to test ROS node-based components.
 *  
 * @author Jaroslav Vitku
 *
 */
public class SubscriberTests extends RosCommunicationTest{

	/**
	 * Testing the subscriber:
	 * <ul>
	 * 	<li>start tested subscriber and tester node</li>
	 * 	<li>obtain the tester node<li>
	 * 	<li>wait for tester to be registered and to establish the communication between nodes</li>
	 * 	<li>give them 500ms for communication</li>
	 * 	<li>shutdown nodes</li>
	 * </ul>
	 */
	@Test
	public void testDemoSubscriberConnections() {
		
		RosRunner subscriberRunner = 
				super.runNode("ctu.nengoros.testsuit.demo.nodes.DemoSubscriber");		
		RosRunner subscriberTestRunner = 
				super.runNode("ctu.nengoros.testsuit.demo.nodeTesters.SubscriberTestNode");
		
		SubscriberTestNode st = null;
		
		if(!(subscriberTestRunner.getNode() instanceof SubscriberTestNode))
			fail("Wrong class launched");
		else
			st = (SubscriberTestNode)subscriberTestRunner.getNode();
		if(st==null)
			fail("Could not load DemoSubscriberTester node");
		
		st.awaitCommunicationReady();
		System.out.println("Nodes registered, communication between nodes established.");
		
		super.sleep(500);	// communicate
		
		subscriberRunner.stop();
		subscriberTestRunner.stop();
		
		assertFalse(subscriberRunner.isRunning());
		assertFalse(subscriberTestRunner.isRunning());
	}
}
