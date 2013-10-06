package nengoros.nodeFactory;

import static org.junit.Assert.*;

import nengoros.comm.nodeFactory.NameProvider;

import org.junit.Test;

/**
 * My first attempt to write unit test.. and it found at least 5 bugs actually..
 * 
 * @author Jaroslav Vitku
 *
  http://supportweb.cs.bham.ac.uk/documentation/tutorials/docsystem/build/tutorials/junit/junit.html
 */
public class NameProviderTest/*extends TestCase*/{

	/**
	 * @param name name of the test
	 *
	public NameProviderTest(String name){
		super(name);
	}*/
	
	@Test
	public void startEmpty(){
		NameProvider np = new NameProvider();
		assertEquals(np.numOfRunningNodes(),0);
	}
	
	@Test
	public void startStopNode_NS(){
		NameProvider np = new NameProvider();
		String ns = "SmartNeuron";
		String[] node = new String[]{"testNode1"};
		String x = np.findNamespace(node, ns);
		assertTrue(x.equalsIgnoreCase(ns));
		
		assertEquals(np.numOfRunningNodes(),1);
		np.shutDown(new String[]{x+np.separator+node[0]});
		assertEquals(np.numOfRunningNodes(),0);		
	}
	
	@Test
	public void startStopNode_NoNS(){
		NameProvider np = new NameProvider();
		String ns = "SmartNeuron";
		String[] name = new String[]{"testNode1"};
		String[] x = np.modifyNames(null, name, ns);
		assertEquals(x.length,1);
		assertTrue(x[0].equalsIgnoreCase(ns+np.separator+name[0]));
		
		assertEquals(np.numOfRunningNodes(),1);
		np.shutDown(x);
		assertEquals(np.numOfRunningNodes(),0);
	}
	
	@Test
	public void startStopNodes_NS(){
		NameProvider np = new NameProvider();
		String ns = "SmartNeuron";
		String[] names = new String[]{"testNode1", "testNode2"};
		String x = np.findNamespace(names, ns);
		assertEquals(x,ns);
		assertEquals(np.numOfRunningNodes(),2);
		np.shutDown(names, x);
		assertEquals(np.numOfRunningNodes(),0);
	}
	
	@Test
	public void startStopNodes_NoNS(){
		NameProvider np = new NameProvider();
		String ns = "SmartNeuron";
		String[] names = new String[]{"testNode1", "testNode2"};
		String[] x = np.modifyNames(null, names, ns);
		assertTrue(x[0].equalsIgnoreCase(ns+np.separator+names[0]));
		assertTrue(x[1].equalsIgnoreCase(ns+np.separator+names[1]));
		
		assertEquals(np.numOfRunningNodes(),2);
		np.shutDown(ns+np.separator+names[0]);
		assertEquals(np.numOfRunningNodes(),1);
		np.shutDown(ns+np.separator+names[1]);
		assertEquals(np.numOfRunningNodes(),0);
	}
	
	@Test
	public void modifyNames(){
		NameProvider np = new NameProvider();
		String ns = "SmartNeuron";
		String[] names = new String[]{"modem", "workNode"};
		String[] x = np.modifyNames(null, names, ns);
		// there was no conflict, so the same:
		assertTrue(x[0].equalsIgnoreCase(ns+np.separator+names[0]));
		assertTrue(x[1].equalsIgnoreCase(ns+np.separator+names[1]));
		
		// launching a smartNeuron with identical name(s)
		String[] y = np.modifyNames(null, names, ns);
		// NameProvider should have changed the namespace to st. else
		assertFalse(y[0].equalsIgnoreCase(ns+np.separator+names[0]));
		assertFalse(y[1].equalsIgnoreCase(ns+np.separator+names[1]));
		
		// just to be sure where the change is..
		assertFalse(y[0].equalsIgnoreCase(x[0]));
		assertFalse(y[1].equalsIgnoreCase(x[1]));
		// running 4 nodes now..
		assertEquals(np.numOfRunningNodes(),4);
	}
	
	@Test
	public void modifyNS(){
		NameProvider np = new NameProvider();
		String ns = "SmartNeuron";
		String[] names = new String[]{"modem", "workNode"};
		String nns = np.findNamespace(names, ns);
		// no conflict, NameProvider whould let the namespace unmodified
		assertTrue(nns.equalsIgnoreCase(ns));

		String nnns = np.findNamespace(names, ns);
		// now there was conflict (another identical neuron) so nnns is changed
		assertFalse(nnns.equalsIgnoreCase(nns));
		// running 4 nodes now..
		assertEquals(np.numOfRunningNodes(),4);
	}
	
	
	
}
