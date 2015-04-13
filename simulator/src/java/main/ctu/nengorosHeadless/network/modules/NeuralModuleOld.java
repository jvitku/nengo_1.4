package ctu.nengorosHeadless.network.modules;

import ca.nengo.model.StructuralException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengoros.network.node.synchedStart.impl.StartedObject;
import ctu.nengorosHeadless.network.modules.ioTmp.MultiTermination;
import ctu.nengorosHeadless.network.modules.ioTmp.Terminaiton;

public interface NeuralModuleOld extends HeadlessNode, StartedObject{

	/**
	 * NeuralModule uses asynchronously launched {@link ctu.nengoros.comm.nodeFactory.modem.Modem}
	 * and other ROS nodes. Therefore it should be able to indicate that the crucial components
	 * are ready to operate.
	 * @throws StartupDelayException thrown if the maximum startup time is exceeded
	 * @see ctu.nengoros.network.node.synchedStart.impl.SyncedStart
	 */
	public void awaitStarted() throws StartupDelayException; 
	
	/**
	 * Set whether to reset also own ROS nodes. The resetting technique
	 * is determined by the particular {@link ctu.nengoros.comm.nodeFactory.modem.Modem} used.
	 * 
	 * @param shouldReset if true, the Module call reset also for own modem, 
	 * which should handle the resetting
	 * 
	 * @see ctu.nengoros.comm.nodeFactory.modem.impl.RosResettingModem#reset(boolean)
	 */
	public void setShouldResetNodes(boolean shouldReset);
	
	/**
	 * <p>This NeuralModule supports connecting multiple "inputs" to one Encoder.
	 * It is accomplished by the fact that each Encoder has MultiTermination, where
	 * the MultiTermination can have several own Terminations, whose values are
	 * combined into one value. </p> 
	 * 
	 * <p>The user is allowed to create new Terminations for Encoder. In order to 
	 * do this, the Encoders MultiTermination has to be available. The MultiTermination
	 * is generated with identical name as the Encoder. This method provides access
	 * to the Encoders MultiTermination by its name.</p>
	 * 
	 * @param name name of the Encoder==MultiTermination
	 * @return MultiTermination if found
	 * @throws StructuralException if no Encoder of such name found
	 */
	public MultiTermination getMultiTermination(String name) throws StructuralException;
	
	public Terminaiton newTerminationFor(String name) throws StructuralException;
	public Terminaiton newTerminationFor(String name, float weight) throws StructuralException;
	public Terminaiton newTerminationFor(String name, float[][] weights) throws StructuralException;
	
	public void setSynchronous(boolean synchronous);

	public void createDecoder(String topicName, String dataType, int[] dimensionSizes);
	public void createDecoder(String topicName, String dataType, int dimensionSize);
	public void createDecoder(String topicName, String dataType);

	public void createEncoder(String topicName, String dataType, int[] dimensionSizes);
	public void createEncoder(String topicName, String dataType, int dimensionSize);
	public void createEncoder(String topicName, String dataType);

	
	public void createConfigEncoder(String topicName, String dataType, float[] defaultValues);
	public void createConfigEncoder(String topicName, String dataType, float defaultValue);
	
	public void createDecoder(String topicName, String dataType, int[] dimensionSizes, boolean synchronous);
	public void createDecoder(String topicName, String dataType, int dimensionSize, boolean synchronous);
	public void createDecoder(String topicName, String dataType, boolean synchronous);

	
}
