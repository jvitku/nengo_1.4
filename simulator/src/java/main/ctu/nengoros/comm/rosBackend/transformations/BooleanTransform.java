package ctu.nengoros.comm.rosBackend.transformations;
/**
 * Template how to convert (array of) floats into booleans and back.
 * 
 * @author Jaroslav Vitku
 *
 */

public interface BooleanTransform {

	public boolean float2bool(float data);
	
	public float bool2float(boolean data);
	
}
