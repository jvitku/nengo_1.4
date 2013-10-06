/*
 * Created on 16-Dec-2010
 */
package ca.nengo.sim.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import junit.framework.TestCase;

import ca.nengo.model.Network;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.nef.NEFEnsemble;
import ca.nengo.model.nef.NEFEnsembleFactory;
import ca.nengo.model.nef.impl.NEFEnsembleFactoryImpl;
import ca.nengo.util.Probe;

/**
 * Unit tests for WriteToDiskSimulatorListener.
 * 
 * @author Trevor Bekolay
 */
public class WriteToDiskSimulatorListenerTest extends TestCase {

	/*
	 * Test method for 'ca.nengo.sim.impl.WriteToDiskSimulatorListener'
	 */
	public void testBasicWriting() throws StructuralException, SimulationException {
		Network network = new NetworkImpl();
		NEFEnsembleFactory factory = new NEFEnsembleFactoryImpl();
		NEFEnsemble ensemble = factory.make("Ensemble",100,2);
		File file = new File("testWTDSL.csv");
		
		network.addNode(ensemble);
		
		Probe probe = network.getSimulator().addProbe("Ensemble", "X", true);
		WriteToDiskSimulatorListener listener = new WriteToDiskSimulatorListener(file,probe,0.005f);
		
		network.getSimulator().addSimulatorListener(listener);
		network.getSimulator().run(0.0f, 1.0f, 0.001f);
		
		assertTrue(file.exists());
		assertTrue(file.length() > 0);
		assertTrue(file.delete());
	}
	
	/*
	 * Test method for 'ca.nengo.sim.impl.WriteToDiskSimulatorListener'
	 */
	public void testInterval() throws StructuralException, SimulationException, FileNotFoundException {
		Network network = new NetworkImpl();
		NEFEnsembleFactory factory = new NEFEnsembleFactoryImpl();
		NEFEnsemble ensemble = factory.make("Ensemble",100,2);
		File file = new File("testWTDSL.csv");
		
		network.addNode(ensemble);
		
		float interval = 0.005f;
		float simLength = 1.0f;
		int numIntervals = (int)(simLength / interval);
		Probe probe = network.getSimulator().addProbe("Ensemble", "X", true);
		WriteToDiskSimulatorListener listener = new WriteToDiskSimulatorListener(file,probe,interval);
				
		network.getSimulator().addSimulatorListener(listener);
		network.getSimulator().run(0.0f, simLength, 0.001f);
		
        Scanner fileReader = new Scanner(file);
        int lineCount = 0;
	    
        while (fileReader.hasNextLine()) {
        	fileReader.nextLine();
        	lineCount++;
        }
        
        assertEquals(numIntervals,lineCount);
        fileReader.close();
        network.getSimulator().removeSimulatorListener(listener);
        
        // Also tests multiple runs
        interval = 0.0f;
		numIntervals = 1000;
		listener = new WriteToDiskSimulatorListener(file,probe,interval);
		
		network.getSimulator().addSimulatorListener(listener);
		network.getSimulator().run(0.0f, simLength, 0.001f);
		
        fileReader = new Scanner(file);
        lineCount = 0;
	    
        while (fileReader.hasNextLine()) {
        	fileReader.nextLine();
        	lineCount++;
        }
        
        assertEquals(numIntervals,lineCount);
        fileReader.close();
        assertTrue(file.delete());
	}
}
