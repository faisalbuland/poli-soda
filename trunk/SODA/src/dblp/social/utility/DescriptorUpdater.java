package dblp.social.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import dblp.social.loader.PartFile;
/**
 * The main method of this class is used to save the parser status, it can be 
 * called in a separate process if the parser encounters an outOfMemoryError.
 * 
 * @author Staffiero
 *
 */
public class DescriptorUpdater {
	/**
	 * Saves a given parser status (checkpoint and offset of a specific 
	 * part file).
	 * 
	 * @param args: a String vector, it has to be built as follows:
	 * 		args[0]: the absolute path of the part file
	 * 		args[1]: the path of the descriptor file (the "_partsInfo.xml file")
	 * 		args[2]: the last reached checkpoint 
	 * 		args[3]: the last reached offset
	 * 		
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static void main(String[] args) throws FileNotFoundException,IOException, 
			NumberFormatException{
		File partFile = new File(args[0]);
		PartFile p = new PartFile();
		p.setFile(partFile);
		p.setPartFileDescriptor(args[1]);
		p.setLastCheckpoint(Integer.parseInt(args[2]));
		p.setLastOffset(Integer.parseInt(args[3]));
		p.updateDescriptor();
	}

}
