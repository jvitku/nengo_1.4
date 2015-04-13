package ctu.nengorosHeadless.network.modules.ioTmp;

import ca.nengo.model.StructuralException;

/**
 * <p>Compared to the {@link ca.nengo.model.Termination}, this one implements 
 * also transformation of signal. The dimensionality of input signal is 
 * converted to another one, expected by its parent. The transformation 
 * is defined by the weight matrix. This means that compared to the 
 * {@link ca.nengo.model.PlasticNodeTermination}, the weight matrix is 2D here.</p> 
 * 
 * <p>This Termination provides opportunity to connect two modules of 
 * arbitrary dimensionality, the transformation matrix should be provided.</p>
 * 
 * <p>Note that for compatibility with the Nengo, the {@link #getDimensions()} method
 * provides dimensionality of input to the termination.</p> 
 *  
 * @author Jaroslav Vitku
 *
 */
public interface TransformTermination extends Terminaiton{
	
	/**
	 * The TransformTermination has input and output dimensions. The inputDimensions
	 * specify dimensionality of input to the Termination, this is provided by the
	 * {@link #getDimensions()}. The outputDimensions define dimensionality of 
	 * output of the Termination, this has to be compatible with its parent Node. 
	 * @return output dimensionality of this Termination, has to be compatible
	 * with the parent nodes dimensionality.
	 */
	public int getOutputDimensions();
	
	/**
	 * Returns the transformation matrix used to convert signal from input to output
	 * on this Termination.
	 * @return weight matrix which defines the transformation
	 */
	public float[][] getTransformationMatrix();

	/**
	 * Sets the Transformation matrix, which defines transformation of signal on
	 * input to the output signal, possible with different dimensionality.  
	 * @param matrix weight matrix
	 * @throws StructuralException thrown in case that the matrix dimensions are incorrect
	 */
	public void setTransformationMatrix(float[][] matrix) throws StructuralException;
	
	/**
	 * If no input is received, these default values are placed on output of this 
	 * TransformTermination.
	 * @param defaultValues array of default values of type float to be returned by the 
	 * getOutput method if no input received
	 * @throws StructuralException thrown if the dimensionality of defaultValues is different
	 * from the dimensionality of the output of this TransformTermination
	 */
	public void setDefaultOutputValues(float[] defaultValues) throws StructuralException;

	/**
	 * If the Encoder has only one TransformTermination, the defaultOutputValue can be set
	 * to non-zero. But during adding of new TranformTermination to the parent MultiTermination,
	 * the default values should be set back to zero.  
	 */
	public void resetDefaultOutputValues();
}
