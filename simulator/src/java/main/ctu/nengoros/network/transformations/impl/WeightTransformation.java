package ctu.nengoros.network.transformations.impl;

import ca.nengo.model.StructuralException;
import ctu.nengoros.network.transformations.Transformation;

/**
 * The simplest implementation of the {@link ctu.nengoros.network.transformations.Transformation}.
 * Defines just an unchanged single weight matrix.
 *  
 * @author Jaroslav Vitku
 *
 */
public class WeightTransformation implements Transformation{

	private float[][] weights;

	public WeightTransformation(float[][] weights){
		this.weights = weights.clone();
	}

	@Override
	public int getInputDimension() { return this.weights.length; }

	@Override
	public int getOutputDimension() { return this.weights[0].length; }

	@Override
	public float[][] getWeights() { return this.weights; }

	@Override
	public void setWeights(float[][] weights) throws StructuralException{
		if(weights.length!=this.weights.length ||
				weights[0].length!=this.weights[0].length){
			throw new StructuralException("Cannot set weights, expected dimensions are: ["
					+this.weights.length+","+this.weights[0].length+"]");
		}
		System.err.println("ERROR: WeightTransformation: could not change the weight matrix!");
	}

	@Override
	public void reset(boolean randomize) {}
}
