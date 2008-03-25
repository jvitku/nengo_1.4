/*
 * Created on 2-Jun-2006
 */
package ca.nengo.util.impl;

import ca.nengo.util.IndexFinder;
import ca.nengo.util.InterpolatorND;
import ca.nengo.util.TimeSeries;

/**
 * Interpolates linearly between adjacent values of a vector time series. 
 * 
 * TODO: test
 *   
 * @author Bryan Tripp
 */
public class LinearInterpolatorND implements InterpolatorND {

	private static final long serialVersionUID = 1L;
	
	private TimeSeries mySeries;
	private IndexFinder myFinder;
	private float[] myTimes;
	
	/**
	 * @param series Series to interpolate
	 */
	public LinearInterpolatorND(TimeSeries series) {
		setTimeSeries(series);
	}

	/**
	 * @see ca.nengo.util.InterpolatorND#setTimeSeries(ca.nengo.util.TimeSeries)
	 */
	public void setTimeSeries(TimeSeries series) {
		mySeries = series;
		myFinder = getFinder(series.getTimes());
		myTimes = series.getTimes();
	}

	/**
	 * @see ca.nengo.util.InterpolatorND#interpolate(float)
	 */
	public float[] interpolate(float time) {
		float[] result = null;
		
		if (myTimes[0] >= time) {
			result = mySeries.getValues()[0];
		} else if (myTimes[myTimes.length-1] <= time) {
			result = mySeries.getValues()[myTimes.length-1];
		} else {
			int below = myFinder.findIndexBelow(time);
			
			float prop = (time - myTimes[below]) / (myTimes[below+1] - myTimes[below]);
			float[] low = mySeries.getValues()[below];
			float[] high = mySeries.getValues()[below+1];
			
			result = new float[low.length];
			for (int i = 0; i < low.length; i++) {
				result[i] = low[i] + prop * (high[i] - low[i]);
			}
		}
		
		return result;
	}
	
	/**
	 * Uses a StatefulIndexFinder by default. Override to change this. 
	 *  
	 * @param times Times of time series 
	 * @return IndexFinder on times 
	 */
	public IndexFinder getFinder(float[] times) {
		return new StatefulIndexFinder(times);
	}

	@Override
	protected LinearInterpolatorND clone() throws CloneNotSupportedException {
		LinearInterpolatorND result = new LinearInterpolatorND(mySeries.clone());
		result.myTimes = myTimes.clone();
		result.myFinder = myFinder.clone();
		return result;
	}
	
	

}