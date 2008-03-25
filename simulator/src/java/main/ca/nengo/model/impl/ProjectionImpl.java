/*
 * Created on May 5, 2006
 */
package ca.nengo.model.impl;

import ca.nengo.model.Network;
import ca.nengo.model.Node;
import ca.nengo.model.Origin;
import ca.nengo.model.Projection;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.nef.NEFEnsemble;
import ca.nengo.model.nef.impl.BiasOrigin;
import ca.nengo.model.nef.impl.BiasTermination;
import ca.nengo.model.nef.impl.DecodedOrigin;
import ca.nengo.model.nef.impl.DecodedTermination;
import ca.nengo.plot.Plotter;
import ca.nengo.util.MU;

/**
 * Default implementation of <code>Projection</code>.
 * 
 * TODO: unit tests
 * 
 * @author Bryan Tripp
 */
public class ProjectionImpl implements Projection {

	private static final long serialVersionUID = 1L;
	
	private Origin myOrigin;
	private Termination myTermination;
	private Network myNetwork;
	
	private boolean myBiasIsEnabled;
	private NEFEnsemble myInterneurons;
	private BiasOrigin myBiasOrigin; 
	private BiasTermination myDirectBT;
	private BiasTermination myIndirectBT;
	private DecodedTermination myInterneuronTermination;
	
	/**
	 * @param origin  The Origin at the start of this Projection
	 * @param termination  The Termination at the end of this Projection
	 * @param Network The Network of which this Projection is a part 
	 */
	public ProjectionImpl(Origin origin, Termination termination, Network network) {
		myOrigin = origin;
		myTermination = termination;
		myNetwork = network;
		
		myBiasIsEnabled = false;
		myInterneurons = null;
		myDirectBT = null;
		myIndirectBT = null;
	}

	/**
	 * @see ca.nengo.model.Projection#getOrigin()
	 */
	public Origin getOrigin() {
		return myOrigin;
	}

	/**
	 * @see ca.nengo.model.Projection#getTermination()
	 */
	public Termination getTermination() {
		return myTermination;
	}

	/**
	 * @see ca.nengo.model.Projection#biasIsEnabled()
	 */
	public boolean biasIsEnabled() {
		return myBiasIsEnabled;
	}

	/**
	 * @see ca.nengo.model.Projection#enableBias(boolean)
	 */
	public void enableBias(boolean enable) {
		if (myInterneurons != null) {
			myDirectBT.setEnabled(enable);
			myIndirectBT.setEnabled(enable);
			myBiasIsEnabled = enable;
		}
	}

	/**
	 * @see ca.nengo.model.Projection#getNetwork()
	 */
	public Network getNetwork() {
		return myNetwork;
	}

	/**
	 * @throws StructuralException 
	 * @see ca.nengo.model.Projection#addBias(int, float, float, boolean, boolean)
	 */
	public void addBias(int numInterneurons, float tauInterneurons, float tauBias, boolean excitatory, boolean optimize) throws StructuralException {
		if ( !(myOrigin instanceof DecodedOrigin) || !(myTermination instanceof DecodedTermination)) {
			throw new RuntimeException("This feature is only implemented for projections from DecodedOrigins to DecodedTerminations");
		}
		
		DecodedOrigin baseOrigin = (DecodedOrigin) myOrigin;
		DecodedTermination baseTermination = (DecodedTermination) myTermination;
		NEFEnsemble pre = (NEFEnsemble) baseOrigin.getNode();
		NEFEnsemble post = (NEFEnsemble) baseTermination.getNode();
				
		myBiasOrigin = pre.addBiasOrigin(baseOrigin, numInterneurons, getUniqueNodeName(post.getName() + ":" + baseTermination.getName()), excitatory);
		myInterneurons = myBiasOrigin.getInterneurons();
		Plotter.plot(myInterneurons, NEFEnsemble.X);
		myNetwork.addNode(myInterneurons);		
		BiasTermination[] bt = post.addBiasTerminations(baseTermination, tauBias, myBiasOrigin.getDecoders(), baseOrigin.getDecoders());
		myDirectBT = bt[0];
		myIndirectBT = bt[1];
		float[][] tf = new float[][]{new float[]{0, 1/tauInterneurons/tauInterneurons}, new float[]{2/tauInterneurons, 1/tauInterneurons/tauInterneurons}};
		myInterneuronTermination = (DecodedTermination) myInterneurons.addDecodedTermination("bias", MU.I(1), tf[0], tf[1], 0, false); 
		
		myNetwork.addProjection(myBiasOrigin, myDirectBT);
		myNetwork.addProjection(myBiasOrigin, myInterneuronTermination);
		myNetwork.addProjection(myInterneurons.getOrigin(NEFEnsemble.X), myIndirectBT);
		
		if (optimize) {
			float[][] baseWeights = MU.prod(post.getEncoders(), MU.prod(baseTermination.getTransform(), MU.transpose(baseOrigin.getDecoders())));			
			myBiasOrigin.optimizeDecoders(baseWeights, myDirectBT.getBiasEncoders());
			myBiasOrigin.optimizeInterneuronDomain(myInterneuronTermination, myIndirectBT);
		}
		
		myBiasIsEnabled = true;
	}
	
	private String getUniqueNodeName(String base) {
		String result = base;
		boolean done = false;
		int c = 2;
		Node[] nodes = myNetwork.getNodes(); 
		while (!done) {
			done = true;
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].getName().equals(result)) {
					done = false;
					result = base + c++;
				}
			}
		}
		return result;
	}

	/**
	 * @see ca.nengo.model.Projection#removeBias()
	 */
	public void removeBias() {
		try {
			myNetwork.removeProjection(myDirectBT);
			myNetwork.removeProjection(myIndirectBT);
			myNetwork.removeProjection(myInterneuronTermination);
			myNetwork.removeNode(myInterneurons.getName());
			myBiasIsEnabled = false;
		} catch (StructuralException e) {
			throw new RuntimeException("Error while trying to remove bias (this is probably a bug in ProjectionImpl)", e);
		}
	}

	/**
	 * @see ca.nengo.model.Projection#getWeights()
	 */
	public float[][] getWeights() {
		float[][] result = null;
		
		if ( (myOrigin instanceof DecodedOrigin) && (myTermination instanceof DecodedTermination)) {
			float[][] encoders = ((NEFEnsemble) myTermination.getNode()).getEncoders();
			float[][] transform = ((DecodedTermination) myTermination).getTransform();
			float[][] decoders = ((DecodedOrigin) myOrigin).getDecoders();
			result = MU.prod(encoders, MU.prod(transform, MU.transpose(decoders)));
			
			if (myBiasIsEnabled) {
				float[] biasEncoders = myDirectBT.getBiasEncoders();
				float[][] biasDecoders = myBiasOrigin.getDecoders();
				float[][] weightBiases = MU.prod(MU.transpose(new float[][]{biasEncoders}), MU.transpose(biasDecoders));
				result = MU.sum(result, weightBiases);
			}
		} else if (myTermination instanceof DecodedTermination) {
			float[][] encoders = ((NEFEnsemble) myTermination.getNode()).getEncoders();
			float[][] transform = ((DecodedTermination) myTermination).getTransform();
			result = MU.prod(encoders, transform);
		} else {
			//TODO: add getWeights() to Termination, implement in EnsembleTermination from LinearExponentialTermination.getWeights()
			throw new RuntimeException("Not implemented for non-DecodedTerminations");
		}
		
		return result;
	}

}