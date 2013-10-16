package nengoros.util;

import ctu.nengoros.util.sync.impl.SyncedUnit;
import static org.junit.Assert.*;
public class SyncHierarchyTest {

	public static void main(String[] args){
		System.out.println(" will init some hierarchy of Units and test if it works");
		SyncHierarchyTest st = new SyncHierarchyTest();

		System.out.println("test A");
		st.testA();
		System.out.println("test B");
		st.testB();
		System.out.println("test C");
		st.testC();
		
		System.out.println("done");
	}
	
	public void testA(){
		Something nochilds = new Something(); 
		nochilds.setReady(true);
		assertTrue(nochilds.isReady());
		nochilds.setReady(false);
		assertFalse(nochilds.isReady());
		
		nochilds.setSynchronous(false);
		assertTrue(nochilds.isReady());
	}
	
	public void testB(){
		// init
		Something a = new Something();
		
		Something b = new Something();
		Something c = new Something(false);
		
		Something d = new Something();
		Something e = new Something(false);
		
		// define the hierarchy
		a.addChild(b);
		a.addChild(c);
		b.addChild(e);
		b.addChild(d);
		
		
		// test
		assertTrue(a.isReady());
		
		assertTrue(c.isReady());
		c.setReady(false);
		assertTrue(c.isReady());
		
		d.setReady(false);
		assertFalse(a.isReady());
		assertFalse(d.isReady());
		assertFalse(b.isReady());
		
		b.removeChild(d);
		
		assertTrue(b.isReady());
		assertTrue(a.isReady());
		
	}
	
	
	// test discardChildsReady
	public void testC(){
		// init
		Something a = new Something();
		
		Something b = new Something();
		Something c = new Something(false);
		
		Something d = new Something();
		Something e = new Something(false);
		
		// define the hierarchy
		a.addChild(b);
		a.addChild(c);
		b.addChild(e);
		b.addChild(d);
		
		
		// test
		assertTrue(a.isReady());
		
		b.discardChildsReady();
		
		assertFalse(a.isReady());	//parent-some child(s) not ready
		assertFalse(b.isReady());	//me-discarded
		assertFalse(d.isReady());	//discarded
		assertTrue(e.isReady());	//asynchronous - always ready
		
		// this is how it is supposed to work!!!!
		d.setReady(true);
		// all parents should be ready now!
		assertTrue(a.isReady());	//parent-all child(s) are ready => I am to
		assertTrue(b.isReady());	//me - ready because d became ready (e is always)
		assertTrue(d.isReady());	//
		assertTrue(e.isReady());	//asynchronous - always ready
		
	}
	
	
	protected class Something extends SyncedUnit{

		public Something(){
			super();
		}
		
		public Something(boolean synchronous) {
			super(synchronous);
		}
	}
	
}
