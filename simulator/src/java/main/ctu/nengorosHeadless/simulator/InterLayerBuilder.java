package ctu.nengorosHeadless.simulator;

import org.hanns.logic.crisp.gates.impl.AND;
import org.hanns.logic.crisp.gates.impl.NAND;
import org.hanns.logic.crisp.gates.impl.OR;

import ca.nengo.model.StructuralException;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;

public class InterLayerBuilder {

	public static NeuralModule addAND(int inputLayer, int outputLayer, EALayeredSimulator sim)
			throws ConnectionException, StartupDelayException, StructuralException{
		NeuralModule ms = NodeBuilder.nandGate("and");
		sim.getNodes().add(ms);

		sim.registerOrigin(ms.getOrigin(AND.outAT), outputLayer);
		sim.registerTermination(ms.getTermination(AND.inAT), inputLayer);
		sim.registerTermination(ms.getTermination(AND.inBT), inputLayer);
		return ms;
	}
	
	public static NeuralModule addNAND(int inputLayer, int outputLayer, EALayeredSimulator sim)
			throws ConnectionException, StartupDelayException, StructuralException{
		NeuralModule ms = NodeBuilder.nandGate("nand");
		sim.getNodes().add(ms);

		sim.registerOrigin(ms.getOrigin(NAND.outAT), outputLayer);
		sim.registerTermination(ms.getTermination(NAND.inAT), inputLayer);
		sim.registerTermination(ms.getTermination(NAND.inBT), inputLayer);
		return ms;
	}

	public static NeuralModule addOR(int inputLayer, int outputLayer, EALayeredSimulator sim)
			throws ConnectionException, StartupDelayException, StructuralException{
		NeuralModule ms = NodeBuilder.orGate("or");
		sim.getNodes().add(ms);

		sim.registerOrigin(ms.getOrigin(OR.outAT), outputLayer);
		sim.registerTermination(ms.getTermination(OR.inAT), inputLayer);
		sim.registerTermination(ms.getTermination(OR.inBT), inputLayer);
		return ms;
	}

}
