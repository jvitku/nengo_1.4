package ctu.nengoros.model.plasticity;

import ca.nengo.model.PlasticNodeTermination;
import ca.nengo.model.StructuralException;

/**
 * PlasticTermination is the same as the {@link ca.nengo.model.PlasticNodeTermination}, 
 * but can be used also on non-neural-based nodes, such as HANNS ndoes. 
 * The method {@link #updateTransform(float, int, int)} is used from externally ran 
 * Thread for better efficiency.  
 * 
 * @author Jaroslav Vitku
 *
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