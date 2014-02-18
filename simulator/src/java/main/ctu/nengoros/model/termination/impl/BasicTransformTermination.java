package ctu.nengoros.model.termination.impl;

import ctu.nengoros.model.termination.TransformTermination;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.Node;
import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SpikeOutput;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.BasicTermination;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * <p>Common case of TransformTermination, which uses BasicTermination for 
 * implementing the Nengo communication, but adds also the transformation matrix.</p>
 * 
 * <p>Note that the signal is ran through the {@link ca.nengo.dynamics.Integrator} and 
 * {@link ca.nengo.dynamics.DynamicalSystem}, and then the transformation matrix is applied
 * to the resulting signal. This should be more compatible with NEF, since the input spikes
 * are not divided by the weight matrix before applying dynamics.</p>  
 *  
 * @author Jaroslav Vitku
 *
 */
public class BasicTransformTermination extends BasicTermination implements TransformTermination{

	/**
	 * The default value to be placed on output if no input is received. 
	 * @see #setDefaultOutputValues(float[]) 	
	 */
	public static final float DEF_OUTVAL = 0f;
	private final float[] defOutputValues;

	private static final long serialVersionUID = -9130566431178179544L;

	protected final int inputDimension;
	protected final int outputDimension;

	protected float[][] matrix;

	/**
	 * Here, the input dimension is determined by the passed DynamicalSystem, the 
	 * output dimension is determined by the parameter.
	 * 
	 * @param node parent
	 * @param dynamics dynamics defining dynamics of the Termination and its input dimension
	 * @param integrator integrating the input signal
	 * @param outputDimension dimensionality of output of this Termination, the signal dimension
	 * that is expected by the parent node
	 * @param name name of this Termination
	 * @param weights weight matrix if size [{@link #getDimensions()},{@link #getOutputDimensions()}]
	 */
	public BasicTransformTermination(Node node, DynamicalSystem dynamics, 
			Integrator integrator, String name, float[][] weights) throws
			StructuralException{
		super(node, dynamics, integrator, name);

		this.inputDimension = dynamics.getInputDimension();
		this.outputDimension = weights[0].length;

		if(weights.length != this.inputDimension || weights[0].length != this.outputDimension)
			throw new StructuralException("BasicTransformTermination: wrong size of transformation"
					+ "matrix! Expected dimensions are: ["+this.inputDimension+","+
					this.outputDimension+"]");

		this.defOutputValues = new float[this.outputDimension];
		for(int i=0; i>this.outputDimension; i++)
			this.defOutputValues[i] = DEF_OUTVAL;

		this.matrix = weights.clone();
	}

	@Override
	public int getDimensions(){ return this.inputDimension; }

	@Override
	public int getOutputDimensions() { return this.outputDimension; }

	@Override
	public float[][] getTransformationMatrix() { return this.matrix; }

	@Override
	public void setTransformationMatrix(float[][] matrix) throws StructuralException {

		if(matrix.length ==0 || matrix.length!=this.getDimensions()){
			throw new StructuralException("BasicTransformTermination: ERROR: "+
					"wrong size of input dimension, expected first dim size is: "+
					this.getDimensions());
		}
		if(matrix[0].length ==0 || matrix[0].length!=this.getOutputDimensions())
			throw new StructuralException("BasicTransformTermination: ERROR: "+
					"wrong size of output dimension, expected second dim size is: "+
					this.getOutputDimensions());

		this.matrix = matrix.clone();
	}

	/**
	 * First, the values on input are collected and dynamics is applied. 
	 * Then the transformation is applied and values are stored.
	 */
	@Override
	public void run(float startTime, float endTime) throws SimulationException {

		// the same processing as in the BasicTermination
		float[] input = this.notConnectedInput;

		if (myInput instanceof RealOutput) {
			input = ((RealOutput) myInput).getValues();

		} else if (myInput instanceof SpikeOutput) {
			boolean[] spikes = ((SpikeOutput) myInput).getValues();
			input = new float[spikes.length];
			float amplitude = 1f / (endTime - startTime);
			for (int i = 0; i < spikes.length; i++) {
				if (spikes[i]) {
					input[i] = amplitude;
				}
			}
		}else{
			// if the Termination is not connected no values are received, so 
			// set default output values to my output and return
			myOutput = this.initDefaultValues(startTime, endTime);
			return;
		}
		// here, compute dynamics as the BasicTermination does
		TimeSeries inSeries = new TimeSeriesImpl(new float[]{startTime, endTime}, 
				new float[][]{input, input}, Units.uniform(Units.UNK, input.length));
		TimeSeries terminationInput = myIntegrator.integrate(myDynamics, inSeries);
		myOutput = this.applyTransformation(startTime, endTime, terminationInput);
	}

	/**
	 * Apply transformation to the TimeSeries which was obtained in the same way as is
	 * in the {@link ca.nengo.model.impl.BasicTermination}. 
	 * @param startTime starting time of the series
	 * @param endTime ending time of the series
	 * @param t TimeSeries on the Termination input with applied dynamics
	 * @return transformed time series to the dimensionality of {@link #getOutputDimensions()} 
	 */
	private TimeSeries applyTransformation(float startTime, float endTime, TimeSeries t) 
			throws SimulationException {
		TimeSeriesImpl ts = (TimeSeriesImpl)t;
		// get array of values[timeSamples][dimensionVals]
		float[][] values = ts.getValues();

		if(values[0].length!=this.inputDimension){
			throw new SimulationException("Wrong dimension of input TimeSeries! "
					+ "Input dimension of this Termination is "+this.inputDimension);
		}
		// get blank TimeSeries with appropriate dimensionality
		TimeSeriesImpl output = this.initZeros(startTime, endTime, this.outputDimension);

		// for all time samples
		for(int i=0; i<values.length; i++){
			// for all output dimensions
			for(int j=0; j<this.outputDimension; j++){
				// compute weighted sum of all input values for this output value
				float vv = 0;
				for(int k=0; k<this.inputDimension; k++){

					vv = vv+ values[i][k] * matrix[k][j];
				}
				output.getValues()[i][j] = vv;
			}
		}
		return output;
	}

	/**
	 * Return time series with current simulation times and with values values set to default
	 * 
	 * @param startTime start time of simulation step
	 * @param endTime end times
	 * @return initialized TimeSeriesImpl with zero data and correct getTimes() values
	 */
	private TimeSeriesImpl initZeros(float startTime, float endTime, int dimensions){

		float[] input = new float[dimensions];
		for(int i=0; i<dimensions; i++)
			input[i] = 0;

		float[] inputII = input.clone();

		TimeSeriesImpl inSeries = 
				new TimeSeriesImpl(
						new float[]{startTime, endTime}, 
						new float[][]{input, inputII}, 
						Units.uniform(Units.UNK, input.length));

		return inSeries;
	}

	/**
	 * Create time series with default values set in the {@link #DEF_OUTVAL} or 
	 * by the method {@link #setDefaultOutputValues(float[])}
	 * @param startTime
	 * @param endTime
	 * @return timeSeries which contains default values
	 */
	private TimeSeriesImpl initDefaultValues(float startTime, float endTime){

		float[] input = new float[this.outputDimension];
		for(int i=0; i<this.outputDimension; i++)
			input[i] = this.defOutputValues[i];

		float[] inputII = input.clone();

		TimeSeriesImpl inSeries = 
				new TimeSeriesImpl(
						new float[]{startTime, endTime}, 
						new float[][]{input, inputII}, 
						Units.uniform(Units.UNK, input.length));

		return inSeries;
	}

	@Override
	public void setDefaultOutputValues(float[] defaultValues) throws StructuralException {
		if(this.getOutputDimensions() != defaultValues.length){
			throw new StructuralException("BasicTransformTermination#setDefaultValues ERROR: "
					+ "given array has incorrect length of: "+defaultValues.length+
					" expected: "+this.notConnectedInput.length);
		}
		for(int i=0; i<this.getOutputDimensions(); i++)
			this.defOutputValues[i] = defaultValues[i];
	}

	@Override
	public void resetDefaultOutputValues() {
		for(int i=0; i<this.outputDimension; i++)
			this.defOutputValues[i] = DEF_OUTVAL;
	}


}

