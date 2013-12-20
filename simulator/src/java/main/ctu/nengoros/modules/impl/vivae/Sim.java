package ctu.nengoros.modules.impl.vivae;

/**
 * Class for storing commands and service names that control the simulation.
 * 
 * The commands are accepted by the SimControlServiceResponseBuilder.
 * 
 * Note: this class is copied from the ViVae project to avoid circular dependencies, 
 * TODO solve this by means of some plugin?
 *
 * @author Jaroslav Vitku
 *
 */
public class Sim {

	public static class Cmd{
		public static final String [] COMMANDS = new String[]{
			"init", 
			"start",
			"stop", 
			"reset",
			"destroy",
			"setvisible",
		"setinvisible"};

		public static final String INIT = COMMANDS[0];
		public static final String START = COMMANDS[1];
		public static final String STOP = COMMANDS[2];
		public static final String RESET = COMMANDS[3];
		public static final String DESTROY = COMMANDS[4];
		public static final String SETVISIBLE = COMMANDS[5];
		public static final String SETINVISIBLE = COMMANDS[6];

		public static String getAll(){
			String out = COMMANDS[0];
			for(int i=1; i<COMMANDS.length; i++)
				out = out +"\n"+COMMANDS[i];
			return out;
		}
	}
	
	public static class Msg{

		// ROS topic names of provided services
		public static final java.lang.String SPAWN = "spawnService";
		public static final java.lang.String CONTROL = "simControlSerice";
		public static final java.lang.String LOAD = "loadMapSerice";

		// Topic names for commmunication?
		public static final java.lang.String V2N = "vivae2nengo";
		public static final java.lang.String N2V = "nengo2vivae";
		
	}
	
	public static class Maps{
		
		// default map used for testing
		public static String DEFAULT = "data/scenarios/arena1.svg"; 
		
		// just some selected names of arenas that are available
		public static final String[] names = new String[]{
			"data/scenarios/arena1.svg",
			"data/scenarios/arena2.svg",
			"data/scenarios/ushape.svg" };

	}
}
