package ctu.nengoros.model.multiTermination.impl;

import ca.nengo.util.TaskSpawner;
import ctu.nengoros.model.multiTermination.MultiTermination;

/**
 * This is {@link ctu.nengoros.model.plasticity.MultiTermination} which
 * supports plastic (and non-plastic) Terminations. For each plastic termination, 
 * there could be a Thread which implements the plasticity rule.
 *  
 * @author Jaroslav Vitku
 *
 */
public interface PlasticMultiTermination extends MultiTermination, TaskSpawner{

	public void updateTransform();
	
}
