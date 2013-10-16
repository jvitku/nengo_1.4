package ctu.nengoros.comm.rosBackend.transformations.impl;

import ctu.nengoros.comm.rosBackend.transformations.IntegerTransform;

/**
 * Convert between Integer and Float by simple casting and rounding..
 * 
 * @author Jaroslav Vitku
 *
 */
public class IntegerSimpleRounding implements IntegerTransform {

	@Override
	public int[] float2int(float[] data) {
		int[] d = new int[data.length];
		for( int i=0; i<data.length; i++)
			d[i] = Math.round(data[i]);
		return d;
	}

	@Override
	public float[] int2float(int[] data) {
		float[] d = new float[data.length];
		for( int i=0; i<data.length; i++)
			d[i] = (float)data[i];
		return d;
	}

}
