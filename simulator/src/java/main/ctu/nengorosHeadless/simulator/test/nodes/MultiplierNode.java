package ctu.nengorosHeadless.simulator.test.nodes;

import java.util.LinkedList;

import org.hanns.physiology.statespace.observers.StateSpaceProsperityObserver;
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
	
		public static final String NAME = "MultiplierNode";
		public static final String me = "["+NAME+"] ";

		/**
		 * Node IO
		 */
		public static final String topicDataOut = io+"MultipliedOUT";
		public static final String topicDataIn  = io+"MultipleIN";
		public static final String topicMultiplier = conf+"MultiplyBy";

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
		
		protected String fullName = NAME;

		
		private float mul = 2.0f;
		
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
					connectedNode.newSubscriber(topicMultiplier, std_msgs.Float32MultiArray._TYPE);

			alphaSub.addMessageListener(new MessageListener<std_msgs.Float32MultiArray>() {
				@Override
				public void onNewMessage(std_msgs.Float32MultiArray message) {
					float[] data = message.getData();
					if(data.length != 1)
						log.error("Multiplier config: Received message has " +
								"unexpected length of"+data.length+"!");
					else{
						logParamChange("RECEIVED chage of value MUL from to",
								mul, data[0]);
						mul = data[0];
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
						log.error(me+":"+topicDataIn+": Received data array has" +
								"unexpected length of"+data.length+"! Expected: "+inputDims);
					else{
						// here, the state description is decoded and one SARSA step executed
						if(step % logPeriod==0)
							System.out.println(me+"<-"+topicDataIn+" Received new input data: "
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
			
			System.out.println(me+"parsing parameters");

			inputDims = r.getMyInteger(noInputsConf, DEF_NOINPUTS);
			
			System.out.println("PARSED: log period is "+logPeriod+" def is: "+DEF_LOGPERIOD+" str: "+logPeriodConf+
					" no of diimensions is "+inputDims);
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
/*
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
			*/
		}

		@Override
		protected void registerParameters() {
			paramList = new ParamList();
			paramList.addParam(noInputsConf, ""+DEF_NOINPUTS,"Num dimensions of input data, input data is summed " +
					"and the value is evaluated as a reinforcement.");

			paramList.addParam(logToFileConf, ""+DEF_LTF, "Enables logging into file");
			paramList.addParam(logPeriodConf, ""+DEF_LOGPERIOD, "How often to log? -1 means never");

		}

		@Override
		public String getFullName() { return this.fullName; }

		@Override
		public StartupManager getStartupManager() { return this.startup; }
		
		@Override
		public LinkedList<Observer> getObservers() { return this.observers; }
		

		protected void registerObservers() {
		}

		protected void onNewDataReceived(float[] data) {
			System.out.println("\n---- data: "+SL.toStr(data));

			/*
			for(int i=0; i<observers.size(); i++)
				((StateSpaceObserver)observers.get(i)).observe();
*/
			float[] dataOut = new float[data.length];
			for(int i=0; i<dataOut.length; i++){
				//dataOut[i] = data[i]*mul + 1f;
				dataOut[i] = data[i]*mul;
			}
			
			System.out.println("----will publish data: "+SL.toStr(data)+ " multiplied by "+mul+" that is: "+SL.toStr(dataOut));

			// publish the current reinforcement and motivation values
			std_msgs.Float32MultiArray fl = dataPublisher.newMessage();
			fl.setData(dataOut);
			dataPublisher.publish(fl);
			
			// publish current value of the prosperity
			this.publishProsperity();
		}

		@Override
		public boolean isStarted() {
			return (dataPublisher !=null);
		}


		@Override
		public float getProsperity() { return o.getProsperity(); }

		@Override
		public String listParams() { return this.paramList.listParams(); }

		@Override
		public void hardReset(boolean randomize) {
			this.step = 0;
			/*
			for(int i=0;i<observers.size(); i++)
				observers.get(i).hardReset(randomize);
				*/
		}

		@Override
		public void softReset(boolean randomize){
			this.step = 0;
			/*
			for(int i=0;i<observers.size(); i++)
				observers.get(i).softReset(randomize);*/
		}

	}

