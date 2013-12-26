package ctu.nengoros.comm.rosBackend.multiTermination.impl;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Termination;
import ca.nengo.model.impl.BasicTermination;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;
import ctu.nengoros.comm.rosBackend.multiTermination.BasicMultiTermination;

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

	TimeSeriesImpl myValue;
	float[][] myVals;

	public SumMultiTermination(Node parent, String name, int dimension){
		super(parent,name, dimension);
	}

	/**
	 * This class sums all values on own Terminations and sets it 
	 * as own value. 
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

		// add the rest of Terminations to it
		for(int i=1; i<this.orderedTerminations.size(); i++){
			Termination t = this.orderedTerminations.get(i);

			super.checkInstance(t);
			TimeSeries out = ((BasicTermination)t).getOutput();
			this.sumToMyValue((TimeSeriesImpl) out);
		}
	}

	private void initMyValue() throws SimulationException{
		Termination t = this.orderedTerminations.get(0);

		super.checkInstance(t);

		try {
			myValue = (TimeSeriesImpl) ((BasicTermination)t).getOutput().clone();
			myVals = myValue.getValues();

		} catch (CloneNotSupportedException e) {
			System.err.println(me+"TimeSeries of the Termination "
					+t.getName()+" does not support clonning");
			e.printStackTrace();
		}
	}

	/**
	 * Just add values on this termination to myValue
	 * @param t Termination with values to read
	 * @throws SimulationException
	 */
	private void sumToMyValue(TimeSeriesImpl t) throws SimulationException{

		float[][] values = t.getValues();

		if((myVals.length != values.length) ||
				(myVals.length>0 && myVals[0].length != values[0].length)){
			System.err.println(mess);
			throw new SimulationException(mess);
		}

		if(values.length == 0){
			System.err.println(me+"Warning: values have length of 0");
			return;
		}

		// for all time samples
		for(int i=0; i<values.length; i++){
			// sum all dimensions
			for(int j=0; j<values[0].length; j++){
				myVals[i][j] = myVals[i][j] + values[i][j];
			}
		}
	}

}
