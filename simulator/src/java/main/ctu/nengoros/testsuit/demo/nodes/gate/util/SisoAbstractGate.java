package ctu.nengoros.testsuit.demo.nodes.gate.util;

import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * @author Jaroslav Vitku vitkujar@fel.cvut.cz
 * 
 */
public abstract class SisoAbstractGate<T> extends Gate<T> {

	// data communication channel
	protected Subscriber<T> subscriberA;
	public static final String inAT = "logic/gates/ina";	// topic
	
	// data configuration topic (optional)
	public static final String confAT = "logic/gates/confa";
	
	// output data channel
	protected Publisher<T> publisherA;
	public final String outAT = "logic/gates/outa";
	
	
	/**
	 * implement this in order to make computation 
	 * @param a input value A
	 * @return output value Y
	 */
	//public abstract boolean compute(T a);

}
