package ctu.nengorosHeadless.simulator.impl;

import java.util.ArrayList;

import ca.nengo.model.SimulationException;

import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.HeadlessNode;
import ctu.nengorosHeadless.network.modules.io.Connection;
import ctu.nengorosHeadless.simulator.Simulator;

public abstract class AbstractSimulator implements Simulator {

	protected float t;
	protected float dt;	// in seconds
	public static float DEF_DT = 1.0f;
	private boolean running = false;
	
	public static final int sleeptime = 10;	// how long to sleep between waiting for nodes to be ready
	public static final int maxSleepCycles = 200; 	// how many times to sleep

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

	@Override
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
	public void run(float from, float to) {

		if(!this.awaitAllStarted())
			return;
		this.reset(randomize);
		
		t = from;
		
		while(t<=to){
			for(int i=0; i<connections.size(); i++){
				connections.get(i).transferData();
			}
			for(int i=0; i<nodes.size(); i++){
				try {
					nodes.get(i).run(t, t+dt);
				} catch (SimulationException e) {
					System.err.println("ERROR: Node named: "+nodes.get(i).getFullName() + "thrown simulatino exception!");
					e.printStackTrace();
				}
			}
			this.awaitAllReady();
			t += dt;
		}
	}
	
	private void awaitAllReady(){
		int slept;
		for(int i=0; i<nodes.size(); i++){
			slept = 0;
			while(!nodes.get(i).isReady()){
				try {
					Thread.sleep(sleeptime);
					if(sleeptime*slept++ > maxSleepCycles){
						System.err.println("ERROR: waited for the node named "+nodes.get(i).getFullName()
								+"for more than "+sleeptime*slept+"ms, ignoring this step!");
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
	}
	
	@Override
	public void setLogToFile(boolean file) {
		// TODO :(
	}

}
