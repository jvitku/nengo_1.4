package ctu.nengorosHeadless.network.connections.impl;

import ca.nengo.model.StructuralException;
import ctu.nengorosHeadless.network.connections.InterLayerConnection;
import ctu.nengorosHeadless.network.connections.InterLayerWeights;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;

public class BasicInterLayerConnection extends BasicConnection implements InterLayerConnection{

	//private final int sourceIndex, targetIndex;
	private final IOGroup sourceGroup, targetGroup;
	
	private final InterLayerWeights interLayer;
	public BasicInterLayerConnection(Orig source, Term target, InterLayerWeights interLayer) {
		super(source, target);

		this.interLayer = interLayer;
		
		IOGroup[] out = this.interLayer.addConnection(source.getSize(), target.getSize());
		this.sourceGroup = out[0];
		this.targetGroup = out[1];
	}

	@Override
	public float[][] getWeights() {
		try {
			return this.interLayer.getWeightsBetween(sourceGroup.getMyIndex(), targetGroup.getMyIndex());
		} catch (StructuralException e) {
			System.err.println("BasicInterLayerConnection: indexes out of range!!");
			return null;
		}
	}

	@Override
	public void setWeights(float[][] w) throws StructuralException {
		//this.interLayer.setWeightsBetween(sourceIndex, targetIndex, w);
		this.interLayer.setWeightsBetween(sourceGroup.getMyIndex(), targetGroup.getMyIndex(), w);
	}
}
