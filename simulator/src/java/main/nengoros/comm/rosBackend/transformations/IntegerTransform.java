package nengoros.comm.rosBackend.transformations;
/**
 * Template how to convert (array of) floats into integers and back.
 * 
 * @author Jaroslav Vitku
 *
 */
public interface IntegerTransform {
	
	public int[] float2int(float[] data);
	
	public float[] int2float(int[] data);
	
}
