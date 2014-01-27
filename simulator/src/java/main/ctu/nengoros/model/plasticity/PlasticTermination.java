package ctu.nengoros.model.plasticity;

import ca.nengo.model.PlasticNodeTermination;
import ca.nengo.model.StructuralException;

/**
 * <p>PlasticTermination is the same as the {@link ca.nengo.model.PlasticNodeTermination}, 
 * but can be used also on non-neural-based nodes, such as HANNS nodes.</p>
 * 
 * <p>Each PlasticTermination may have associated the {@link ctu.nengoros.model.plasticity.HannsLearningTask}, 
 * which is able to run learning in separate Thread by calling the {@link #updateTransform(float, int, int)}.</p>
 * 
 *  @see ca.nengo.sim.impl.LocalSimulator#step(float, float)
 * 
 * @author Jaroslav Vitku
 */
public interface PlasticTermination extends PlasticNodeTermination{

    /**
     * This is used externally by the {@link Thread#run()}.
     * 
     * @param time Current time
     * @param start The start index of the range of transform values to update (for multithreading)
     * @param end The end index of the range of transform values to update (for multithreading)
     * @throws StructuralException if
     */
    public abstract void updateTransform(float time, int start, int end) throws StructuralException;
}