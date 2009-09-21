package dblp.social.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is an in-memory structure which contains informations on a single _partFile.
 * Each _partFile is the result of a previous execution of the pre parser 
 * @author staffiero
 *
 */
public class PartFile {
	private String partFileDescriptor;
	private String source;
	private File file; 
	private long checkpoints;
	private int checkpointsStep;
	private int lastCheckpoint;
	private int lastOffset;
	private boolean complete;
	private boolean updated=true;
	
	/**
	 * Empty constructor
	 */
	public PartFile(){}
	
	/**
	 * The constructor to be used
	 * 
	 * @param sourcePath the path of the source dblp xml file
	 * @param filePath the absolute path of the _partFile
	 * @param checkpoints the number of checkpoints in the _partFile
	 * @param checkpointsStep the checkpoints step (the number of "main" dblp tags between two checkpoints)
	 */
	public PartFile(String sourcePath, String filePath, long checkpoints, int checkpointsStep) {
		super();
		this.source = sourcePath;
		this.file = new File(filePath);
		this.checkpoints = checkpoints;
		this.checkpointsStep = checkpointsStep;
		this.updated = true;
	}
	
	/**
	 * Getter
	 * @return the path of the source dblp xml file
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Setter, sets the path of the source dblp xml file
	 * @param source
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * Getter
	 * @return a File object which points to the _partFile
	 */
	public File getFile() {
		return file;
	}
	/**
	 * Setter, sets the File object which points to the _partFile
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
	}
	/**
	 * Getter
	 * @return: the checkpoints number
	 */
	public long getCheckpoints() {
		return checkpoints;
	}
	/**
	 * Setter, sets the checkpoints number
	 * @param checkpoints
	 */
	public void setCheckpoints(long checkpoints) {
		this.checkpoints = checkpoints;
	}
	/**
	 * Getter
	 * @return: the checkpoints step
	 */
	public int getCheckpointsStep() {
		return checkpointsStep;
	}
	/**
	 * Setter, sets the checkpoints step
	 * @param checkpointsStep
	 */
	public void setCheckpointsStep(int checkpointsStep) {
		this.checkpointsStep = checkpointsStep;
	}
	/**
	 * Getter
	 * @return the last reached checkpoint
	 */
	public int getLastCheckpoint() {
		return lastCheckpoint;
	}
	/**
	 * Setter, sets the last reached checkpoint
	 * @param lastCheckpoint
	 */
	public void setLastCheckpoint(int lastCheckpoint) {
		this.lastCheckpoint = lastCheckpoint;
	}
	/**
	 * Getter
	 * @return the last reached offset
	 */
	public int getLastOffset() {
		return lastOffset;
	}
	/**
	 * Setter, sets the last reached offset
	 * @param lastOffset
	 */
	public void setLastOffset(int lastOffset) {
		this.lastOffset = lastOffset;
	}
	/**
	 * Getter
	 * @return true if the _partFile has been completely parsed, false otherwise
	 */
	public boolean isComplete() {
		return complete;
	}
	/**
	 * Setter, sets the boolean value which indicates if the _partFile has been completely parsed
	 * @param complete
	 */
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	/**
	 * Getter
	 * @return the path of the partsInfo xml file
	 */
	public String getPartFileDescriptor() {
		return partFileDescriptor;
	}
	/**
	 * Setter, sets the path of the partsInfo xml file
	 * @param partFileDescriptor
	 */
	public void setPartFileDescriptor(String partFileDescriptor) {
		this.partFileDescriptor = partFileDescriptor;
	}
	/**
	 * Getter. When the current parse status is saved the part file is considered updated if and only if the saved status (last checkpoint, last offset) is greater than the previous stored status
	 * @return true if the partFile has been updated, false otherwise
	 */
	public boolean hasBeenUpdated(){
		return this.updated;
	}
	/**
	 * Setter, sets a boolean variable which indicates if this part file has been updated.
	 * @param updated
	 */
	public void setUpdated(boolean updated){
		this.updated=updated;
	}
	
	
	/**
	 * Updates the xml _partsInfo file with the informations stored in this object.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void updateDescriptor() throws IOException, FileNotFoundException{
		/*
		 * Reads the previous state of the part file progress (last checkpoint && last offset), 
		 * if the current state is > of the previous
		 * 	updates the part file descriptor and sets this.updated=true
		 * else
		 * 	doesn't update the part file descriptor and sets this.updated=false
		 */
		File descriptor = new File(this.partFileDescriptor);
		//reads the whole descriptor file
		BufferedReader input = 
			new BufferedReader(new FileReader(descriptor));
		StringBuffer buffer = new StringBuffer();
		String text;
		int previousLastCheckpoint=0;
		int previousLastOffset=0;
		if (descriptor.exists()){
			while ((text = input.readLine() ) != null){
				if (text.contains("<path>"+file.getAbsolutePath()+"</path>")){
					buffer.append(text+"\n");
					text = input.readLine(); //string containing the number of checkpoints
					buffer.append(text+"\n");
					text = input.readLine(); //string containing last checkpoint
					previousLastCheckpoint = Integer.parseInt(cleanString(text));
					buffer.append("\t\t<last-checkpoint>"+this.lastCheckpoint+"</last-checkpoint>\n");
					text = input.readLine(); //string containing last offset
					previousLastOffset = Integer.parseInt(cleanString(text));
					buffer.append("\t\t<last-offset>"+this.lastOffset+"</last-offset>\n");
					text = input.readLine(); //string containing complete tag
					String complete = "";
					if (this.complete)
						complete="true";
					else		
						complete="false";
					buffer.append("\t\t<complete>"+complete+"</complete>\n");
					//end of mods
				}	
				else
					buffer.append(text+"\n");
			}		
			if (previousLastCheckpoint< this.lastCheckpoint || 
					(previousLastCheckpoint== this.lastCheckpoint &&
							previousLastOffset<this.lastOffset)){
				this.updated=true;
			}
			else{
				this.updated=false;
			}
			input.close();
			descriptor.delete();
			FileWriter output = 
				new FileWriter(descriptor);
			//rewrites the whole descriptor file
			output.write(buffer.toString());
			output.close();
		}
		else
			throw new FileNotFoundException();
		
		
	}
	
	/**
	 * Cleans a string containing a number before casting the number with the static method Integer.parseInt()
	 * 
	 * @param in the string to be cleaned
	 * @return the cleaned string
	 */
	protected static String cleanString(String in){
		String out="";
		out = in.replaceAll("<last-checkpoint>", "");
		out = out.replaceAll("</last-checkpoint>", "");
		out = out.replaceAll("<last-offset>", "");
		out = out.replaceAll("</last-offset>", "");
		out = out.replaceAll("\t", "");
		out = out.replaceAll("\n", "");
		return out;
	}

}
