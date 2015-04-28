package ctu.nengorosHeadless.simulator.impl;

import java.util.ArrayList;

import ca.nengo.model.SimulationException;
import ctu.nengoros.comm.rosutils.RosUtils;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.HeadlessNode;
import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;
import ctu.nengorosHeadless.network.modules.io.impl.BasicConnection;
import ctu.nengorosHeadless.simulator.Simulator;

public abstract class AbstractSimulator implements Simulator {

	protected float t;
	protected float dt;	// in seconds
	public static float DEF_DT = 1.0f;
	private boolean running = false;

	public static final int sleepTimeNs= 100;
	public static final int sleepInfoPeriod = 200;	// how often to print to console
	public static final long maxSleepTime = 50000;  // [ns/10], 50000 = 0.5s

	protected ArrayList<HeadlessNode> nodes;
	protected ArrayList<Connection> connections; 

	public boolean randomize = false;

	public AbstractSimulator(){
		this.t =0; 
		this.running = false;
		this.dt = DEF_DT;

		this.nodes = new ArrayList<HeadlessNode>();
		this.connections = new ArrayList<Connection>();
	}
	public boolean isRunning(){ return this.running; }

	@Override
	public void setDt(float step) {
		if(step<=0){
			System.err.println("step must be positive");
			return;
		}
		if(this.isRunning()){
			System.err.println("not allowed during simulation");
			return;
		}
		this.dt = step;
	}

	@Override
	public void reset(boolean randomize) {
		for(int i=0; i<nodes.size(); i++){
			nodes.get(i).reset(randomize);
		}
	}

	@Override
	public void prepareForSimulaiton(){
		if(!this.awaitAllStarted())
			return;
		this.reset(randomize);
	}
	
	@Override
	public void run(float from, float to) {

		this.running = true;
		this.prepareForSimulaiton();

		t = from;

		while(t<=to){
			this.makeStep();
			t += dt;
		}
		this.running = false;
	}
	
	public void makeStep(){
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx step no "+t+" stert");
		for(int i=0; i<connections.size(); i++){
			connections.get(i).transferData();
		}
		for(int i=0; i<nodes.size(); i++){
			try {
				// run all Origins/Encoders encode message and send to own modem
				nodes.get(i).run(t, t+dt);
			} catch (SimulationException e) {
				System.err.println("ERROR: Node named: "+nodes.get(i).getFullName() + "thrown simulatino exception!");
				e.printStackTrace();
			}
		}
		// wait for all responses
		this.awaitAllReady();
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx step no "+t+" end");
	}

	private void awaitAllReady(){
		int slept;
		int poc = 0;
		for(int i=0; i<nodes.size(); i++){
			slept = 0;
			while(!nodes.get(i).isReady()){
				try {
					if(poc>1 && poc % sleepInfoPeriod == 0){
						System.out.println("waiting for the node named: "+nodes.get(i).getFullName()+" for: "+slept/100+" ms");
					}
					Thread.sleep(0, sleepTimeNs);

					slept += sleepTimeNs;
					poc++;

					if(slept > maxSleepTime){
						System.err.println("ERROR: waited for the node named "+nodes.get(i).getFullName()
								+" for more than "+maxSleepTime/100+"ms, ignoring this step!");
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean awaitAllStarted(){
		for(int i=0; i<nodes.size(); i++){
			try {
				nodes.get(i).getStartupManager().awaitStarted();
			} catch (StartupDelayException e) {
				System.err.println("ERROR: Unable to wait for starting the Neural Module named "+nodes.get(i).getFullName());
				e.printStackTrace();
				System.err.println("---exiting the simultion");
				return false;
			}
		}
		return true;
	}

	@Override
	public abstract void defineNetwork();

	@Override
	public void cleanup(){
		for(int i=0; i<nodes.size(); i++){
			nodes.get(i).notifyAboutDeletion();
		}
		RosUtils.utilsShallStop();
	}

	@Override
	public Connection connect(Orig o, Term t){
		if(o==null){
			System.err.println("Orig o is null, ignoring this connection!");
			return null;
		}
		if(t==null){
			System.err.println("Term t is null, ignoring this connection!");
			return null;
		}
		Connection c = new BasicConnection(o,t);
		this.connections.add(c);
		return c;
	}

	@Override
	public void setLogToFile(boolean file) {
		// TODO :(
	}

}
