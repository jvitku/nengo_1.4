package ctu.nengorosHeadless.simulator;

import org.hanns.physiology.statespace.ros.BasicMotivation;
import org.hanns.rl.discrete.ros.srp.QLambda;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.impl.DefaultNeuralModule;

public class NodeBuilder {

	public static final boolean SYNC= true;

	public static NeuralModule buildBasicMotivationSource(String name, int noInputs, float decay, int logPeriod)
			throws ConnectionException, StartupDelayException{

		String className = "org.hanns.physiology.statespace.ros.BasicMotivation";
		String[] command = new String[]{className, "_"+BasicMotivation.noInputsConf+ ":=:" + noInputs, 
				"_"+BasicMotivation.decayConf+":="+decay,
				"_"+BasicMotivation.logPeriodConf+":="+logPeriod};

		NodeGroup g = new NodeGroup("BasicMotivation", true);
		g.addNode(command, "BasicMotivation", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_BasicMotivation", g, SYNC);

		// connect the decay parameter to the Nengoros network (changed online)
		module.createConfigEncoder(BasicMotivation.topicDecay,"float", 1); 			//# decay config (unconneced=BasicMotivation.DEF_DECAY)

		module.createDecoder(BasicMotivation.topicDataOut, "float", 2);           	//# decode float[]{reward,BasicMotivation}
		module.createEncoder(BasicMotivation.topicDataIn, "float", noInputs); 		//# encode input data (sum rewards here)

		module.createDecoder(BasicMotivation.topicProsperity,"float", 1);			//# float[]{prosperity}  = MSD from the limbo area
		return module;
	}

	public static NeuralModule buildBasicMotivationReceiver(String name, int logPeriod) throws ConnectionException, StartupDelayException{

		String className = "org.hanns.physiology.statespace.ros.testnodes.MotivationReceiverAutoStart";
		String[] command = new String[]{className,
				"_"+BasicMotivation.logPeriodConf+":="+logPeriod};

		NodeGroup g = new NodeGroup("ReceiverFullReward", true);
		g.addNode(command, "ReceiverFullReward", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_ReceiverFullReward", g, SYNC);

		System.out.println("topicDataIn is; "+BasicMotivation.topicDataIn);
		module.createDecoder(BasicMotivation.topicDataIn, "float", 1);	// one reward sent           	
		module.createEncoder(BasicMotivation.topicDataOut, "float", 2); // reward and motivation received

		return module;
	}


	public static NeuralModule qlambdaASM(String name, int noStateVars, int noActions, int noValues, int logPeriod, int maxDelay,
			int prospLen) throws ConnectionException, StartupDelayException{

		String className="org.hanns.rl.discrete.ros.srp.config.QlambdaCoverageReward";
		String[] command = new String[]{className,
				"_"+QLambda.noInputsConf+":="+ noStateVars,
				"_"+QLambda.noOutputsConf+":="+noActions,
				"_"+QLambda.sampleCountConf+":="+noValues,
				"_"+QLambda.logPeriodConf+":="+logPeriod,
				"_"+QLambda.filterConf+":="+maxDelay};

		NodeGroup g = new NodeGroup("QLambdaASM", true);
		g.addNode(command, "QLambdaASM", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_QLambdaASM", g, SYNC);

		// create config IO
		module.createConfigEncoder(QLambda.topicAlpha,"float",(float)QLambda.DEF_ALPHA); 	// alpha config input, def. value is DEF_ALPHA
		module.createConfigEncoder(QLambda.topicGamma,"float",(float)QLambda.DEF_GAMMA);
		module.createConfigEncoder(QLambda.topicLambda,"float",(float)QLambda.DEF_LAMBDA);
		module.createEncoder(QLambda.topicImportance,"float",1);//					# default value is 0

		// QLambdaCoverageReward classname => float[]{prosperity, coverage, reward/step}
		module.createDecoder(QLambda.topicProsperity,"float", prospLen);			

		// create data IO
		module.createDecoder(QLambda.topicDataOut, "float", noActions);  	//# decode actions
		module.createEncoder(QLambda.topicDataIn, "float", noStateVars+1); 	//# encode states (first is reward)

		return module;
	}

	public static NeuralModule gridWorld(String name, int logPeriod, boolean logToFile,
			int[] size, int noActions, int[] agentPos) throws ConnectionException, StartupDelayException{
		String[] command = new String[]{
				"_logToFile:="+logToFile,//		Enables logging into file
				"_logPeriod:="+logPeriod,// How often to log?
				"_randomize:="+false,//		Should allow RANDOMIZED reset from Nengo?
				"_size:=["+size[0]+", "+size[1]+"]",//		List of two integers determining X, Y size of the map
				"_noActions:="+noActions,	//	Number of actions allowed by the agent (1ofN coded)
				"_agentPos:=["+agentPos[0]+", "+agentPos[1]+"]"};		//Two integers determining X, Y starting position of the agent
		//"_obstacles:="+[2, 5]};		//List (even no.) of coordinates (X1,Y1,X2,Y2..) of obstacles

		//noActions = 4;		# hardcoded
		//noStateVars = 2;

		NodeGroup g = new NodeGroup("GridWorld", true);
		g.addNode(command, "GridWorld", "java");
		NeuralModule module = new DefaultNeuralModule(name+"_GridWorld", g, SYNC);

		module.createEncoder(QLambda.topicDataOut, "float", noActions);  	//# decode actions
		module.createDecoder(QLambda.topicDataIn, "float", 2+1); 	//# encode states (first is reward)
		
		return module;
	}

}
