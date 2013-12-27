package ctu.nengoros.dynamics;

import ca.nengo.dynamics.impl.LTISystem;
import ca.nengo.model.Units;
import ca.nengo.util.MU;

/**
 * New Termination needs to have defined its own dynamics for now. 
 * This is simple helper class which defines simple LTI with no dynamics at all.
 * 
 * @see <a href="http://web.mit.edu/2.14/www/Handouts/StateSpace.pdf">LTI system reminder</a>
 * 
 * @author Jaroslav Vitku
 *
 */
public class IdentityLTISystem extends LTISystem {

	private static final long serialVersionUID = 9025070383196906837L;

	/**
	 * Creates SimpleLTISystem which implements just identity
	 * It has one state variable and matrix A=0
	 * 
	 * @param numberOfInputs
	 */
	public IdentityLTISystem(int numInputs){

		super(MU.zero(1,1),						// A 
				MU.zero(1,numInputs),			// B
				MU.zero(numInputs, 1),			// C
				MU.diag(ones(numInputs)), 		// D - identity here
				new float[1],					// x0 = initial conditions 
				Units.uniform(Units.UNK, numInputs));
	}

	private static float[] ones(int len){
		float [] out = new float[len];
		for(int i=0;i<len; i++)
			out[i]=1;
		return out;
	}
}
