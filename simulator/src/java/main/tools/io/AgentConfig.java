package tools.io;

/**
 * Holds info about generation, fitness and genome
 * 
 * @author j
 */
public class AgentConfig{
	public int gen;
	public float fitness;
	public float[] genome;
	
	public AgentConfig(String config){
		String[] s = config.split(",");
		if(s.length <3 ){
			System.err.println("AC: found line with too few numbers");
			this.error();
			return;
		}
		gen = Integer.parseInt(s[0]);
		fitness = Float.parseFloat(s[1]);
		this.parseGenome(s);
	}
	private void parseGenome(String[] s){
		this.genome = new float[s.length-2];
		
		for(int i=2; i<s.length; i++){
			genome[i-2]=Float.parseFloat(s[i]);
		}
	}
	
	private void error(){
		gen=-1;
		fitness = -1;
		genome = new float[]{};
	}
	
	public String genomeToString(){
		if(genome.length<1)
			return null;
		String out= "";
		for(int i=0; i<genome.length-1; i++)
			out= out +genome[i]+", ";
		out = out +genome[genome.length-1];
		return out;
	}
	
	public float[] getRange(int from, int to){
		
		float[] vector = this.genome;
		int range = to-from;
		System.out.println("range requested is from to range: "+from+" "+to+" "+range);
		System.out.println("and vec len is: "+vector.length);
		float[] out = new float[range];

		for(int i=0; i<range; i++)
			out[i] = vector[from+i];

		return out;
	}
}