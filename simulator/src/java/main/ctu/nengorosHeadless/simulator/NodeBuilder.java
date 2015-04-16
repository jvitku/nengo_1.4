package ctu.nengorosHeadless.simulator;

import org.hanns.physiology.statespace.ros.BasicMotivation;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.impl.DefaultNeuralModule;

public class NodeBuilder {

	public static NeuralModule buildBasicMotivationSource(String name, int noInputs, float decay, int logPeriod, boolean synchornous)
			throws ConnectionException, StartupDelayException{
		
		String className = "org.hanns.physiology.statespace.ros.BasicMotivation";
		String[] command = new String[]{className, "_"+BasicMotivation.noInputsConf+ ":=:" + noInputs, 
				"_"+BasicMotivation.decayConf+":="+decay,
				"_"+BasicMotivation.logPeriodConf+":="+logPeriod};

		NodeGroup g = new NodeGroup("BasicMotivation", true);
		g.addNode(command, "BasicMotivation", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_BasicMotivation", g, synchornous);

		// connect the decay parameter to the Nengoros network (changed online)
		module.createConfigEncoder(BasicMotivation.topicDecay,"float", 1); 			//# decay config (unconneced=BasicMotivation.DEF_DECAY)

		module.createDecoder(BasicMotivation.topicDataOut, "float", 2);           	//# decode float[]{reward,BasicMotivation}
		module.createEncoder(BasicMotivation.topicDataIn, "float", noInputs); 		//# encode input data (sum rewards here)

		module.createDecoder(BasicMotivation.topicProsperity,"float", 1);			//# float[]{prosperity}  = MSD from the limbo area
		return module;
	}
	
	public static NeuralModule buildBasicMotivationReceiver(String name, int logPeriod, boolean synchronous) throws ConnectionException, StartupDelayException{
		
		String className = "org.hanns.physiology.statespace.ros.testnodes.MotivationReceiverAutoStart";
		String[] command = new String[]{className,
				"_"+BasicMotivation.logPeriodConf+":="+logPeriod};
		
		NodeGroup g = new NodeGroup("ReceiverFullReward", true);
		g.addNode(command, "ReceiverFullReward", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_ReceiverFullReward", g, synchronous);

		System.out.println("topicDataIn is; "+BasicMotivation.topicDataIn);
		module.createDecoder(BasicMotivation.topicDataIn, "float", 1);	// one reward sent           	
		module.createEncoder(BasicMotivation.topicDataOut, "float", 2); // reward and motivation received
		
		//module.
		
		//((MotivationReceiver)module).setAutoResponse(true);
		
		//MotivationReceiver mr;
		
		return module;
	}

}
