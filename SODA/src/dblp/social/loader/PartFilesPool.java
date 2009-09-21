package dblp.social.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import dblp.social.exceptions.PoolException;

/**
 * This class defines a pool of PartFiles object. 
 * 
 * @author staffiero
 *
 */
public class PartFilesPool {
	private static PartFilesPool pool=null;
	private ArrayList<PartFile> partFiles, parsedFiles, availableFiles;
	private int lastGivenIndex;
	
	private static Logger logger = Logger.getLogger(PartFilesPool.class);
	private boolean accessAllowed;
	
	/**
	 * Private constructor used by the static method initialize
	 * 
	 * @param descriptorFilePath: the absolute path of the _partsInfo xml file
	 * @throws SAXException
	 * @throws IOException
	 */
	private PartFilesPool(String descriptorFilePath) throws SAXException, IOException{
		File descriptorFile = new File(descriptorFilePath);
		DescriptorLoader dl = new DescriptorLoader(descriptorFile);
		dl.parseXmlDescriptor();
		this.partFiles =  dl.getPartFiles();
		this.availableFiles = this.partFiles;
		this.parsedFiles = new ArrayList<PartFile>();
		this.lastGivenIndex=-1;
		this.accessAllowed = true;
		
//		//TODO Debug code
//		System.out.println("Part files pool initialized.\nFile(s) in the pool:\n");
//		for (PartFile p : this.availableFiles){
//			System.out.println(p.getFile().getAbsolutePath());
//		}
//		System.out.println("");
	}
	/**
	 * Static method which initializes the pool. Only one pool is allowed, therefore a second call to this method will not affect an existing pool.
	 * 
	 * @param descriptorFilePath: the absolute path of the _partsInfo xml file
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void initialize(String descriptorFilePath) throws SAXException, IOException{
		if (pool==null)
			pool = new PartFilesPool(descriptorFilePath); 
	}
	/**
	 * Static method which returns an instance of PartFilesPool
	 * @return an instance of PartFilesPool
	 * @throws PoolException
	 */
	public static PartFilesPool getInstance() throws PoolException{
		if (pool!=null)
			return pool;
		else 
			throw new PoolException("Pool not initialized");
	}
	
	/**
	 * To call to get a PartFile object from the pool
	 * 
	 * @return a PartFile object from the pool, null if the pool has no available part files
	 */
	public synchronized PartFile getPartFile(){
		while (!this.accessAllowed){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.accessAllowed=false;
		if (this.availableFiles.size()==0){
			this.accessAllowed=true;
			return null;
		}
		int index= indexGenerator();
		PartFile pf = this.availableFiles.get(index);
		this.availableFiles.remove(index);
		pf.setUpdated(false);
		this.accessAllowed=true;
		return pf;
	}
	/**
	 * A custom comparator to order partFiles objects within a pool.
	 * Part files will be ordered according to their sequence number, thus sorting a collection of PartFiles with this comparator the partFile whose filename is xxx_part1.xml will be positioned before the partFile whose filename is xxx_part2.xml.
	 * 
	 * @author staffiero
	 *
	 */
	public class MyComparator implements Comparator<PartFile>{

		@Override
		public int compare(PartFile p1, PartFile p2) {
			if (p1!=null && p2!=null){
				String n1 = p1.getFile().getName();
				n1= n1.substring(n1.lastIndexOf("t")+1, n1.lastIndexOf("."));
				String n2 = p2.getFile().getName();
				n2= n2.substring(n2.lastIndexOf("t")+1, n2.lastIndexOf("."));
				int index1 = Integer.parseInt(n1);
				int index2 = Integer.parseInt(n2);
				if (index1==index2)
					return 0;
				else if (index1< index2)
					return-1;
				else
					return 1;
			}
			else if (p2==null)
				return 1;
			return 0;
		}
		
	}
	/**
	 * To be called if parser fails to parse a partFile or terminates before reaching the end of the file.
	 * 
	 * @param partFile
	 */
	public synchronized void giveBack(PartFile partFile){
		while (!this.accessAllowed){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.accessAllowed=false;
		this.availableFiles.add(0, partFile);
		if (this.availableFiles.size()>1)
			Collections.sort(this.availableFiles,new MyComparator());
		logger.debug("Part File "+partFile.getFile().getName()+" sent back to its pool");
		this.accessAllowed=true;
	}
	
	/**
	 * Provides the number of the pool objects
	 * @return the number of the pool objects
	 */
	public int poolObjectsNumber(){
		return this.partFiles.size();
	}
	
	
	/**
	 * Provides the number of available objects
	 * @return the number of available objects
	 */
	public int availableObjectsNumber(){
		return this.availableFiles.size();
	}
	
	/**
	 * To call to know if all partFiles have been completely parsed
	 * 
	 * @return true if all partFiles have been parsed, false otherwise
	 */
	public boolean totalParseFinished(){
		if (this.partFiles.size()==this.parsedFiles.size())
			return true;
		else
			return false;
	}
	
	/**
	 * To be called when the parse of a PartFile is finished without errors.
	 * 
	 * @param partFile
	 */
	public synchronized void parseFinished(PartFile partFile){
		while (!this.accessAllowed){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.accessAllowed=false;
		this.partFiles.remove(partFile);
		this.parsedFiles.add(partFile);
		this.accessAllowed=true;
	}
	
	/**
	 * To be called to know if there is at least one part file in the pool which has been updated
	 * 
	 * @return true if exists at least one part file in the pool which has been updated, false otherwise
	 */
	public boolean hasBeenUpdated(){
		boolean result=false;
		for (PartFile p : this.availableFiles){
			result = result || p.hasBeenUpdated();
		}
		return result;
	}
	
	/**
	 * Generate indexes trying to avoid to have two indexes one next to the other.
	 * This is useful to minimize collisions during parse, for example two threads which update the same book (or proceedings)
	 * @return index
	 */
	//Returns "distant" part files to minimize collisions among threads
	private int indexGenerator(){
		int idx=0;
		if (this.lastGivenIndex==-1){
			idx= 0;
			this.lastGivenIndex=idx;
		}
		else{
			int sum = this.lastGivenIndex+3;
			if ((sum)<this.availableFiles.size()){
				idx = sum;
				this.lastGivenIndex=idx;
			}
			else {
				idx=0;
				this.lastGivenIndex=idx;
			}
		}
		
		return idx;
	}
	/**
	 * To call to know if all the part files in the pool have been completely parsed.
	 * @return true if all the part files in the pool have been completely parsed.
	 */
	public boolean isComplete(){
		boolean complete = true;
		for (PartFile p : this.availableFiles)
			complete = complete && p.isComplete();
		for (PartFile p : this.parsedFiles)
			complete = complete && p.isComplete();
		for (PartFile p : this.partFiles)
			complete = complete && p.isComplete();
		return complete;
	}
}
