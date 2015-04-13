package ctu.nengorosHeadless.network.modules.impl;

import ctu.nengoros.comm.nodeFactory.NodeGroup;
import ctu.nengoros.exceptions.ConnectionException;
import ctu.nengoros.network.common.exceptions.StartupDelayException;
import ctu.nengorosHeadless.network.modules.NeuralModule;

public class DefaultNeuralModule extends NeuralModule{

	public DefaultNeuralModule(String name, NodeGroup group, boolean synchronous)
			throws ConnectionException, StartupDelayException{
		
		super(name, group, synchronous );	
	}

	@Override
	public void createDecoder(String topicName, String dataType, int dimensionSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createEncoder(String topicName, String dataType, int dimensionSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createConfigEncoder(String topicName, String dataType, float defValue) {
		// TODO Auto-generated method stub
		
	}
	
	
}
