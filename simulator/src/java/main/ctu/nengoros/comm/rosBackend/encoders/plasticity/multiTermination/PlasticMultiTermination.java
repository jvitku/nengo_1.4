package ctu.nengoros.comm.rosBackend.encoders.plasticity.multiTermination;

import ca.nengo.util.TaskSpawner;
import ctu.nengoros.comm.rosBackend.encoders.multiTermination.MultiTermination;

/**
 * This is {@link ctu.nengoros.comm.rosBackend.encoders.plasticity.MultiTermination} which
 * supports plastic (and non-plastic) Terminations. For each plastic termination, 
 * there could be a Thread which implements the plasticity rule.
 *  
 * @author Jaroslav Vitku
 *
 */
public interface PlasticMultiTermination extends MultiTermination, TaskSpawner{

	public void updateTransform();
	
}
