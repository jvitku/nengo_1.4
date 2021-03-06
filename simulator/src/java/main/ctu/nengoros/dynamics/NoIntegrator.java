package ctu.nengoros.dynamics;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.util.TimeSeries;

/**
 * We do not want to integrate anything, but we need an Integrator in order to 
 * instantiate BasicTermination.
 * 
 * @author Jaroslav Vitku
 *
 */
public class NoIntegrator implements Integrator {

	private static final long serialVersionUID = 3531878935017940617L;

	/**
	 * Integrate nothing..
	 */
	@Override
	public TimeSeries integrate(DynamicalSystem system, TimeSeries input) {
		return input;
	}

	public NoIntegrator clone(){
		return null;
	}

}
