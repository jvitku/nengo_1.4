package ctu.nengoros.model.transformMultiTermination;

import ca.nengo.util.TaskSpawner;

/**
 * This is {@link ctu.nengoros.model.plasticity.MultiTermination} which
 * supports plastic (and non-plastic) Terminations. For each plastic termination, 
 * there could be a Thread which implements the plasticity rule.
 *  
 * @author Jaroslav Vitku
 *
 */
public interface PlasticMultiTermination extends MultiTermination, TaskSpawner{

	
}
