package service.simulator;

/**
 * Is small tester for some generic simulator. Serves fo thesting how the simulator can be
 * turned on/off.
 * 
 * 
 * @author Jaroslav Vitku
 *
 */
public class SimulatorX implements Simulator, Runnable{

	Thread thread;
	private final SimulatorController sc;
	int poc;
	private final String me;
	public SimulatorX(String name, SimulatorController sc){
		me = name;
		poc = 0;
		this.sc = sc;
	}
	
	
	@Override
	public void startSimulation() {
		this.start();
	}
	
	@Override
	public void stopSimulation() {
		try {
			thread.join();
			System.out.println(me+"Simulation stopped OK");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * wait some time (loading..)
	 */
	@Override
	public void init() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * spawn new simulation thread
	 */
	public void start(){
		thread = new Thread (this);
		thread.start();
	}
	
	@Override
	public void run() {

		while(sc.isRunning()){
			try {
				Thread.sleep(1000);
				System.out.println(me+"Simulation step no."+poc++);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(me+"SimulatorControls say should stop, exiting...");
	}

	@Override
	public SimulatorController getController() {
		return sc;
	}

	@Override
	public boolean loadMap(String pathToMap) {
		
		return false;
	}
	
	
}
