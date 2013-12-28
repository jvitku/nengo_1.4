package ctu.nengoros.testsuit.demo.nodes.gate;

import org.ros.namespace.GraphName;

import ctu.nengoros.testsuit.demo.nodes.gate.util.MisoGate;

public class OR extends MisoGate{

	
	public static final String inBT = "logic/gates/inb";
	public static final String inAT = "logic/gates/ina";	// topic
	public static final String outAT = "logic/gates/outa";

	@Override
	public boolean compute(boolean a, boolean b) { return (a | b); }
	
	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("OR"); }
}
