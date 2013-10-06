package nengoros.comm.rosBackend.encoders;


import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Termination;

/**
 * each Enoder is associated with it Nengo Origin, it does:
 * 	-in constructor:	
 * 		-registers new ROS message publisher (topic equals to its name)
 * 
 * 	-on each call of run() method collects input values
 * 		-in specified intervals creates and sends ROS mesasges 		
 * 
 * 
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Encoder extends Termination, Resettable{

	String getName();
	
	
	/**
	 * run encoder for a given time that is: 
	 * 	-generate values based on actual myValues
	 * 
	 * @param startTime
	 * @param endTime
	 */
	public void run(float startTime, float endTime) throws SimulationException;
	
	
}
