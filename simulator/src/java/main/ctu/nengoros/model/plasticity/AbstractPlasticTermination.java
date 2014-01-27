package ctu.nengoros.model.plasticity;

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.PlasticNodeTermination;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;

/**
 * This implements PlasticTermination, where each Termination has
 * input and output dimension. Weights between these two dimensions
 * are determined by a Transformation that can be either
 * defined before simulation, or changed during the simulation by some
 * PlasticityRule. 
 * 
 * //TODO implement this one 
 * 
 * @author Jaroslav Vitku
 *
 */
public abstract class AbstractPlasticTermination implements PlasticTermination{

	private static final long serialVersionUID = -1178115852907379798L;
	
	private final String name;
	private final int[] dimensionSizes;
	
	public AbstractPlasticTermination(String name, int[] dimensionSizes){
		this.name = name;
		this.dimensionSizes = dimensionSizes.clone();
	}
	
	@Override
	public String getName() { return this.name; }

	@Override
	public int getDimensions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setValues(InstantaneousOutput values)
			throws SimulationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getTau() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTau(float tau) throws StructuralException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getModulatory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setModulatory(boolean modulatory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset(boolean randomize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float[] getWeights() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWeights(float[] weights, boolean save) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyWeights(float[] change, boolean save) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveWeights() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InstantaneousOutput getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getOutput() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void updateTransform(float time, int start, int end)
			throws StructuralException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public PlasticNodeTermination clone() throws CloneNotSupportedException{
		return null;	
	}
	
	@Override
	public PlasticNodeTermination clone(Node node)
			throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
