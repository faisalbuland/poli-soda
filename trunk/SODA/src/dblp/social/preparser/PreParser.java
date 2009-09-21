package dblp.social.preparser;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import dblp.social.exceptions.FileExistsException;


/**
 * This class defines the functions used to validate and pre-parse an XML dblp file.
 * @author Staffiero
 *
 */
public class PreParser extends Thread{
	static Logger logger = Logger.getLogger(PreParser.class.getName());
	private File inputFile;
	private ArrayList<File> partFiles;
	private SharedBuffer sharedBuffer=null;
	private int partNumber=0;
	private int chkptStep=0;
	private Frame parent;
	public static final String SUBFOLDER = "XMLPartFiles/";
	
	/**
	 * The constructor to be used to call the preParse method, this will block the execution until the pre-parser finishes .
	 * If you use this constructor, then call the preParse(int partNumber, int chkptStep) 
	 * method to pre parse the given inputFile
	 * 
	 * @param inputFile: the XML file to be pre-parsed
	 * 
	 */
	public PreParser(File inputFile){		
		this.setInputFile(inputFile);
	}
	/**
	 * The constructor to be used to call the preParse into a separate thread.
	 * If this constructor is used, then call the start() method to start a thread which 
	 * pre parses the given inputFile.
	 * 
	 * @param inputFile: the XML file to be pre-parsed
	 * @param buffer: a synchronized SharedBuffer in which the state of the current pre-parsing is saved and updated (used to show the current progress). If you do not want to display the current progress set this to null.
	 * @param partNumber: the number of "_part" files to be generated, it has to be greater than 1
	 * @param chkptStep: the number of dblp "main" tags between checkpoints, it has to be greater than 0
	 * @param parent: the parent Frame, used to display errors, if parent==null errors are displayed trough logger
	 */
	public PreParser(File inputFile, SharedBuffer buffer, int partNumber, 
			int chkptStep, Frame parent){
		this.sharedBuffer = buffer;
		this.chkptStep=chkptStep;
		this.partNumber=partNumber;
		this.parent=parent;
		this.setInputFile(inputFile);
	}
	
	/**
	 * The constructor to be used to call the preParse method sequentially using a SharedBuffer 
	 * as progress monitor.
	 * If this constructor is used, then call the preParse(int partNumber, int chkptStep) 
	 * method to pre parse the given inputFile
	 * 
	 * @param inputFile: the XML file to be pre-parsed
	 * @param buffer: a synchronized SharedBuffer in which the state of the current pre-parsing is saved
	 */
	public PreParser(File inputFile, SharedBuffer buffer){
		this.sharedBuffer = buffer;
		this.setInputFile(inputFile);
	}
	
	/**
	 * Getter, returns the input file pointer
	 * @return this.inputFile
	 */
	public File getInputFile() {
		return inputFile;
	}

	/**
	 * Setter, sets this.inputFile
	 * @param inputFile
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * Getter, returns the list of Files, which are the pre-parser output
	 * @return this.partFiles
	 */
	public ArrayList<File> getPartFiles() {
		return partFiles;
	}

	/**
	 * Setter, sets this.partFiles
	 * @param partFiles
	 */
	public void setPartFiles(ArrayList<File> partFiles) {
		this.partFiles = partFiles;
	}

	/**
	 * This method checks the given inputFile. If the file is a valid DBLP XML it returns true, false otherwise
	 * 
	 * @return result check 
	 */
	public boolean validateInputFile() throws IOException{
		if (this.inputFile==null)
			return false;
		if (this.inputFile.isFile()){
			BufferedReader input = 
				new BufferedReader(new FileReader(this.inputFile));
			StringBuffer buffer = new StringBuffer();
			String text;
			for (int i=0; i<4; i++){
				text = input.readLine();
				buffer.append(text);
			}
			if (buffer.toString().startsWith("<?xml")&& 
					buffer.toString().contains("dblp.dtd") &&
					buffer.toString().contains("<dblp>") &&
					!buffer.toString().contains("<checkpoint")){
				return true;
			}
		}
		return false;
	}
	/**
	 * This method pre parses the given XML file. The given file is pointed by the inputFile attribute, therefore this attribute needs to be initialized
	 * The inputFile is divided in different smaller files (_part files).
	 * In each _part file checkpoints are added between dblp "main" tags. A main tag is a tag which is a first level child of the xml tag <dblp>.
	 * 
	 * @param partNumber: the number of "_part" files to be generated, it has to be not null and greater than 0
	 * @param chkptStep: the number of dblp "main" tags between checkpoints, it has to be greater than 0
	 * 
	 * @return a string containing the path of the generated descriptor xml file if the operation succeeds, null if the operation fails
	 */
	public String preParse(int partNumber, int chkptStep) throws Exception,IOException, FileExistsException{
		
		/* TODO this code can be removed, however it is used to show 
		 * interesting informations about the xml file composition
		 * 
		 * Debug code
		 * 
		 */
		int artCont=0, inpCont=0, procCont=0, bookCont=0, incCont=0, phdCont=0, masterCont=0, wwwCont=0;
		/*
		 * end debug code
		 */
		
		
		
		String result = null;
		if (this.inputFile==null)
			return null;
		if (partNumber < 2 || chkptStep <1)
			throw new Exception("Parts number or checkpoint step contain invalid value. At least parts number = 2 and checkpoint step = 1");
		if (this.inputFile.isFile()){
			int cont = 1;
			int tagsSoFar = 0;
			int cpn=0;
			float avgPartLenght = this.inputFile.length()/partNumber;			
			String parentDir = this.inputFile.getParent();
			if (parentDir==null){
				parentDir="";
			}
			else{
				parentDir=parentDir+"/";
			}
			String inputFileName = inputFile.getName().substring(0,inputFile.getName().lastIndexOf("."));
			
			//Initialize the XMLPartsInfo
			XMLPartsInfo info = new XMLPartsInfo(this.getInputFile(),SUBFOLDER,chkptStep);
			
			//Check if SUBDIRECTORY exists and if it is a directory, if not create it
			File subDir = new File(parentDir+SUBFOLDER);
			if (!subDir.exists() || !subDir.isDirectory())
				subDir.mkdirs();
			
			//Point the _part file
			File partFile = new File(parentDir+SUBFOLDER+inputFileName+"_part"+cont+".xml");
			
			if (partFile.exists())
				throw new FileExistsException("The _part file "+partFile.getName()+" already exists");
			else
				partFile.createNewFile();
			
			System.out.println("Starting pre parser");
			
			//Create the first _part file
			FileWriter output = new FileWriter(partFile); 
			
			//Write the name and sequence number of the _part file in the shared 
			// buffer if it is not null
			if (sharedBuffer!=null)
				sharedBuffer.set(partFile.getName());
			
			//Open the inputFile
			BufferedReader input = 
				new BufferedReader(new FileReader(this.inputFile));
			StringBuffer buffer = new StringBuffer();
			String text;
			
			//the first 3 lines of the dblp xml are supposed to be:
			//<?xml version="1.0" encoding="ISO-8859-1"?>
			//<!DOCTYPE dblp SYSTEM "dblp.dtd">
			//<dblp>
			for (int i=0; i<3; i++){
				text = input.readLine();
				buffer.append(text+"\n");
			}
			buffer.append("<checkpoint number=\""+cpn+"\" />\n");
			cpn++;
			
			//Read the whole input file
			while ((text = input.readLine() ) != null){
				
				/* TODO this code can be removed, however it is used to show 
				 * interesting informations about the xml file composition
				 * 
				 * Debug code
				 * 
				 */
				if (text.contains("</article>"))
					artCont++;
				if (text.contains("</inproceedings>"))
					inpCont++;
				if (text.contains("</proceedings>"))
					procCont++;
				if (text.contains("</book>"))
					bookCont++;
				if (text.contains("</incollection>"))
					incCont++;
				if (text.contains("</phdthesis>"))
					phdCont++;
				if (text.contains("</mastersthesis>"))
					masterCont++;
				if (text.contains("</www>"))
					wwwCont++;
				/*
				 * end debug code
				 */
				
				
				//check if a "main" tag is closed.
				//Dblp main tags are: <article>, <inproceedings>, <proceedings>, 
				//<book>, <incollection>
                //<phdthesis>, <mastersthesis>, <www>
				buffer.append(text+"\n");
				if (text.compareTo("</article>")==0 ||
						text.compareTo("</inproceedings>")==0 ||
						text.compareTo("</proceedings>")==0 ||
						text.compareTo("</book>")==0 ||
						text.compareTo("</incollection>")==0 ||
						text.compareTo("</phdthesis>")==0 ||
						text.compareTo("</mastersthesis>")==0 ||
						text.compareTo("</www>")==0){
			
					tagsSoFar++;
										
					//Check if a checkpoint should be added
					if (tagsSoFar>=chkptStep){
						buffer.append("<checkpoint number=\""+cpn+"\" />\n");
						cpn++;
						tagsSoFar=0;
						
						//after adding a checkpoint write the buffer to the output file
						output.write(buffer.toString());
						buffer = new StringBuffer();
						
						//control whether to close the current _part file
						if (partFile.length()>=avgPartLenght){
							cont++;
							output.write("</dblp>");
							output.close();
							
							//append the _part file informations to the XMLPartsInfo
							info.appendPartFile(partFile.getAbsolutePath(), cpn, partFile.length());
							
							//Point the new _part file
							partFile = new File(parentDir+SUBFOLDER+inputFileName+"_part"+cont+".xml");
						
							//create a new _part file
							output = new FileWriter(partFile);
							
							//Write the name and sequence number of the _part file in the shared buffer
							if (sharedBuffer!=null)
								sharedBuffer.set(partFile.getName());
							
							//Initialize the _part file with the proper XML declarations and tags
							buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
							buffer.append("<!DOCTYPE dblp SYSTEM \"dblp.dtd\">\n");
							buffer.append("<dblp>\n");
							cpn=0;
							tagsSoFar = 0;
							buffer.append("<checkpoint number=\""+cpn+"\" />\n");
							cpn++;
						}
					}
				}
				
			}
			//Write the buffer and close the last _part file
			output.write(buffer.toString());
			output.close();
			
			//Write "DONE" in the shared buffer
			if (sharedBuffer!=null)	
				sharedBuffer.set(SharedBuffer.DONE);
			
			//append the _part file informations to the XMLPartsInfo
			info.appendPartFile(partFile.getAbsolutePath(), cpn, partFile.length());
			
			//save the XMLPartsInfo
			String descriptorPath = info.savePartFileInfo();
			
			input.close();
			
			/* TODO this code can be removed, however it is used to show 
			 * interesting informations of the xml file composition
			 * 
			 * Debug code
			 * 
			 */
			long total=artCont+inpCont+procCont+bookCont+incCont+phdCont+masterCont+wwwCont;
			long actual = artCont+inpCont+procCont+bookCont+incCont;
			System.out.println("Article tags number=" +artCont+
					"\nInProceedings tags number="+inpCont+
					"\nProceedings tags number="+procCont+
					"\nBook tags number="+bookCont+
					"\nIncollection tags number="+incCont+
					"\nPhdThesis tags number="+phdCont+
					"\nMasterthesis tags number="+masterCont+
					"\nWww tags number="+wwwCont+
					"\nTotal tags number="+total+
					"\nActual tags number (not including phdthesis, masterthesis and www tags)="+actual);
			/*
			 * end debug code
			 */
			return descriptorPath;
		}
		else{
			result=null;
		}
		return result;
	}

	/**
	 * Called when the thread starts, it pre-parses the given inputFile with the partNumber 
	 * and chkptStep parameters.
	 */
	@Override
	public void run() {
		Calendar begin = Calendar.getInstance();
		try{
			if (preParse(partNumber, chkptStep)!=null){
				Calendar end = Calendar.getInstance();
				long timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
				if (this.parent!=null){
					JOptionPane.showMessageDialog(parent,"Pre Parsing done\n"+
							"Operation took "+timeTaken+" milliseconds",
							"Info", JOptionPane.INFORMATION_MESSAGE);
				}
				else{
					System.out.println("Pre Parsing done\nOperation took "+timeTaken+" milliseconds");
				}
			}
		}
		catch (IOException ioe){
			ioe.printStackTrace();
			if (this.parent!=null){
				JOptionPane.showMessageDialog(parent,"IOException thrown:\n"+
					ioe.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			}
			else{
				logger.error("IOException thrown:\n"+ioe.getMessage());
				//System.err.println("IOException thrown:\n"+ioe.getMessage());
			}
		}
		catch (FileExistsException fe){
			fe.printStackTrace();
			//Release resources to prevent deadlocks
			sharedBuffer.releaseMonitor();
			if (this.parent!=null){
				JOptionPane.showMessageDialog(parent,"FileExistsException thrown:\n" +
						fe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			else{
				logger.error("FileExistsException thrown:\n" + fe.getMessage());
				//System.err.println("FileExistsException thrown:\n" + fe.getMessage());
			}
					
		}
		catch (Exception ex){
			ex.printStackTrace();
			//Release resources to prevent deadlocks
			sharedBuffer.releaseMonitor();
			if (this.parent!=null){
				JOptionPane.showMessageDialog(parent,"Exception thrown:\n"+
						ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
			}
			else{
				logger.error("Exception thrown:\n"+ex.getMessage());
				//System.err.println("Exception thrown:\n"+ex.getMessage());
			}
			
		}
	}
	
	
}
