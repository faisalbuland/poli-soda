package dblp.social.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
/**
 * A simple class which generates a subset of dblp.xml for testing purposes
 * 
 * @author Staffiero
 *
 */
public class SubsetCreator {
	private static final long SUBSET_SIZE = 50*1024*1024;
	private static final String DBLP_XML = "./dblp.xml";
	
	public static void main(String[] args) throws IOException{
		//mitTagsFinder();
		articleTagsFinder();
	}
	
	/**
	 * Creates a simple subset of dblp.xml writing the first encountered tags until the subset dimension reaches SUBSET_SIZE.
	 * @throws IOException
	 */
	public static void createSimpleSubset() throws IOException{
		File dblp = new File(DBLP_XML);
		File subset = new File("dblp_subset_new.xml");
		if (!subset.exists()){
			subset.createNewFile();
		}
		else {
			subset.delete();
			subset.createNewFile();
		}
		
		FileWriter output = new FileWriter(subset); 
		BufferedReader input = 
			new BufferedReader(new FileReader(dblp));
		StringBuffer buffer= new StringBuffer();
		String text;
		boolean goOn=true;
		while ((text = input.readLine() ) != null && goOn){
			buffer.append(text+"\n");
			if (text.contains("</article>") ||
					text.contains("</inproceedings>") ||
					text.contains("</proceedings>") ||
					text.contains("</book>") ||
					text.contains("</incollection>") ||
					text.contains("</phdthesis>") ||
					text.contains("</mastersthesis>") ||
					text.contains("</www>")){
				output.write(buffer.toString());
				buffer = new StringBuffer();
				
				if (subset.length()>=SUBSET_SIZE){
					goOn=false;
					output.write("</dblp>");
				}
			}
		}
		input.close();
		output.close();
	}
	
	/**
	 * Creates a subset containing all the mit-tags in the dblp.xml file.
	 * A mit-tag is a main tag whose url contains the string "http://theory.lcs.mit.edu/".
	 * 
	 * @throws IOException
	 */
	public static void mitTagsFinder() throws IOException{
		System.out.println("Starting mit tags finder");
		File dblp = new File(DBLP_XML);
		File subset = new File("dblp_subset_mit.xml");
		if (!subset.exists()){
			subset.createNewFile();
		}
		else {
			subset.delete();
			subset.createNewFile();
		}
		
		FileWriter output = new FileWriter(subset); 
		BufferedReader input = 
			new BufferedReader(new FileReader(dblp));
		StringBuffer buffer= new StringBuffer();
		String text;
		int cont=0;
		boolean writeTag=false;
		while ((text = input.readLine() ) != null){
			buffer.append(text+"\n");
			
			if (text.contains("<url>") ){
				if (text.contains("http://theory.lcs.mit.edu/"))
					writeTag=true;
			}
			
			if (text.contains("</article>") ||
					text.contains("</inproceedings>") ||
					text.contains("</proceedings>") ||
					text.contains("</book>") ||
					text.contains("</incollection>") ||
					text.contains("</phdthesis>") ||
					text.contains("</mastersthesis>") ||
					text.contains("</www>")){
				if (text.contains("</phdthesis>") ||
					text.contains("</mastersthesis>") ||
					text.contains("</www>")){
					buffer = new StringBuffer();
				}else{
				
					if (writeTag){
						output.write(buffer.toString());
						cont++;
						writeTag=false;
					}
					buffer = new StringBuffer();
				}
			}
		}
		input.close();
		output.close();
		System.out.println("Mit tags finder finished. "+cont+" tags written");
	}
	
	/**
	 *  Creates a subset containing all the article tags in the dblp.xml file.
	 *  
	 * @throws IOException
	 */
	public static void articleTagsFinder() throws IOException{
		int cont=0;
		System.out.println("Starting article tags finder");
		File dblp = new File(DBLP_XML);
		File subset = new File("dblp_subset_article.xml");
		if (!subset.exists()){
			subset.createNewFile();
		}
		else {
			subset.delete();
			subset.createNewFile();
		}
		
		FileWriter output = new FileWriter(subset); 
		BufferedReader input = 
			new BufferedReader(new FileReader(dblp));
		StringBuffer buffer= new StringBuffer();
		String text;
		
		while ((text = input.readLine() ) != null){
			buffer.append(text+"\n");
			
			if (text.contains("<dblp>")){
				output.write(buffer.toString());
			}
			
			if (text.contains("</article>") ||
					text.contains("</inproceedings>") ||
					text.contains("</proceedings>") ||
					text.contains("</book>") ||
					text.contains("</incollection>") ||
					text.contains("</phdthesis>") ||
					text.contains("</mastersthesis>") ||
					text.contains("</www>")){
				if (text.contains("</article>")){
					cont++;
					output.write(buffer.toString());
				}
				buffer = new StringBuffer();
				
				
			}
		}
		output.write("\n</dblp>");
		input.close();
		output.close();
		System.out.println("Mit tags finder finished. "+cont+" tags written");
	}
	
}
