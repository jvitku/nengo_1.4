package ctu.nengorosHeadless.simulator.test;

import java.util.LinkedList;

import org.hanns.physiology.statespace.motivationSource.Source;
import org.hanns.physiology.statespace.motivationSource.impl.BasicSource;
import org.hanns.physiology.statespace.observers.StateSpaceObserver;
import org.hanns.physiology.statespace.observers.StateSpaceProsperityObserver;
import org.hanns.physiology.statespace.observers.impl.ProsperityMSD;
import org.hanns.physiology.statespace.transformations.Transformation;
import org.hanns.physiology.statespace.transformations.impl.Sigmoid;
import org.hanns.physiology.statespace.variables.StateVariable;
import org.hanns.physiology.statespace.variables.impl.LinearDecay;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import ctu.nengoros.network.node.AbstractConfigurableHannsNode;
import ctu.nengoros.network.node.infrastructure.rosparam.impl.PrivateRosparam;
import ctu.nengoros.network.node.infrastructure.rosparam.manager.ParamList;
import ctu.nengoros.network.node.observer.Observer;
import ctu.nengoros.network.node.observer.stats.ProsperityObserver;
import ctu.nengoros.network.node.synchedStart.StartupManager;
import ctu.nengoros.util.SL;

public class MultiplierNode extends AbstractConfigurableHannsNode{
	
		public static final String NAME = "LinearPhysVar";
		public static final String me = "["+NAME+"] ";

		/**
		 * Node IO
		 */
		public static final String topicDataOut = io+"MotivationReward";
		public static final String topicDataIn  = io+"Reward";

		/**
		 * Variable configuration
		 */
		// the higher decay speed, the faster the reward needs to be received
		public static final String decayConf = "decaySpeed";
		public static final String topicDecay = conf+decayConf;
		public static final double DEF_DECAY = LinearDecay.DEF_DECAY;
		protected double decay;

		// number of inputs
		public static final int DEF_NOINPUTS = 1;
		protected int inputDims;

		/**
		 * HannsNode stuff
		 */
		protected StateSpaceProsperityObserver o;				// observes the prosperity of node
		protected LinkedList<Observer> observers;	// logging & visualization TODO

		/**
		 * Algorithm utilities
		 */
		protected int step = 0;
		protected StateVariable var;	// state variable implementation 
		protected Source source;		// motivation source
		protected Transformation t;		// transforms var->motivation
		
		protected String fullName = NAME;
		
		
		
		// value of reward that is published further after receiving a reward
		public static final float DEF_REWARD = BasicSource.DEF_REWARD;
		public static final String rewardConf = "rewardValue";
		public float rewardVal;

		// all values above this value are evaluated as receiving reward  
		public static final double DEF_REWTHRESHOLD = LinearDecay.DEF_THRESHOLD;
		public static final String rewardThrConf = "rewardThrValue";
		public double rewardThr;

		
		@Override
		public GraphName getDefaultNodeName() { return GraphName.of(NAME); }

		@Override
		public void onStart(ConnectedNode connectedNode) {
			log = connectedNode.getLog();
			//logger = new SL(this.get)

			System.out.println(me+"started, parsing parameters");
			this.registerParameters();
			paramList.printParams();

			this.parseParameters(connectedNode);

			System.out.println(me+"Creating data structures.");
			this.initStructures();
			
			System.out.println(me+"initializing ROS Node IO");

			this.registerObservers();
			this.registerSimulatorCommunication(connectedNode);
			super.buildProsperityPublisher(connectedNode);
			this.buildConfigSubscribers(connectedNode);
			this.buildDataIO(connectedNode);
			
			fullName = super.getFullName(connectedNode);
			System.out.println(me+"Node configured and ready now!");
			
		}

		
		@Override
		protected void buildConfigSubscribers(ConnectedNode connectedNode) {
			/**
			 * Decay
			 */
			Subscriber<std_msgs.Float32MultiArray> alphaSub = 
					connectedNode.newSubscriber(topicDecay, std_msgs.Float32MultiArray._TYPE);

			alphaSub.addMessageListener(new MessageListener<std_msgs.Float32MultiArray>() {
				@Override
				public void onNewMessage(std_msgs.Float32MultiArray message) {
					float[] data = message.getData();
					if(data.length != 1)
						log.error("Decay config: Received message has " +
								"unexpected length of"+data.length+"!");
					else{
						logParamChange("RECEIVED chage of value DECAY",
								var.getDecay(), data[0]);
						var.setDecay(data[0]);
					}
				}
			});
		}

		@Override
		protected void buildDataIO(ConnectedNode connectedNode){
			dataPublisher = connectedNode.newPublisher(topicDataOut, std_msgs.Float32MultiArray._TYPE);

			Subscriber<std_msgs.Float32MultiArray> dataSub = 
					connectedNode.newSubscriber(topicDataIn, std_msgs.Float32MultiArray._TYPE);

			dataSub.addMessageListener(new MessageListener<std_msgs.Float32MultiArray>() {
				@Override
				public void onNewMessage(std_msgs.Float32MultiArray message) {
					step++;
					float[] data = message.getData();

					if(data.length != inputDims)
						log.error(me+":"+topicDataIn+": Received state description has" +
								"unexpected length of"+data.length+"! Expected: "+topicDataIn);
					else{
						// here, the state description is decoded and one SARSA step executed
						if(step % logPeriod==0)
							System.out.println(me+"<-"+topicDataIn+" Received new reward data: "
									+SL.toStr(data)+"  step: "+step);
						// implement this
						onNewDataReceived(data);
					}
				}
			});
		}

		@Override
		public ProsperityObserver getProsperityObserver() {
			// TODO Auto-generated method stub
			System.err.println("ProsperityObserver is TODO");
			return null;
		}

		@Override
		protected void parseParameters(ConnectedNode connectedNode) {
			r = new PrivateRosparam(connectedNode);
			logToFile= r.getMyBoolean(logToFileConf, DEF_LTF);
			
			logPeriod = r.getMyInteger(logPeriodConf, DEF_LOGPERIOD);
			System.out.println("log period is!!! "+logPeriod+" def is: "+DEF_LOGPERIOD+" str: "+logPeriodConf);
			System.out.println(me+"parsing parameters");

			inputDims = r.getMyInteger(noInputsConf, DEF_NOINPUTS);
			decay = r.getMyDouble(decayConf, DEF_DECAY);
			
			double reward = r.getMyDouble(rewardConf, DEF_REWARD);
			rewardVal = (float)reward;
			rewardThr = r.getMyDouble(rewardThrConf, DEF_REWTHRESHOLD);
			
		}

		/**
		 * Initialize prosperity publisher
		 */
		
		@Override
		public void buildProsperityPublisher(ConnectedNode connectedNode){}

		/**
		 * By default, the prosperity vector should have length of 1 (ProsperityMSD)
		 */
		@Override
		public void publishProsperity(){

			float[] data;
			std_msgs.Float32MultiArray fl = prospPublisher.newMessage();

			if(o.getChilds() == null){
				data = new float[]{o.getProsperity()};
			}else{
				ProsperityObserver[] childs = o.getChilds();	
				data = new float[childs.length+1];
				data[0] = o.getProsperity();

				for(int i=0; i<childs.length; i++){
					data[i+1] = childs[i].getProsperity();
				}
			}
			fl.setData(data);
			prospPublisher.publish(fl);
		}

		@Override
		protected void registerParameters() {
			paramList = new ParamList();
			paramList.addParam(noInputsConf, ""+DEF_NOINPUTS,"Num dimensions of input data, input data is summed " +
					"and the value is evaluated as a reinforcement.");

			paramList.addParam(logToFileConf, ""+DEF_LTF, "Enables logging into file");
			paramList.addParam(logPeriodConf, ""+DEF_LOGPERIOD, "How often to log? -1 means never");

			paramList.addParam(decayConf, ""+DEF_DECAY, "Speed of decay of state variable each simulation step.");
			
			paramList.addParam(rewardConf, ""+DEF_REWARD, "Node publishes value of reward derivation, this is value of reward published");
			paramList.addParam(rewardThrConf, ""+DEF_REWTHRESHOLD, "If the sum of rewards on inputs is bigger than this threshold, " +
					"it is evaluated as reward");
		}

		@Override
		public String getFullName() { return this.fullName; }

		@Override
		public StartupManager getStartupManager() { return this.startup; }
		
		@Override
		public LinkedList<Observer> getObservers() { return this.observers; }
		

		protected void registerObservers() {
			observers = new LinkedList<Observer>();
			this.o = new ProsperityMSD(this.var);
			observers.add(o);
		}


		public void initStructures() {
			this.t = new Sigmoid();
			this.var = new LinearDecay(this.inputDims,this.decay, this.rewardThr);
			this.source = new BasicSource(var,t,this.rewardVal);
		}

		protected void onNewDataReceived(float[] data) {
			System.out.println("---- data: "+SL.toStr(data));
			this.source.makeStep(data);
			
			for(int i=0; i<observers.size(); i++)
				((StateSpaceObserver)observers.get(i)).observe();

			float rew = this.source.getReinforcement();
			float mot = this.source.getMotivation();

			System.out.println("\n----will publish rew: "+rew+ " motivation "+mot);
			
			if(step%logPeriod==0)
				log.info(me+"sending: "+SL.toStr(new float[]{rew,mot}));

			// publish the current reinforcement and motivation values
			std_msgs.Float32MultiArray fl = dataPublisher.newMessage();
			fl.setData(new float[]{rew,mot});
			dataPublisher.publish(fl);
			
			// publish current value of the prosperity
			this.publishProsperity();
		}

		@Override
		public boolean isStarted() {
			return (dataPublisher !=null && source!=null 
					&& t!=null && var!=null && source !=null);
		}


		@Override
		public float getProsperity() { return o.getProsperity(); }

		@Override
		public String listParams() { return this.paramList.listParams(); }

		@Override
		public void hardReset(boolean randomize) {
			this.step = 0;
			this.var.hardReset(randomize);
			this.source.hardReset(randomize);
			for(int i=0;i<observers.size(); i++)
				observers.get(i).hardReset(randomize);
		}

		@Override
		public void softReset(boolean randomize){
			this.step = 0;
			this.var.softReset(randomize);
			this.source.softReset(randomize);
			for(int i=0;i<observers.size(); i++)
				observers.get(i).softReset(randomize);
		}

	}

