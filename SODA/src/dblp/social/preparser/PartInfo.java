package dblp.social.preparser;
/**
 * This class defines a structure used to memorize informations relative to a single _part file
 * Each generated _part file is described by its name, length and the number of inserted checkpoints (the checkpoint number 0 is also included)
 * @author Staffiero
 *
 */

public class PartInfo {
	private String filePath; 
	private long checkpoints; 
	private long length;
	
	/**
	 * The constructor to be used
	 * @param filePath: the _part file absolute path
	 * @param checkpoints: the number of checkpoints inserted into the _part file
	 * @param length: the _part file length
	 */
	public PartInfo(String filePath, long checkpoints, long length) {
		super();
		this.filePath = filePath;
		this.checkpoints = checkpoints;
		this.length = length;
	}
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filename) {
		this.filePath = filename;
	}
	public long getCheckpoints() {
		return checkpoints;
	}
	public void setCheckpoints(long checkpoints) {
		this.checkpoints = checkpoints;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	
}
