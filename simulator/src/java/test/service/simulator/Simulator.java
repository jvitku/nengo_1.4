package service.simulator;

public interface Simulator {
	
	public void startSimulation();
	
	public void stopSimulation();
	
	public boolean loadMap(String pathToMap);
	
	public void init();
	
	public SimulatorController getController();
}
