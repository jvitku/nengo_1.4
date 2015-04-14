package ctu.nengoros.comm.rosBackend.encoders;

import ctu.nengoros.model.transformMultiTermination.MultiTermination;
import ctu.nengoros.modules.NeuralModule;
import ca.nengo.model.StructuralException;

/**
 * <p>Each Encoder is allowed to register one or more own Nengo Terminations.
 * Encoder registers own Termination to own parent (NeuralModule). This means
 * that NeuralModule is responsible for correct order of running the components:
 * first run all the registered Terminations, then run all Encoders.</p>
 * 
 * <p>Encoder should:
 * <ul>
 * 	<li>Register new ROS message publisher in the constructor. Here, the ROS 
 * topic on which are messages published equals to the name of Encoder.</li>.
 * 
 * 	<li>In each call of method run() collects input values on all own Terminations,
 * encodes the result into the ROS message and publishes on the ROS topic.</li>
 * </ul>
 * </p>
 *  
 * 
 * @author Jaroslav Vitku
 *
 */
public interface Encoder extends CommonEncoder{

	
	/**
	 * Get the parent of Encoder
	 * @return my parent to whom I register my Terminations 
	 */
	NeuralModule getParent();
	
	/**
	 * Return own MultiTermination, this allows user to add new Terminations
	 * to it.
	 * @return MultiTermination if found
	 */
	public MultiTermination getMultiTermination() throws StructuralException;
}
