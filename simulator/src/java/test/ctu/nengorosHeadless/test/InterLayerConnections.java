package ctu.nengorosHeadless.test;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import ca.nengo.model.StructuralException;
import ctu.nengorosHeadless.network.connections.InterLayerWeights;
import ctu.nengorosHeadless.network.connections.impl.BasicInterLayerWeights;
import ctu.nengorosHeadless.network.connections.impl.IOGroup;

public class InterLayerConnections {

	//@Ignore
	@Test
	public void BasicConnect() {

		int sourceDim, targetDim;
		Random r = new Random();

		for(int i=0; i<200; i++){
			sourceDim = r.nextInt(1000);
			targetDim = r.nextInt(1000);

			this.simpleConnect(sourceDim, targetDim);	
		}
	}
	
	//@Ignore
	@Test
	public void BasicSetWeights() {

		int sourceDim, targetDim;

		Random r = new Random();
		sourceDim = r.nextInt(10)+1;
		targetDim = r.nextInt(10)+1;

		InterLayerWeights iw = new BasicInterLayerWeights();
		
		
		IOGroup[] iog = iw.addConnection(sourceDim, targetDim);
		IOGroup input = iog[0];
		IOGroup output = iog[1];
		
		iw.designFinished();

		this.checkConnection(input, output, iw);
	}


	@Test
	public void MultipleSetWeights() {

		InterLayerWeights iw = new BasicInterLayerWeights();
				
		IOGroup[] iog = iw.addConnection(2, 3);
		IOGroup input = iog[0];
		IOGroup output = iog[1];

		IOGroup[] iog2 = iw.addConnection(4, 5);
		IOGroup input2 = iog2[0];
		IOGroup output2 = iog2[1];
		
		iw.designFinished();

		this.checkConnection(input, output, iw);
		this.checkConnection(input2, output2, iw);
	}

	
	public void checkConnection(IOGroup input, IOGroup output, InterLayerWeights iw){

		float[][] w, wOrig;
		Random r = new Random();

		/*
		System.out.println("tessting connection ------------------ sizes are: "+
		input.getNoUnits()+" "+output.getNoUnits()+" indexes are: "+input.getMyIndex()+" "+output.getMyIndex()
		+" starting positions: "+input.getStartingIndex()+" "+output.getStartingIndex()
		+" no units are: "+input.getNoUnits()+" "+output.getNoUnits());
		*/
		
		try {
			w = iw.getWeightsBetween(input.getMyIndex(), output.getMyIndex());

			assertTrue(w.length == input.getNoUnits());
			assertTrue(w[0].length == output.getNoUnits());

			wOrig = new float[w.length][w[0].length];

			for(int i=0; i<w.length; i++){
				for(int j=0; j<w[0].length; j++){

					wOrig[i][j] = r.nextFloat();
					w[i][j] = wOrig[i][j];
				}
			}

			iw.setWeightsBetween(input.getMyIndex(), output.getMyIndex(), w);
			float[][] x = iw.getWeightsBetween(input.getMyIndex(), output.getMyIndex());

			for(int i=0; i<w.length; i++){
				for(int j=0; j<w[0].length; j++){

					System.out.println(w[i][j]+" == "+wOrig[i][j]+" == "+x[i][j]);

					assertTrue( w[i][j] == wOrig[i][j] );
					assertTrue( x[i][j] == wOrig[i][j] );
				}
			}
		} catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}
	}

	//@Ignore
	@Test
	public void TwoStageConnect(){

		int sourceDimA = 3;
		int targetDimA = 2;

		int sourceDimB = 5;
		int targetDimB = 6;

		InterLayerWeights iw = this.simpleConnect(sourceDimA, targetDimA);

		IOGroup[] iog = iw.addConnection(sourceDimB, targetDimB);
		IOGroup input = iog[0];
		IOGroup output = iog[1];

		assertTrue(input.getMyIndex() == 1);
		assertTrue(output.getMyIndex() == 1);

		assertTrue(input.getNoUnits() == sourceDimB);
		assertTrue(output.getNoUnits() == targetDimB);

		assertTrue(input.getStartingIndex()==sourceDimA);
		assertTrue(output.getStartingIndex()==targetDimA);
	}

	private InterLayerWeights simpleConnect(int sourceDim, int targetDim){
		InterLayerWeights iw = new BasicInterLayerWeights();

		IOGroup[] iog = iw.addConnection(sourceDim, targetDim);
		IOGroup input = iog[0];
		IOGroup output = iog[1];


		assertTrue(input.getMyIndex() == 0);
		assertTrue(output.getMyIndex() == 0);

		assertTrue(input.getNoUnits() == sourceDim);
		assertTrue(output.getNoUnits() == targetDim);

		assertTrue(input.getStartingIndex()==0);
		assertTrue(output.getStartingIndex()==0);
		return iw;
	}
	
	
	@Test
	public void GetSetVector() {

		InterLayerWeights iw = new BasicInterLayerWeights();
				
		IOGroup[] iog = iw.addConnection(2, 1);
		IOGroup input = iog[0];
		IOGroup output = iog[1];

		IOGroup[] iog2 = iw.addConnection(3, 3);
		IOGroup input2 = iog2[0];
		IOGroup output2 = iog2[1];
		
		iw.designFinished();

		this.checkConnection(input, output, iw);
		this.checkConnection(input2, output2, iw);
		
		float[] v = iw.getVector();
		
		float[] w = new float[v.length];
		Random r = new Random();
		for(int i=0; i<w.length; i++){
			w[i] = r.nextFloat();
		}
		
		try {
			iw.setVector(w);
			v = iw.getVector();
			assertTrue(v.length == w.length);
			for(int i=0; i<v.length; i++){
				assertTrue(v[i] == w[i]);
			}
			
		} catch (StructuralException e) {
			e.printStackTrace();
			fail();
		}
		
	}
}

