package ctu.nengorosHeadless.rosBackend.encoders;

import ctu.nengoros.comm.rosBackend.encoders.CommonEncoder;
import ctu.nengorosHeadless.network.modules.NeuralModule;

public interface Encoder extends CommonEncoder{

	/**
	 * Get the parent of Encoder
	 * @return my parent to whom I register my Terminations 
	 */
	NeuralModule getParent();
	
}
