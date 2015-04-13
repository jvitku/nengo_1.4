package ctu.nengorosHeadless.network.modules.io.transformMultiTermination;

/**
 * <p>Serves as a factory for generating (default/initial) weights for transformation matrixes
 * for the {@link ctu.nengoros.model.termination.TransformTermination}. Particularly,
 * this is used in the 
 * {@link ctu.nengoros.model.transformMultiTermination.AbstractTransformMultiTermination}.</p>
 *  
 *  TODO: possibly implement equivalents of the Python 
 *  <a href="http://nengo.ca/docs/html/nef.Network.html#nef.Network.connect">NEF</a> 
 *  index_pre etc? 
 *  
 * @author Jaroslav Vitku
 *
 */
public interface WeightFactory {

	public static final float DEF_W = 1.0f;

	/**
	 * Identity matrix with default value of {@value #DEF_W}
	 * @param size size of square matrix
	 * @return identity matrix with specified size 
	 */
	public float[][] eye(int size);

	/**
	 * Identity matrix with custom weight
	 * @param size size of square matrix
	 * @param weight values on the diagonal
	 * @return identity matrix
	 */
	public float[][] eye(int size, float weight);

	/**
	 * Generate array of floats[inputDim,outputDim]
	 * @param inputDim input dimension (the first one)
	 * @param outputDim output dimension (the second one)
	 * @return array of zeros
	 */
	public float[][] zeros(int inputDim, int outputDim);
}
