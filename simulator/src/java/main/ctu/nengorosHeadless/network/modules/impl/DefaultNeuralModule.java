package ctu.nengorosHeadless.network.modules.impl;

import ca.nengo.model.StructuralException;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.BackendUtils;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.exceptions.MessageFormatException;
import ctu.nengoros.exceptions.UnsupportedMessageFormatExc;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.rosBackend.decoders.Decoder;
import ctu.nengorosHeadless.rosBackend.decoders.impl.BasicDec;
import ctu.nengorosHeadless.rosBackend.encoders.Encoder;
import ctu.nengorosHeadless.rosBackend.encoders.impl.BasicEnc;

public class DefaultNeuralModule extends NeuralModule{

	public DefaultNeuralModule(String name, NodeGroup group, boolean synchronous)
			throws ConnectionException, StartupDelayException{

		super(name, group, synchronous );	
	}

	@Override
	public void createDecoder(String topicName, String dataType, int dimensionSize){
		try {
			Backend ros = BackendUtils.select(topicName, dataType, new int[]{dimensionSize}, mc.getConnectedNode(), false);
			
			// make decoder synchronous or not (always ready)
			Decoder d = new BasicDec(this, topicName, dataType, dimensionSize, mc, ros, synchronous);
			
			// register as child (will or will not block the simulation if is not set to be synchronous)
			super.addChild(d);
			//this.addOrigin((Orig)d);

		} catch (Exception e) {
			this.catchException(e);
		}
	}

	/**
	 * Create encoder where the dimensionality of message data is determined by data type (e.g. turtlesim/Velocity=2)
	 */
	@Override
	public void createEncoder(String topicName, String dataType, int dimensionSize) {
		Backend ros;
		try {
			this.checkEncoderAvailable(topicName);

			ros = BackendUtils.select(topicName, dataType, new int[]{dimensionSize}, mc.getConnectedNode(), true);
			
			Encoder enc = new BasicEnc(this, topicName, dataType, mc, ros);
			//this.addEncoder(enc);
		} catch (Exception e) {
			this.catchException(e);
		}
	}

	@Override
	public void createConfigEncoder(String topicName, String dataType, float defValue) {
		//int dim;
		Backend ros;
		try {
			this.checkEncoderAvailable(topicName);
			
			ros = BackendUtils.select(topicName, dataType, new int[]{1}, mc.getConnectedNode(), true);
			//dim = BackendUtils.countNengoDimension(dimSizes);

			//Encoder enc = new BasicEnc(this, new int[]{1}, topicName, dataType, mc, ros, defValue);
			Encoder enc = new BasicEnc(this, topicName, dataType, mc, ros, defValue);
			// set default values for the first TransformTermination
			//((TransformTermination)enc.getMultiTermination().getOrderedTerminations().get(0)).
			//setDefaultOutputValues(defaultValues);

			//this.addEncoder(enc);

		} catch (Exception e){
			this.catchException(e);
		}
	}

	/**
	 * Check whether an encoder to the same topicName is not already registered here.
	 * @param topicName name of the ROS topic and name of the Encoder (base name for its Terminations). 
	 * @throws StructuralException if a Encoder with given name already registered here
	 */
	private void checkEncoderAvailable(String topicName) throws StructuralException{
		if(this.myEncoders.containsKey(topicName))
			throw new StructuralException(super.getFullName()+" Encoder to the requested topic "+topicName+
					" alredy registered to this NeuralModule!");
	}

	private void catchException(Exception e){
		if(e instanceof MessageFormatException){
			System.err.println(super.getFullName()+" Bad message format.");
			e.printStackTrace();
		}else if(e instanceof UnsupportedMessageFormatExc) {
			System.err.println(super.getFullName()+" Message format is not supported so far");
			e.printStackTrace();
		}else if(e instanceof StructuralException) {
			System.err.println(super.getFullName()+" Could not add the corresponding termination to Nengo network");
			e.printStackTrace();
		}else if(e instanceof ConnectionException) {
			System.err.println(super.getFullName()+" my modem was not connected. Probably ROS communication error!!");
			e.printStackTrace();
		}else if(e instanceof StartupDelayException) {
			System.err.println(super.getFullName()+" my modem was not started in a given time. Probably ROS communication error!");
			e.printStackTrace();
		}else{
			e.printStackTrace();
		}
	}

}
