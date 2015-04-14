package ctu.nengorosHeadless.network.modules.impl;

import ca.nengo.model.Origin;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.comm.rosBackend.backend.Backend;
import ctu.nengoros.comm.rosBackend.backend.BackendUtils;
import ctu.nengoros.comm.rosBackend.encoders.Encoder;
import ctu.nengoros.comm.rosBackend.encoders.impl.BasicEncoder;
import ctu.nengoros.dynamics.IdentityLTISystem;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.exceptions.MessageFormatException;
import ctu.nengoros.exceptions.UnsupportedMessageFormatExc;
import ctu.nengoros.model.termination.TransformTermination;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;
import ctu.nengorosHeadless.rosBackend.decoders.Decoder;
import ctu.nengorosHeadless.rosBackend.decoders.impl.BasicDec;

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

			ros = BackendUtils.select(topicName, dataType, /*dimensionSizes,*/ mc.getConnectedNode(), true);
			int dim = ros.gedNumOfDimensions();

			Encoder enc = new BasicEnc(this, topicName, dataType, mc, ros);
			this.addEncoder(enc);

		} catch (Exception e) {
			this.catchException(e);
		}
	}

	@Override
	public void createConfigEncoder(String topicName, String dataType, float defValue) {
		int dim;
		Backend ros;
		try {
			this.checkEncoderAvailable(topicName);
			
			int[] dimSizes = new int[]{defValue};

			ros = BackendUtils.select(topicName, dataType, dimSizes, mc.getConnectedNode(), true);
			dim = BackendUtils.countNengoDimension(dimSizes);

			Encoder enc = new BasicEncoder(this, dimSizes, topicName, dataType, mc, ros);
			// set default values for the first TransformTermination
			((TransformTermination)enc.getMultiTermination().getOrderedTerminations().get(0)).
			setDefaultOutputValues(defaultValues);

			this.addEncoder(enc);

		} catch (Exception e){
			this.catchException(e);
		}
	}
	



	/**
	 * Called by the Decoders, who add themselves.
	 * 
	 * @param o
	 * @throws StructuralException
	 */
	@Override
	public void addOrigin(Orig o) throws StructuralException {

		String name = o.getName();

		if(myOrigins.containsKey(name)){
			System.err.println("Origin with the same name already here, ignoring!");
			throw new StructuralException("Origin with this name already connected! "+name);
		}
		myOrigins.put(name, o);
		orderedOrigins.add(o);
	}

	/**
	 * Called by the Encoders, who are Terminations.
	 * @param t
	 * @throws StructuralException
	 */
	@Override
	public void addTermination(Term t) throws StructuralException {

		System.out.println(super.getFullName()+" adding termination named "+t.getName());
		String name = t.getName();

		if(myTerminations.containsKey(name)){
			System.err.println("Termination with this name already here, ignoring!");
			throw new StructuralException("Termination iwth this name already here, ignoring! " + name);
		}
		myTerminations.put(name,t);
		orderedTerminations.add(t);
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
		//catch (MessageFormatException e) {
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
