package ctu.nengoros.test.external.tools.io;

import ctu.nengoros.tools.io.AgentConfig;
import ctu.nengoros.tools.io.AgentSaver;

public class AgentSaverT {
	
	public static void main(String[] none){
		
		String name = "test.txt";
		AgentSaverT at = new AgentSaverT();
		at.testWrite(name);
		at.testRead(name);
		
	}

	private boolean testWrite(String name){
		AgentSaver as;
		as = new AgentSaver(name);

		as.save(0,new Float(1.111),new float[]{0,2,1,4,5,5});
		as.save(1,new Float(1),new float[]{0,2,1,4,5,5,7});
		as.close();
		System.out.println("done");
		return true;
	}
	
	private boolean testRead(String name){
		AgentSaver as;
		as = new AgentSaver(name);

		AgentConfig ac = as.load(1);
		System.out.println("i have got: g:"+ac.gen+" f:"+ac.fitness+" ac.genome:"+ac.genomeToString());
		
		ac = as.load(0);
		System.out.println("i have got: g:"+ac.gen+" f:"+ac.fitness+" ac.genome:"+ac.genomeToString());
		
		as.close();
		System.out.println("read done");
		return true;
	}
	
}
