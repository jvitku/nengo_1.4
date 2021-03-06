package ctu.nengoros.comm.rosBackend.encoders.multiTermination.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.model.impl.BasicTermination;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;
import ctu.nengoros.comm.rosBackend.encoders.multiTermination.BasicMultiTermination;
import ctu.nengoros.modules.NeuralModule;

/**
 * 
 * <p>The Nengo did not have sufficient support of connecting multiple Origins 
 * to one Termination. This provides weighted combination (e.g. sum) of multiple 
 * inputs to one MultiTermination.</p>
 * 
 * <p>This is similar to Termination in terms that it represents one input to the Node. 
 * It holds list of own Terminations with their weights. The weighted inputs from own 
 * Terminations are combined into the resulting value on the MultiTermination.</p>
 *   
 * @author Jaroslav Vitku
 *
 */
public class SumMultiTermination extends BasicMultiTermination{

	private static final long serialVersionUID = 5317677987162247159L;

	public final String me = "[SumMultiTermination] ";

	String mess = me+"Dimension sizes of particular Terminations differ!";

	public SumMultiTermination(NeuralModule parent, String name, Integrator integ, DynamicalSystem myDynamics){
		super(parent, name, integ, myDynamics);
	}

	/**
	 * This class sums all values on own Terminations and sets it as own value. 
	 */
	@Override
	protected void runCombineValues(float startTime, float endTime)
			throws SimulationException {

		// initialize with zeros (turns out that there have to be two arrays)
		myValue = this.initZeros(startTime, endTime);
		
		if(this.orderedTerminations.isEmpty()){
			System.err.println(me+"Warning: no terminations registered");
			return;
		}

		Float[] weight; 

		// add the rest of Terminations to it
		for(int i=0; i<this.orderedTerminations.size(); i++){
			Termination t = this.orderedTerminations.get(i);

			weight = readWeights(t.getName());

			super.checkInstance(t);
			
			TimeSeries out = ((BasicTermination)t).getOutput();
			this.sumToMyValue((TimeSeriesImpl) out, weight);
		}
	}

	public String toStr(float[][] f){
		String out = "";

		for(int i=0; i<f.length; i++){
			for(int j=0; j<f[0].length; j++){
				out = out+" "+f[i][j];
			}
			out = out+" | ";
		}
		return out;
	}

	/**
	 * Return time series with current simulation times and with zero values
	 * @param startTime start time of sim. step
	 * @param endTime end times
	 * @return initialized TimeSeriesImpl with zero data and correct getTimes() values
	 */
	private TimeSeriesImpl initZeros(float startTime, float endTime){

		float[] input = new float[this.dimensions];
		for(int i=0; i<this.dimensions; i++)
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
	 * Just add values on this termination to myValue.
	 * 
	 * @param t Termination with values to read
	 * @throws SimulationException
	 */
	private void sumToMyValue(final TimeSeriesImpl t, final Float[] weights) throws SimulationException{
		final float[][] values = t.getValues();

		// for all time samples
		for(int i=0; i<values.length; i++){
			// sum all dimensions
			for(int j=0; j<values[0].length; j++){
				float vv = values[i][j] * weights[j];
				myValue.getValues()[i][j] = myValue.getValues()[i][j] + vv;
			}
		}
	}

	public String toStr(Float[] f){
		String out = "";
		for(int i=0; i<f.length; i++)
			out = out+" "+f[i];
		return out;
	}

	public String toStr(float[] f){
		String out = "";
		for(int i=0; i<f.length; i++)
			out = out+" "+f[i];
		return out;
	}


}
