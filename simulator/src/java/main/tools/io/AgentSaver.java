package tools.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import tools.utils.U;

/**
 * Is able to save and load agent configurations made by EA e.g., holds fitness, generation and genome.
 * Note that number of generations stored should be unique.
 * 
 * NoteII: this works only for files in the current directory from Jython!!! (so do not use sub-directories)
 * @author j
 *
 */
public class AgentSaver {

	private final String name;
	BufferedWriter bw;
	File file;
	FileWriter fw;

	public AgentSaver(String filename){
		//name =System.getProperty("user.dir")+"/"+filename;
		name =filename;
	}
	
	private void initFile(){
		file =new File(name);

		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//true = append file
		try {
			fw = new FileWriter(file.getName(),true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		bw = new BufferedWriter(fw);
	}

	/**
	 * Save agent into file
	 * @param gen
	 * @param fitness
	 * @param genome
	 * @throws IOException
	 */
	public void save(int gen, float fitness, float[] genome){
		this.initFile();
		String wr = dataToString(gen, fitness, genome);
		try {
			bw.write(wr);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String dataToString(int gen, float fitness, float[] genome){
		String out = String.format("%d,%f,",gen,fitness);
		for(int i=0; i<genome.length-1; i++)
			out = out+String.format("%f,", genome[i]);
		out = out+String.format("%f", genome[genome.length-1]);
		return out;
	}

	/**
	 * Will return the first agent found with the specified generation 
	 * @param which generation when the agent has been saved
	 * @return agent configuration (generation, fitness, genome)
	 * @throws IOException 
	 */
	public AgentConfig load(int which){
		BufferedReader br;
		AgentConfig ac = null;
		try {
			FileInputStream fstream = new FileInputStream(name);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			//br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				ac = new AgentConfig(line);
				if(ac.gen == which){
					br.close();
					return ac;
				}
			}
			System.err.println("Configuration of agent from generation: "+which+" not found in: "+name);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ac;
	}

	public void close(){
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

