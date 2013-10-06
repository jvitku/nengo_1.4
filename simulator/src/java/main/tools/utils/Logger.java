package tools.utils;

/**
 * implements very simple unbuffered (file) logger
 */

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {


	// logging
	private File outFile;
	private FileWriter flog;
	private String name;

	private boolean write = true; // you want to write at all?
	private boolean useConsole;   // use console now?
	private int level;
	
	public final int WARN = 1;
	public final int DEBUG = 9;
	public final int DEFLEV = 9;
	
	public Logger(){
		this.useConsole = true;
		this.write = true;
	}
	
	public Logger(String name, boolean useConsole, boolean write){

		// the lower number, the more important message! 
		this.level = 10;
		this.name = name;
		this.useConsole = useConsole;
		this.write = write;
		
		if(write && !useConsole)
			this.initFile();
	}

	
	/**
	 * creates instance of my simple Logger, 
	 * the logger uses console by default
	 * @param name - name of file where to write out logs
	 * note: writing to the file must be set using the printToFile 
	 * method
	 */
	public Logger(String name){

		// the lower number, the more important message! 
		this.level = 10;
		this.name = name;
		this.useConsole = false;
		
		if(write && !useConsole)
			this.initFile();
	}
	
	
	private void initFile(){
		// create file, write to it
		outFile = new File(name);
		// try to create new file writer with given name
		try {
			// append-true: append strings to the end of existing file
			flog= new FileWriter(outFile,false);
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void printToFile(boolean c){
		if(c && this.outFile == null){
			this.initFile();
		}
		this.useConsole = !c;
	}

	public void setLevel(int lev){ this.level = lev; }
	public int getLevel(){ return this.level; }
	public void setLogging(boolean bo){ this.write = bo; }

	private boolean write(String s, int lev){
		if(write){
			// if the message is important enough
			if(lev<=this.level){
				
				if(useConsole)
					System.out.print(/*"l"+lev+"| "+*/s);
				else
					try{
						flog.write(/*"l"+lev+"| "+*/s);
						flog.flush();
					}
				// return false if the strean could not be written! (waiting for stream avaibility)
					catch(IOException e){ /*e.printStackTrace();/**/ return false; }
			}
		}
		return true;
	}
	
	public void p(String cn, int lev, String s){
		this.write("lev: "+lev+" class "+cn+":\t"+s, lev);
	}
	
	public void pl(String cn, int lev, String s){
		this.write("lev: "+lev+" class "+cn+":\t"+s+"\n", lev);
	}
	
	public void p(int LEV, String s){
		this.write(s, LEV);
	}
	
	public boolean pl(int LEV, String s){
		return this.write(s+"\n", LEV);
	}

	public void p(String s){
		this.write(s, this.DEFLEV);
	}
	
	public boolean pl(String s){
		return this.write(s+"\n", this.DEFLEV);
	}
	
	public void err(String cn, String s){
		this.write("ERROR: class "+cn+":\t"+s+"\n", 0);
	}
	
	public void err(String s){
		this.write("ERROR:\t"+s+"\n", 0);
	}
	
	public void end(){
		if(write && this.outFile!= null){
			try {
				//flog.write("Closing the output stream, bye");
				flog.flush();
				flog.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * print array
	 * @param LEV
	 * @param inds
	 */
	public void pa(int LEV, int[] inds){
		this.write(LU.toStr(inds)+"\n", LEV);
	}
	
	public void pa(int LEV, float[] inds){
		this.write(LU.toStr(inds)+"\n", LEV);
	}
	
	public static String getUniqueName(){
		DateFormat dateFormat = new SimpleDateFormat("dd.MM_HH-mm-ss");
        Date date = new Date();
        String dt = dateFormat.format(date);
        return "_"+ dt;
	}
	
}

