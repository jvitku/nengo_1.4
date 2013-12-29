package ctu.nengoros.comm.rosBackend.encoders.multiTermination.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Termination;
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
public class SumMultiTerminationII extends BasicMultiTermination{

	private static final long serialVersionUID = 5317677987162247159L;

	public final String me = "[SumMultiTermination] ";

	String mess = me+"Dimension sizes of particular Terminations differ!";

	//TimeSeriesImpl myValue;
	float[][] myVals;

	public SumMultiTerminationII(NeuralModule parent, String name, Integrator integ, DynamicalSystem myDynamics){
		super(parent, name, integ, myDynamics);
	}

	/**
	 * This class sums all values on own Terminations and sets it as own value. 
	 */
	@Override
	protected void runCombineValues(float startTime, float endTime)
			throws SimulationException {

		if(this.orderedTerminations.isEmpty()){
			System.err.println(me+"Warning: no terminations registered");
			return;
		}
		// read the first Termination
		this.initMyValue();

		Float[] weight; 

		// add the rest of Terminations to it
		for(int i=1; i<this.orderedTerminations.size(); i++){
			Termination t = this.orderedTerminations.get(i);

			weight = readWeights(t.getName());

			super.checkInstance(t);
			TimeSeries out = ((BasicTermination)t).getOutput();
			this.sumToMyValue((TimeSeriesImpl) out, weight);
		}
	}

	private void initMyValue() throws SimulationException{
		Termination t = this.orderedTerminations.get(0);

		//float[] val;
		float [][] values = null;

		super.checkInstance(t);

		try {
			myValue = (TimeSeriesImpl) ((BasicTermination)t).getOutput().clone();
			// termination not set
			/*
			if(myValue ==null){
				myVals = 
			}*/
			myVals = myValue.getValues();
			
			values = ((BasicTermination)t).getOutput().getValues();
			
			System.out.println("values are these "+toStr(values));
			System.out.println("values are these MyVals "+toStr(myVals));
			if(true)
				return;
			
			System.out.println("reading vals from first Termination named "+t.getName()+" these are "+toStr(values));
			System.out.println("reading MU vals, these are: these are "+toStr(myVals));

		} catch (CloneNotSupportedException e) {
			System.err.println(me+"TimeSeries of the Termination "
					+t.getName()+" does not support clonning");
			e.printStackTrace();
		}

		Float[] weights = readWeights(t.getName());
		/*
		// for each time step
		for(int i=0; i<myVals[0].length; i++){
			// each dimension
			for(int j=0; j<myVals.length; j++){
				// System.out.println("val of first termination is: "+myVals[i][j]);
				String tmp ="myVals[i][j] "+ myVals[i][j];
				// each dimension is weighted according to own weight
				myVals[i][j] = v[i][j] * weights[j];
				//System.out.println("weighted value "+myVals[i][j]);
				System.out.println(tmp+" is: "+myVals[i][j]+ " time: "+i+" dim "+j+" weight "+weights[j]+" value[i][j] "+myVals[i][j]);
			}
		}

		 */

		// for all time samples
		for(int i=0; i<values.length; i++){
			// sum all dimensions
			for(int j=0; j<values[0].length; j++){
				String tmp ="myVals[i][j] "+ myVals[i][j];
				// each dimension is weighted according to own weight

				myVals[i][j] = values[i][j] * weights[j];
				System.out.println(tmp+" is: "+myVals[i][j]+ " time: "+i+" dim "+j+" weight "+weights[j]+" value[i][j] "+values[i][j]);
			}
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
	/*
	private TimeSeriesImpl initZeros(){
		TimeSeriesImpl zeros = new 	
	}*/

	/**
	 * Just add values on this termination to myValue.
	 * 
	 * @param t Termination with values to read
	 * @throws SimulationException
	 */
	private void sumToMyValue(final TimeSeriesImpl t, Float[] weights) throws SimulationException{

		final float[][] values = t.getValues();

		System.out.println("values are these "+toStr(values));
		if(true)
			return;
		
		if((myVals.length != values.length) ||
				(myVals.length>0 && myVals[0].length != values[0].length)){
			System.err.println(mess);
			throw new SimulationException(mess);
		}
		if(values.length == 0){
			System.err.println(me+"Warning: values have length of 0");
			return;
		}

		System.out.println("reading vals the Termination .. these are      "+toStr(values));
		System.out.println("reading MU vals, these are: these are "+toStr(myVals));

		System.out.println("weight is "+this.toStr(weights));

		// for all time samples
		for(int i=0; i<values.length; i++){

			System.out.println("time "+i);
			// sum all dimensions
			for(int j=0; j<values[0].length; j++){
				//String tmp ="myVals[i][j] "+ myVals[i][j];
				// each dimension is weighted according to own weight
				System.out.println("i "+i+" j"+j);
				System.out.println("cc = "+values[i][j]+" * "+weights[j]);
				float vv = values[i][j] * weights[j];
				System.out.println("cc is "+vv+" input is "+values[i][j]+" w is "+weights[i]);
				myVals[i][j] = myVals[i][j] + vv;
				//myVals[i][j] = myVals[i][j] + values[i][j] * weights[j];
				//System.out.println(tmp+" is: "+myVals[i][j]+ " time: "+i+" dim "+j+" weight "+weights[j]+" value[i][j] "+values[i][j]);
			}
		}
		System.out.println("AFTER reading MU vals, these are: these are "+toStr(myVals));
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
