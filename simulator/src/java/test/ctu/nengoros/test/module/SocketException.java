package ctu.nengoros.test.module;

import static org.junit.Assert.*;

import org.junit.Test;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.modules.NeuralModule;
import ctu.nengoros.modules.impl.DefaultNeuralModule;

public class SocketException {

	public static String OR 		= "ctu.nengoros.testsuit.demo.nodes.gate.OR";

	@Test
	public void communicationWorks(){
		RosUtils.setAutorun(true); // disable Nengoros core autorun, the @BeforeClass is used

		String name = "myName";
		NodeGroup g = new NodeGroup("OR", true);
		g.addNode(OR, "OR", "java");
		NeuralModule module = null;
		
		try {
			module = new DefaultNeuralModule(name+"_OR", g);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		module.createEncoder("logic/gates/ina", "bool", 1);
		/*
		module.createEncoder("logic/gates/inb", "bool", 1);
		module.createDecoder("logic/gates/outa", "bool", 1);		
*/
		// TODO: find out how to test network parts
		//Network net = new NetworkImpl();
		//net.ad
		
		RosUtils.utilsShallStop();
	}

	
}
