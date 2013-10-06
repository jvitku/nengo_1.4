/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NetworkImpl.java". Description:
"Default implementation of Network"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on 27-Nov-2012
 */
package ca.nengo.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ca.nengo.math.Function;
import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.Origin;
import ca.nengo.model.PreciseSpikeOutput;
import ca.nengo.model.RealOutput;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SpikeOutput;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.Units;
import ca.nengo.model.nef.impl.DecodedTermination;
import ca.nengo.model.nef.impl.NEFEnsembleImpl;
import ca.nengo.model.nef.impl.DecodedOrigin;
import ca.nengo.model.plasticity.impl.PESTermination;
import ca.nengo.model.plasticity.impl.PlasticEnsembleImpl;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;

/**
 * Default implementation of Network Array.
 *
 * @author Xuan Choo, Daniel Rasmussen
 */
public class NetworkArrayImpl extends NetworkImpl {

	/**
	 * Default name for a Network
	 */
	private static final long serialVersionUID = 1L;
	// ?? private static Logger ourLogger = Logger.getLogger(NetworkImpl.class);
	
	private final int myNumNodes;
	private int myDimension;
	private final int[] myNodeDimensions;
	
	private NEFEnsembleImpl[] myNodes;
	private Map<String, Origin> myOrigins;
	private int myNeurons;

	/**
	 * Create a network holding an array of nodes.  An 'X' Origin
	 * is automatically created which concatenates the values of each
	 * internal element's 'X' Origin.
	 *  
	 * This object is meant to be created using :func:`nef.Network.make_array()`, allowing for the
	 * efficient creation of neural groups that can represent large vectors.  For example, the
	 * following code creates a NetworkArray consisting of 50 ensembles of 1000 neurons, each of 
	 * which represents 10 dimensions, resulting in a total of 500 dimensions represented::
	 *  
	 *   net=nef.Network('Example Array')
	 *   A=net.make_array('A',neurons=1000,length=50,dimensions=10,quick=True)
	 *    
	 * The resulting NetworkArray object can be treated like a normal ensemble, except for the
	 * fact that when computing nonlinear functions, you cannot use values from different
	 * ensembles in the computation, as per NEF theory.
	 *  
	 * @param name The name of the NetworkArray to create
	 * @param nodes The ca.nengo.model.nef.NEFEnsemble nodes to combine together
	 * @throws StructuralException
	 */
	public NetworkArrayImpl(String name, NEFEnsembleImpl[] nodes) throws StructuralException {
		super();
		
		this.setName(name);
		
		myNodes = nodes;
		myNumNodes = myNodes.length;
		
		myNodeDimensions = new int[myNumNodes];
		myDimension = 0;
		for (int i = 0; i < myNumNodes; i++) {
			myNodeDimensions[i] = myNodes[i].getDimension();
			myDimension += myNodeDimensions[i];
		}
		
		myNeurons = 0;
		
		myOrigins = new HashMap<String, Origin>(10);
		
		for (int i = 0; i < nodes.length; i++) {
			this.addNode(nodes[i]);
			myNeurons += nodes[i].getNodeCount();
		}
		createEnsembleOrigin("X");
		this.setUseGPU(true);
	}
	
	/** 
	 * Create an Origin that concatenates the values of internal Origins.
     *
     * @param name The name of the Origin to create.  Each internal node must already have an Origin 
     * with that name.
     * @throws StructuralException
	 */
	public void createEnsembleOrigin(String name) throws StructuralException {
		Origin[] origins = new Origin[myNumNodes];
		for (int i = 0; i < myNumNodes; i++) {
			origins[i] = myNodes[i].getOrigin(name);
		}
		createEnsembleOrigin(name, origins);
	}
	
	private void createEnsembleOrigin(String name, Origin[] origins) throws StructuralException {
		myOrigins.put(name, new ArrayOrigin(this, name, origins));
		this.exposeOrigin(this.myOrigins.get(name), name);
	}
	
	public int getNeurons() {
		return myNeurons;
	}
		
	
	/**
	 * Create a new Origin.  A new origin is created on each of the 
     * ensembles, and these are grouped together to create an output.
     *  
     * This method uses the same signature as ca.nengo.model.nef.NEFEnsemble.addDecodedOrigin()
     * 
	 * @param name The name of the newly created origin
	 * @param functions A list of ca.nengo.math.Function objects to approximate at this origin
	 * @param nodeOrigin Name of the base Origin to use to build this function approximation
       (this will always be 'AXON' for spike-based synapses)
	 * @return Origin that encapsulates all of the internal node origins
	 * @throws StructuralException
	 */
	public Origin addDecodedOrigin(String name, Function[] functions, String nodeOrigin) throws StructuralException {
		DecodedOrigin[] origins = new DecodedOrigin[myNumNodes];
		for (int i = 0; i < myNumNodes; i++) {
			origins[i] = (DecodedOrigin) myNodes[i].addDecodedOrigin(name,  functions,  nodeOrigin);
		}
		this.createEnsembleOrigin(name, origins);
		return this.getOrigin(name);
	}
	
	/**
	 * Create a new origin by splitting the given functions across the nodes.
	 * 
	 * This method uses the same signature as ca.nengo.model.nef.NEFEnsemble.addDecodedOrigin()
     * 
	 * @param name The name of the newly created origin
	 * @param functions A list of ca.nengo.math.Function objects to approximate at this origin
	 * @param nodeOrigin Name of the base Origin to use to build this function approximation
       (this will always be 'AXON' for spike-based synapses)
       @param splitFunctions True if the functions should be split across the nodes, otherwise
       this behaves the same as the default addDecodedOrigin
	 * @return Origin that encapsulates all of the internal node origins
	 * @throws StructuralException
	 */
	public Origin addDecodedOrigin(String name, Function[] functions, String nodeOrigin, boolean splitFunctions) throws StructuralException {
		if(!splitFunctions)
			return addDecodedOrigin(name, functions, nodeOrigin);
		
		if(functions.length != myDimension)
			System.err.println("Warning, trying to split functions but function list length does " +
					"not match network array dimension");
		
		DecodedOrigin[] origins = new DecodedOrigin[myNumNodes];
		int f=0;
		for (int i = 0; i < myNumNodes; i++) {
			Function[] oFuncs = new Function[myNodeDimensions[i]];
			for (int d=0; d < myNodeDimensions[i]; d++)
				oFuncs[d] = functions[f++];
			origins[i] = (DecodedOrigin) myNodes[i].addDecodedOrigin(name,  oFuncs,  nodeOrigin);
		}
		this.createEnsembleOrigin(name, origins);
		return this.getOrigin(name);
	}
	
	
	/**
	 * Create a new termination.  A new termination is created on each
     * of the ensembles, which are then grouped together.  This termination
     * does not use NEF-style encoders; instead, the matrix is the actual connection
     * weight matrix.  Often used for adding an inhibitory connection that can turn
     * off the whole array (by setting *matrix* to be all -10, for example). 
     *   
	 * @param name The name of the newly created termination
	 * @param weights Synaptic connection weight matrix (NxM where N is the total number of neurons in the NetworkArray)
	 * @param tauPSC Post-synaptic time constant
	 * @param modulatory Boolean value that is False for normal connections, True for modulatory connections 
	 * (which adjust neural properties rather than the input current)
	 * @return Termination that encapsulates all of the internal node terminations
	 * @throws StructuralException
	 */
	public Termination addTermination(String name, float[][] matrix, float tauPSC) throws StructuralException {
		return addTermination(name, matrix, tauPSC, false);
	}
	
	public Termination addTermination(String name, float[][] weights, float tauPSC, boolean modulatory) throws StructuralException {
		assert weights.length == myNeurons;
		
		Termination[] terminations = new Termination[myNumNodes];
		
		for (int i = 0; i < myNumNodes; i++) {
			int nodeNeuronCount = myNodes[i].getNeurons();
			
			float[][] matrix = MU.copy(weights, i * nodeNeuronCount, 0, nodeNeuronCount, -1);
			assert matrix[0].length == myNodeDimensions[i];

			terminations[i] = myNodes[i].addTermination(name, matrix, tauPSC, modulatory);
		}
		
		exposeTermination(new EnsembleTermination(this, name, terminations), name);
		return getTermination(name);
	}
	
	/**
	 * Create a new termination.  A new termination is created on each
     * of the ensembles, which are then grouped together.  This termination
     * does not use NEF-style encoders; instead, the matrix is the actual connection
     * weight matrix.  Often used for adding an inhibitory connection that can turn
     * off the whole array (by setting *matrix* to be all -10, for example). 
     *   
	 * @param name The name of the newly created termination
	 * @param weights Synaptic connection weight matrix (LxNxM where L is the number of nodes in the array, 
	 * N is the number of neurons in each node, and M is the dimensionality of each node)
	 * @param tauPSC Post-synaptic time constant
	 * @param modulatory Boolean value that is False for normal connections, True for modulatory connections 
	 * (which adjust neural properties rather than the input current)
	 * @return Termination that encapsulates all of the internal node terminations
	 * @throws StructuralException
	 */
	public Termination addTermination(String name, float[][][] matrix, float tauPSC) throws StructuralException {
		return addTermination(name, matrix, tauPSC, false);
	}
	
	public Termination addTermination(String name, float[][][] weights, float tauPSC, boolean modulatory) throws StructuralException {
		assert weights.length == myNumNodes && weights[0].length == myNeurons;
		
		Termination[] terminations = new Termination[myNumNodes];
		
		for (int i = 0; i < myNumNodes; i++) {
			assert weights[i][0].length == myNodeDimensions[i];
			terminations[i] = myNodes[i].addTermination(name, weights[i], tauPSC, modulatory);
		}
		
		exposeTermination(new EnsembleTermination(this, name, terminations), name);
		return getTermination(name);
	}

	
	/**
	 * Create a new decoded termination.  A new termination is created on each
     * of the ensembles, which are then grouped together.  
     *   
	 * @param name The name of the newly created termination
	 * @param matrix Transformation matrix which defines a linear map on incoming information,
     *      onto the space of vectors that can be represented by this NetworkArray. The first dimension
     *      is taken as matrix columns, and must have the same length as the Origin that will be connected
     *      to this Termination. The second dimension is taken as matrix rows, and must have the same
     *      length as the encoders of this NEFEnsemble.
	 * @param tauPSC Post-synaptic time constant
	 * @param modulatory Boolean value that is False for normal connections, True for modulatory connections 
	 * (which adjust neural properties rather than the input current)
	 * @return Termination that encapsulates all of the internal node terminations
	 * @throws StructuralException
	 */
	public Termination addDecodedTermination(String name, float[][] matrix, float tauPSC) throws StructuralException {
		return addDecodedTermination(name, matrix, tauPSC, false);
	}
	
	public Termination addDecodedTermination(String name, float[][] matrix, float tauPSC, boolean modulatory) throws StructuralException {
		assert matrix.length == myDimension;
		
		Termination[] terminations = new Termination[myNumNodes];
		
		int dimCount = 0;
		
		for (int i = 0; i < myNumNodes; i++) {
			float[][] submatrix = MU.copy(matrix, dimCount, 0, myNodeDimensions[i], -1);

			terminations[i] = myNodes[i].addDecodedTermination(name, submatrix, tauPSC, modulatory);
			dimCount += myNodeDimensions[i];
		}
		
		exposeTermination(new EnsembleTermination(this, name, terminations), name);
		return getTermination(name);
	}	
	
	

	/**
	 * Create a new termination.  A new termination is created on the specified ensembles, 
	 * which are then grouped together.  This termination does not use NEF-style encoders; 
	 * instead, the matrix is the actual connection weight matrix.  Often used for adding an inhibitory 
	 * connection that can turn off selected ensembles within the array (by setting *matrix* to be 
	 * all -10, for example).  
	 * 
	 * @param string name: the name of the newly created origin
	 * @param matrix: synaptic connection weight matrix (NxM where M is the total number of neurons in the ensembles to be connected)
	 * @param float tauPSC: post-synaptic time constant
	 * @param boolean isModulatory: False for normal connections, True for modulatory connections (which adjust neural
	 *                                  properties rather than the input current)
	 * @param index: The indexes of the ensembles to connect to. If set to None, this function behaves exactly like addTermination().
	 * 
	 * @return the new termination
	 */
	public Termination addIndexTermination(String name, float[][] matrix, float tauPSC) throws StructuralException {
		return addIndexTermination(name, matrix, tauPSC, false, null);
	}
	

	public Termination addIndexTermination(String name, float[][] matrix, float tauPSC, boolean isModulatory) throws StructuralException {
		return addIndexTermination(name, matrix, tauPSC, isModulatory, null);
	}
		
	public Termination addIndexTermination(String name, float[][] matrix, float tauPSC, int[] index) throws StructuralException {
		return addIndexTermination(name, matrix, tauPSC, false, index);
	}
	
	public Termination addIndexTermination(String name, float[][] matrix, float tauPSC, boolean isModulatory, int[] index) throws StructuralException {
		if(index == null){
			index = new int[myNumNodes];
			for(int i=0; i < index.length; i++)
				index[i] = i;
		}
		
		ArrayList<Termination> terminations = new ArrayList<Termination>();

		int count=0;
		for(int i=0; i < myNumNodes; i++) {
			for(int j=0; j < index.length; j++) {
				if(index[j] == i) {
					Termination t = myNodes[i].addTermination(name, MU.copy(matrix,count*myNodes[i].getNeurons(),0,myNodes[i].getNeurons(),-1),
							tauPSC, isModulatory);
					count++;
					terminations.add(t);
					break;
				}
			}
		}
		
		EnsembleTermination term = new EnsembleTermination(this, name, terminations.toArray(new Termination[0]));
		exposeTermination(term,name);
		return getTermination(name);
	}
	
	/**
	 * Gets the nodes in the proper order from the network array. The NetworkImpl version of this function relies on 
     * the nodeMap object which is sometimes out of order.
     * 
     * @return the nodes in this network array
	 */
	public Node[] getNodes() {
		return myNodes;
	}
	
	/**
	 * @see ca.nengo.model.Network#getTerminations()
	 */
	public Termination[] getTerminations() {
		Termination[] terminations = super.getTerminations();
		ArrayList<Termination> decodedTerminations = new ArrayList<Termination>();
		ArrayList<Termination> nonDecodedTerminations = new ArrayList<Termination>();
		EnsembleTermination baseTermination;
		for(int i=0; i < terminations.length; i++) {
			if(terminations[i] instanceof NetworkImpl.TerminationWrapper)
				baseTermination = (EnsembleTermination)((NetworkImpl.TerminationWrapper)terminations[i]).getBaseTermination();
			else
				baseTermination = (EnsembleTermination)terminations[i];
			
			Termination[] nodeTerminations = baseTermination.getNodeTerminations();
			if(nodeTerminations != null) {
				if(nodeTerminations[0] instanceof DecodedTermination)
					decodedTerminations.add(terminations[i]);
				else if(nodeTerminations[0] instanceof EnsembleTermination)
					nonDecodedTerminations.add(terminations[i]);
			}
		}
		nonDecodedTerminations.addAll(decodedTerminations);
		
		return nonDecodedTerminations.toArray(new Termination[0]);
		
	}
	
	public Termination addPlasticTermination(String name, float[][] weights, float tauPSC, float[][] decoders) throws StructuralException {
		return addPlasticTermination(name, weights, tauPSC, decoders, null);
	}
	
	/**
	 * Create a new plastic termination.  A new termination is created on each
     * of the ensembles, which are then grouped together.
     * 
	 * @param name The name of the newly created PES termination
	 * @param weights Synaptic connection weight matrix (NxM where N is the total number of neurons in the NetworkArray)
	 * @param tauPSC Post-synaptic time constant
	 * (which adjust neural properties rather than the input current)
	 * @param weightFunc object wrapping a function that consumes a weight matrix and returns a modified weight matrix
	 * @return Termination that encapsulates all of the internal node terminations
	 * @throws StructuralException
	 */
	public Termination addPlasticTermination(String name, float[][] weights, float tauPSC, float[][] decoders, WeightFunc weightFunc) throws StructuralException {
		assert weights.length == myNeurons;
		
		Termination[] terminations = new Termination[myNumNodes];
		int d=0;
		
		int nodeDs = myNodes[0].getDimension();
		
		float[][] decoderWeights = MU.prod(weights,MU.transpose(decoders));
		
		for (int i = 0; i < myNodes.length; i++) {
			float[][] encoders = myNodes[i].getEncoders();
			float[][] w = MU.prod(encoders,MU.copy(decoderWeights,d,0,nodeDs,-1));
			if(weightFunc != null)
				w = weightFunc.call(w);
			
			terminations[i] = myNodes[i].addPESTermination(name, w, tauPSC, false);
			d += myNodes[i].getDimension();
		}
		
		exposeTermination(new EnsembleTermination(this, name, terminations), name);
		return getTermination(name);
	}
	
	public interface WeightFunc {
		public float[][] call(float[][] weights);
	}
	
	
	public int[] getNodeDimension() {
		return myNodeDimensions;
	}
	
	/**
	 * @see ca.nengo.model.nef.NEFEnsemble#getDimension()
	 */
	public int getDimension() {
		return myDimension;
	}
	
	/**
	 * Exposes the AXON terminations of each ensemble in the network.
	 */
	public void exposeAxons() throws StructuralException {
		for(int i=0; i < myNumNodes; i++)
			exposeOrigin(myNodes[i].getOrigin("AXON"), "AXON_"+i);
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
	public Properties listStates() {
		return myNodes[0].listStates();
	}

	/**
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException{
		float[] times = myNodes[0].getHistory(stateName).getTimes();
		float[][] values = new float[1][myNumNodes];
		Units[] units = new Units[myNumNodes];
		
		for(int i=0; i < myNumNodes; i++) {
			TimeSeries data = myNodes[i].getHistory(stateName);
			units[i] = data.getUnits()[0];
			values[0][i] = data.getValues()[0][0];
		}
		return new TimeSeriesImpl(times, values, units);
	}
	
	/**
	 * Sets learning parameters on learned terminations in the array.
	 * 
	 * @param learnTerm name of the learned termination
	 * @param modTerm name of the modulatory termination
	 * @param rate learning rate
	 */
	public void learn(String learnTerm, String modTerm, float rate) {
		learn(learnTerm, modTerm, rate, true);
	}
	
	/**
	 * Sets learning parameters on learned terminations in the array.
	 * 
	 * @param learnTerm name of the learned termination
	 * @param modTerm name of the modulatory termination
	 * @param rate learning rate
	 * @param oja whether or not to use Oja smoothing
	 */
	public void learn(String learnTerm, String modTerm, float rate, boolean oja) {
		for(int i=0; i < myNumNodes; i++) {
			PESTermination term;
			try {
				term = (PESTermination)myNodes[i].getTermination(learnTerm);
			}
			catch(StructuralException se) {
				//term does not exist on this node
				term=null;
			}
			catch(ClassCastException se) {
				//term is not a PESTermination
				term=null;
			}
			
			if(term != null) {
				term.setLearningRate(rate);
				term.setOja(oja);
				term.setOriginName("X");
				term.setModTermName(modTerm);
			}
		}
		
	}
	
	/**
	 * Sets learning on/off for all ensembles in the network.
	 * 
	 * @param learn true if the ensembles are learning, else false
	 */
	public void setLearning(boolean learn) {
		for(int i=0; i < myNumNodes; i++)
			((PlasticEnsembleImpl)myNodes[i]).setLearning(learn);
	}
	
	/**
	 * Releases memory of all ensembles in the network.
	 */
	public void releaseMemory() {
		for(int i=0; i < myNumNodes; i++)
			myNodes[i].releaseMemory();
	}
	
	/**
	 * Returns the encoders for the whole network array (the encoders of each 
	 * population within the array concatenated together).
	 * 
	 * @return encoders of each neuron in the network array
	 */
	public float[][] getEncoders() {
		float[][] encoders = new float[myNeurons][myDimension];
		for(int i=0; i < myNumNodes; i++)
			MU.copyInto(myNodes[i].getEncoders(), encoders, i*myNeurons, i*myNodes[i].getDimension(), myNeurons);
		return encoders;
	}

	@Override
	public NetworkArrayImpl clone() throws CloneNotSupportedException {
		// Note: Cloning fails (so far) because arrayorigin takes network array as node reference, and attempting to 
		//       find a node with the same name as the network array inside the network array is impossible.
		// Note: Also need to clone exposed axons?
		try {
			NetworkArrayImpl result = (NetworkArrayImpl) super.clone();
			
			// Clone node references
			result.myNodes = new NEFEnsembleImpl[myNodes.length];
			for (int i = 0; i < myNodes.length; i++) {
				result.myNodes[i] = (NEFEnsembleImpl) result.getNode(myNodes[i].getName());
			}
			
			// Clone array origins and ensemble terminations
			for (Origin exposedOrigin : getOrigins()) {
				Origin clonedOrigin = ((OriginWrapper) exposedOrigin).getBaseOrigin().clone(result);
				result.exposeOrigin(clonedOrigin, exposedOrigin.getName());
			}
			for (Termination exposedTermination : getTerminations()) {
				Termination clonedTermination = ((TerminationWrapper) exposedTermination).getBaseTermination().clone(result);
				result.exposeTermination(clonedTermination, exposedTermination.getName());
			}
			
			return result;
		}
		catch (CloneNotSupportedException e) {
			System.err.println(e.getMessage());
			throw new CloneNotSupportedException("Error cloning NetworkArrayImpl: " + e.getMessage());
		}
		catch (StructuralException e) {
			System.err.println(e.getMessage());
			throw new CloneNotSupportedException("Error cloning NetworkArrayImpl: " + e.getMessage());
		}
	}
	
	/**
	 * Origin representing the concatenation of origins on each of the
	 * ensembles within the network array.
	 */
	public class ArrayOrigin extends BasicOrigin {

		private static final long serialVersionUID = 1L;
		
		private String myName;
		private NetworkArrayImpl myParent;
		private Origin[] myOrigins;
		private int myDimensions;

		public ArrayOrigin(NetworkArrayImpl parent, String name, Origin[] origins) {
			myParent = parent;
			myName = name;
			myOrigins = origins;
			myDimensions = 0;
			for(int i=0; i < myOrigins.length; i++)
				myDimensions += myOrigins[i].getDimensions();
		}
		
		public String getName() {
			return myName;
		}
		
		public int getDimensions() {
			return myDimensions;
		}
		
		public Origin[] getNodeOrigins(){
			return myOrigins;
		}
		
		public void setValues(InstantaneousOutput values) {
			float time = values.getTime();
			Units units = values.getUnits();

			float[] vals;
			if(values instanceof RealOutput)
				vals = ((RealOutput)values).getValues();
			else if(values instanceof PreciseSpikeOutput) {
				vals = ((PreciseSpikeOutput)values).getSpikeTimes();
			}
			else if(values instanceof SpikeOutput) {
				boolean[] spikes = ((SpikeOutput)values).getValues();
				vals = new float[spikes.length];
				for(int i=0; i < spikes.length; i++)
					vals[i] = spikes[i] ? 1.0f : 0.0f;
			}
			else {
				System.err.println("Unrecognized type in NetworkArrayImpl.setValues()");
				return;
			}
			
			int d=0;
			for(int i=0; i < myOrigins.length; i++) {
				float[] ovals = new float[myOrigins[i].getDimensions()];
				for(int j=0; j < ovals.length; j++)
					ovals[j] = vals[d++];
				
				if(values instanceof RealOutput)
					myOrigins[i].setValues(new RealOutputImpl(ovals, units, time));
				else if(values instanceof PreciseSpikeOutput) {
					myOrigins[i].setValues(new PreciseSpikeOutputImpl(ovals, units, time));
				}
				else if(values instanceof SpikeOutput) {
					boolean[] ospikes = new boolean[ovals.length];
					for(int j=0; j < ospikes.length; j++)
						ospikes[j] = vals[j] == 1.0 ? true : false;
					myOrigins[i].setValues(new SpikeOutputImpl(ospikes, units, time));
				}
				
			}
			
		}

		public InstantaneousOutput getValues() throws SimulationException {
			InstantaneousOutput v0 = myOrigins[0].getValues();
			
			Units unit = v0.getUnits();
			float time = v0.getTime();
			
			if(v0 instanceof PreciseSpikeOutputImpl) {
				float[] vals = new float[myDimensions];
				int d=0;
				for(int i=0; i < myOrigins.length; i++) {
					float[] ovals = ((PreciseSpikeOutputImpl)myOrigins[i].getValues()).getSpikeTimes();
					for(int j=0; j < ovals.length; j++)
						vals[d++] = ovals[j];
				}
				
				return new PreciseSpikeOutputImpl(vals, unit, time);
			} else if(v0 instanceof RealOutputImpl) {
				float[] vals = new float[myDimensions];
				int d=0;
				for(int i=0; i < myOrigins.length; i++) {
					float[] ovals = ((RealOutputImpl)myOrigins[i].getValues()).getValues();
					for(int j=0; j < ovals.length; j++)
						vals[d++] = ovals[j];
				}
				
				return new RealOutputImpl(vals, unit, time);
			} else if(v0 instanceof SpikeOutputImpl) {
				boolean[] vals = new boolean[myDimensions];
				int d=0;
				for(int i=0; i < myOrigins.length; i++) {
					boolean[] ovals = ((SpikeOutputImpl)myOrigins[i].getValues()).getValues();
					for(int j=0; j < ovals.length; j++)
						vals[d++] = ovals[j];
				}
				
				return new SpikeOutputImpl(vals, unit, time);
			} else {
				System.err.println("Unknown type in ArrayOrigin.getValues()");
				return null;
			}
		}
		
		public Node getNode() {
			return myParent;
		}
		
		public boolean getRequiredOnCPU() {
			for(int i=0; i < myOrigins.length; i++)
				if(myOrigins[i].getRequiredOnCPU())
					return true;
			return false;
		}
		
		public void setRequiredOnCPU(boolean req) {
			for(int i=0; i < myOrigins.length; i++)
				myOrigins[i].setRequiredOnCPU(req);
		}
		
		public ArrayOrigin clone() throws CloneNotSupportedException {
			//this is how it was implemented in networkarray, but I don't think it will work (myOrigins needs to be updated to the cloned origins)
			return new ArrayOrigin(myParent, myName, myOrigins);
		}
		
		public ArrayOrigin clone(Node node) throws CloneNotSupportedException {
			if( !(node instanceof NetworkArrayImpl) ){
				throw new CloneNotSupportedException("Error cloning ArrayOrigin: Invalid node type");
			}
			
			try {
				ArrayOrigin result = (ArrayOrigin) super.clone();
				
				DecodedOrigin[] origins = new DecodedOrigin[myOrigins.length];
				for (int i = 0; i < myOrigins.length; i++)
					origins[i] = (DecodedOrigin) ((NetworkArrayImpl) node).getNodes()[i].getOrigin(myOrigins[i].getName());
				result.myOrigins = origins;
				
				return result;
			} catch (StructuralException e) {
				throw new CloneNotSupportedException("Error cloning ArrayOrigin: " + e.getMessage());
			} catch (CloneNotSupportedException e) {
				throw new CloneNotSupportedException("Error cloning ArrayOrigin: " + e.getMessage());
			}
		}
		
		public float[][] getDecoders() {
			if(! (myOrigins[0] instanceof DecodedOrigin))
				return null;
			
			int neurons = myParent.getNeurons();
			float[][] decoders = new float[neurons*myOrigins.length][myDimensions];
			for(int i=0; i < myOrigins.length; i++) {
				MU.copyInto(((DecodedOrigin)myOrigins[i]).getDecoders(), decoders, i*neurons, i*myOrigins[i].getDimensions(), neurons);
			}
			return decoders;
		}

	}
}
