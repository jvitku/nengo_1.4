package ctu.nengorosHeadless.simulator.impl;


import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ctu.nengorosHeadless.network.connections.Connection;
import ctu.nengorosHeadless.network.connections.InterLayerWeights;
import ctu.nengorosHeadless.network.connections.impl.BasicInterLayerWeights;
import ctu.nengorosHeadless.network.connections.impl.ReferencedInterlayerConnection;
import ctu.nengorosHeadless.network.modules.io.Orig;
import ctu.nengorosHeadless.network.modules.io.Term;

public abstract class AbstractLayeredSimulator extends AbstractSimulator {

	protected InterLayerWeights[] interlayers;
	
	public boolean randomize = false;

	public AbstractLayeredSimulator(int noInterlayerConnections){
		super();
		
		interlayers = new BasicInterLayerWeights[noInterlayerConnections];
		for(int i=0; i<interlayers.length; i++){
			interlayers[i] = new BasicInterLayerWeights();
		}
	}
	
	public void makeStep(){
		//System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx step no "+t+" stert");
		for(int i=0; i<connections.size(); i++){
			connections.get(i).transferData();
		}
		for(int i=0; i<nodes.size(); i++){
			try {
				// run all Origins/Encoders encode message and send to own modem
				nodes.get(i).run(t, t+dt);
			} catch (SimulationException e) {
				System.err.println("ERROR: Node named: "+nodes.get(i).getFullName() + "thrown simulatino exception!");
				e.printStackTrace();
			}
		}
		// wait for all responses
		this.awaitAllReady();
		//System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx step no "+t+" end");
	}

	@Override
	public abstract void defineNetwork();

	/*
	
	@Override
	public Connection connect(Orig o, Term t, int interLayerNo) throws StructuralException{
		if(o==null){
			throw new StructuralException("Orig o is null, ignoring this connection!");
		}
		if(t==null){
			throw new StructuralException("Term t is null, ignoring this connection!");
		}
		if(interLayerNo < 0 || interLayerNo >= this.interlayers.length){
			throw new StructuralException("index of interLayer is out of range, max is: "+(this.interlayers.length-1));
		}
		
		Connection c = new BasicInterLayerConnection(o,t, interlayers[interLayerNo]); 
		this.connections.add(c);
		return c;
	}*/

	@Override
	public Connection connect(Orig o, Term t, int interLayerNo) throws StructuralException{
		if(o==null){
			throw new StructuralException("Orig o is null, ignoring this connection!");
		}
		if(t==null){
			throw new StructuralException("Term t is null, ignoring this connection!");
		}
		if(interLayerNo < 0 || interLayerNo >= this.interlayers.length){
			throw new StructuralException("index of interLayer is out of range, max is: "+(this.interlayers.length-1));
		}
		
		Connection c = new ReferencedInterlayerConnection(o,t, interlayers[interLayerNo]);
		this.connections.add(c);
		return c;
	}
	
	@Override
	public void makeFullConnections(int interLayerNo) throws StructuralException{
		if(interLayerNo < 0 || interLayerNo >= this.interlayers.length){
			throw new StructuralException("index of interLayer is out of range, max is: "+(this.interlayers.length-1));
		}
		// build full connections in a given interLayer
		Connection[] newOnes = this.interlayers[interLayerNo].makeFullConnections();
		
		// register them all to the other ones
		for(int i=0; i<newOnes.length; i++){
			this.connections.add(newOnes[i]);
		}
	}
	
	@Override
	public void setLogToFile(boolean file) {
		// TODO :(
	}

	public void designFinished(){
		for(int i=0; i<this.interlayers.length; i++){
			this.interlayers[i].designFinished();
		}
	}

	public void registerOrigin(Orig o, int interLayerNo) throws StructuralException{
		if(o==null){
			throw new StructuralException("Orig o is null, ignoring this connection!");
		}
		if(interLayerNo < 0 || interLayerNo >= this.interlayers.length){
			throw new StructuralException("index of interLayer is out of range, max is: "+(this.interlayers.length-1));
		}
		this.interlayers[interLayerNo].addOrigin(o);
	}
	
	public void registerTermination(Term t, int interLayerNo) throws StructuralException{
		if(t==null){
			throw new StructuralException("Term t is null, ignoring this connection!");
		}
		if(interLayerNo < 0 || interLayerNo >= this.interlayers.length){
			throw new StructuralException("index of interLayer is out of range, max is: "+(this.interlayers.length-1));
		}
		this.interlayers[interLayerNo].addTermination(t);
	}
	
}
