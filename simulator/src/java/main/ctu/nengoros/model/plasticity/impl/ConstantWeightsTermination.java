package ctu.nengoros.model.plasticity.impl;

import ctu.nengoros.model.plasticity.AbstractPlasticTermination;

/**
 * <p>This implementation of the {@link ca.nengo.model.PlasticTermination} represents
 * an equivalent to the BasicTermination for Neural Ensembles. </p>
 * 
 * <p>Here, no modification of connection weights occurs.</p> 
 *   
 * @author Jaroslav Vitku
 *
 */
public class ConstantWeightsTermination extends AbstractPlasticTermination{

	private static final long serialVersionUID = 2589578065949657511L;

	public ConstantWeightsTermination(String name, int[] dimensionSizes) {
		super(name, dimensionSizes);
		// TODO Auto-generated constructor stub
	}
	

}
