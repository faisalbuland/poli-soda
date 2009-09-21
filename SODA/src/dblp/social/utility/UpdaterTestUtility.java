package dblp.social.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A custom class used to find a bug... it is useless now 
 * 
 * @author Staffiero
 *
 */
@Deprecated
public class UpdaterTestUtility {
	private static final String FILE ="newsource.xml";
	public static void main(String[] args){
		try {
			divideEtImpera();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void divideEtImpera() throws IOException{
		File inputFile=new File(FILE);
		int partNumber = 2;
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
		
		
		
		if (inputFile==null)
			return;
		if (inputFile.isFile()){
			float avgPartLenght = inputFile.length()/partNumber;			
			
			//Point the _part file
			File outputFile = new File("divide_et_impera1_dblp.xml");
			
			if (outputFile.exists())
				outputFile.delete();
			else
				outputFile.createNewFile();
			
			//Create the first _part file
			FileWriter output = new FileWriter(outputFile); 
			
			//Open the inputFile
			BufferedReader input = 
				new BufferedReader(new FileReader(inputFile));
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
			
			//Read the whole input file
			while ((text = input.readLine() ) != null){
				
				/* TODO this code can be removed, however it is used to show 
				 * interesting informations of the xml file composition
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
			
					output.write(buffer.toString());
					buffer = new StringBuffer();
					if (outputFile.length()>=avgPartLenght){
						output.write("</dblp>");
						output.close();
						
						
						//Point the new file
						outputFile = new File("divide_et_impera2_dblp.xml");
						if(outputFile.exists())
							outputFile.delete();
						else
							outputFile.createNewFile();
						
						output = new FileWriter(outputFile);
						
						//Initialize the _part file with the proper XML declarations and tags
						buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
						buffer.append("<!DOCTYPE dblp SYSTEM \"dblp.dtd\">\n");
						buffer.append("<dblp>\n");
						
					}
				}
				
			}
			//Write the buffer and close the last _part file
			output.write(buffer.toString());
			output.close();
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
		}
	}

}
