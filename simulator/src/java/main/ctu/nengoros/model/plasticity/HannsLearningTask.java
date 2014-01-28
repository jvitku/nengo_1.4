package ctu.nengoros.model.plasticity;

import ctu.nengoros.model.transformMultiTermination.AbstractPlasticMultiTermination;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.plasticity.impl.PlasticEnsembleImpl;
import ca.nengo.util.ThreadTask;

/**
 * The equivalent of {@link ca.nengo.util.impl.LearningTask} used for separating
 * learning process used for the HANNS nodes.  
 * 
 * "This task will separate the learning calculations such as getDerivative into independent
 * threadable tasks."
 *
 * @author Jaroslav Vitku
 */
public class HannsLearningTask implements ThreadTask {

	private AbstractPlasticMultiTermination myParent;
	private AbstractPlasticTermination myTermination;

	private final int startIdx;
	private final int endIdx;
	private boolean finished;

	/**
	 * @param parent Parent AbstractPlasticMultiTermination of this task
	 * @param termination AbstractPlasticTermination that this task will learn on
	 * @param start Starting index for the set of terminations to learn on
	 * @param end Ending index for the set of terminations to learn on
	 */
	public HannsLearningTask(AbstractPlasticMultiTermination parent, AbstractPlasticTermination termination, int start, int end) {
		myParent = parent;
		myTermination = termination;
		startIdx = start;
		endIdx = end;
		finished = true;
	}

	/**
	 * @param copy LearningTask to copy the parent and termination values from
	 * @param start Starting index for the set of terminations to learn on
	 * @param end Ending index for the set of terminations to learn on
	 */
	public HannsLearningTask(HannsLearningTask copy, int start, int end) {
		myParent = copy.myParent;
		myTermination = copy.myTermination;
		startIdx = start;
		endIdx = end;
		finished = copy.finished;
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		finished = false;
	}

	/**
	 * @see ca.nengo.util.ThreadTask#getParent()
	 */
	public AbstractPlasticMultiTermination getParent() {
		return myParent;
	}

	/**
	 * @see ca.nengo.util.ThreadTask#isFinished()
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * @see ca.nengo.util.ThreadTask#run(float, float)
	 */
	public void run(float startTime, float endTime) throws SimulationException {
		if (!finished) {
			try {
				myTermination.updateTransform(endTime, startIdx, endIdx);
			} catch (StructuralException e) {
				throw new SimulationException(e.getMessage());
			}
			finished = true;
		}
	}

	@Override
	public HannsLearningTask clone() throws CloneNotSupportedException {
		return this.clone(myParent, myTermination);
	}

	// TODO: implement cloning similar to the original LearningTask
	public HannsLearningTask clone(PlasticEnsembleImpl parent) throws CloneNotSupportedException {
		throw new CloneNotSupportedException("ERROR: cloning is TODO");
	}

	public HannsLearningTask clone(AbstractPlasticMultiTermination parent, AbstractPlasticTermination term) 
			throws CloneNotSupportedException {
		throw new CloneNotSupportedException("ERROR: cloning is TODO");
	}

}
