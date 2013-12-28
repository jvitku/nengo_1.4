package ctu.nengoros.testsuit.demo.nodes.gate.util;

import org.ros.node.topic.Subscriber;

/**
 * 
 * Abstract logical MISO gate extends SISO gate with one input.
 * 
 * @author Jaroslav Vitku vitkujar@fel.cvut.cz
 * 
 */
public abstract class MisoAbstractGate<T> extends SisoAbstractGate<T> {

	protected Subscriber<T> subscriberA, subscriberB;
	
	// data input topics
	public static final String inBT = "logic/gates/inb";
	public static final String inCT = "logic/gates/inc";
	public static final String indDT = "logic/gates/ind";
	public static final String indET = "logic/gates/ine";
	public static final String indFT = "logic/gates/inf";
	
	// data output topic 
	public static final String outBT = "logic/gates/outb";
	public static final String outCT = "logic/gates/outc";
	public static final String outDT = "logic/gates/outd";
	public static final String outET = "logic/gates/oute";
	public static final String outFT = "logic/gates/outf";
	
	// configuration input topics
	public static final String confBT = "logic/gates/confb";
	public static final String confCT = "logic/gates/confc";
	public static final String confDT = "logic/gates/confd";
	public static final String confET = "logic/gates/confe";
	public static final String confFT = "logic/gates/conff";
}
