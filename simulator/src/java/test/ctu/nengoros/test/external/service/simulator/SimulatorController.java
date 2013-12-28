package ctu.nengoros.test.external.service.simulator;
/**
 * 
 * Controls the state of the simulator. 
 * This class is mainly for testing how the simulator can 
 * be controlled over the ROS network.
 * 
 * @author Jaroslav Vitku
 */
public class SimulatorController {

	private final String me="SimulatorControls ";
	private volatile boolean running = false;
	private volatile boolean inited = false;
	private Simulator mySim;
	
	public SimulatorController(){
	}

	public synchronized void setMySimulator(Simulator s){
		mySim = s;
	}
	
	public boolean isRunning() { return running; }

	public synchronized void start() {
		if(!inited){
			System.err.println(me+"I am not initialized");
			return;
		}
		if(running){
			System.err.println(me+"I am already running");
			return;
		}
		running = true;
		System.out.println(me+"OK, starting simulation");
		mySim.startSimulation();
	}
	
	public synchronized void stop(){
		if(!running){
			System.err.println(me+"Simulator already stopped");
			return;
		}
		running = false;
		mySim.stopSimulation();
	}

	public boolean isInited() {	return inited; }

	public synchronized void init() {
		mySim.init();		
		this.inited = true;
	}
}
