package ctu.nengorosHeadless.simulator.impl;

import ctu.nengorosHeadless.simulator.Simulator;

public abstract class AbstractSimulator implements Simulator {

	protected int step;
	protected float dt;	// in seconds
	public static float DEF_DT = 1.0f;
	private boolean running = false;
	
	public AbstractSimulator(){
		this.step =0; 
		this.running = false;
		this.dt = DEF_DT;
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
		// TODO reset all my nodes
	}

	@Override
	public void simulate(float from, float to) {
		// while dt*step < to, simulate all nodes
	}

	@Override
	public void defineNetwork() {
		// TODO override this
		
	}

	@Override
	public void setLogToFile(boolean file) {
		// TODO :(
	}

}
