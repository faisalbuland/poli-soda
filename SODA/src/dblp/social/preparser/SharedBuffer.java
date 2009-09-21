package dblp.social.preparser;
/**
 * A synchronized shared buffer in which a pre parser thread can write the name of the
 * last generated _part file. A concurrent thread can read the file name in order to display to the user the current progress.
 * 
 * @author staffiero
 *
 */

public class SharedBuffer {
	private String bufferContent = "";
	private boolean readBufferContent = false;
	
	public static final String DONE = "Pre parse done";
	
	public synchronized void set(String value){
		this.bufferContent = value;
		this.readBufferContent = true;
		notify(); 
	}
	
	public synchronized String get(){
		while (this.readBufferContent==false){
			try{
				wait();
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		this.readBufferContent=false;
		return this.bufferContent;
	}
	
	public void releaseMonitor(){
		this.readBufferContent=true;
		this.bufferContent = DONE;
	}
}
