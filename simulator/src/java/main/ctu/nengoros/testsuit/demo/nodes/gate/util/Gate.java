package ctu.nengoros.testsuit.demo.nodes.gate.util;

import org.apache.commons.logging.Log;
import org.ros.node.topic.Publisher;

import ctu.nengoros.network.node.testsuit.CommunicationAwareNode;


/**
 * Abstract logical gate.
 * 
 * @author Jaroslav Vitku
 *
 * @param <T>
 */
public abstract class Gate<T> extends CommunicationAwareNode {

	protected boolean SEND = false; 
	
	// everything handled asynchronously by listeners
	protected final int sleepTime = 1000;

	// each gate has one output
	protected Publisher<T> publisher;	
	public final String yT = "logic/gates/outa";
	public static final String inAT = "logic/gates/ina";	// topic
	
	protected Log log;
	
	// this should be set to true after everything is ready in onStart()
	protected volatile boolean inited = false;
	
	protected abstract void send();

}
