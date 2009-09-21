package dblp.social.updater;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import dblp.social.importer.DblpIdUtility;

/**
 * This class provides a simple parser. 
 * This parser only recognizes some dblp tags needed to execute the stored queries.
 * The stored queries are written knowing the result of the database mapping executed by Hibernate.
 * 
 * If the mapping changes also the static strings defined in this class need to be changed.
 * 
 * 
 * @author staffiero
 *
 */
public class DblpUpdaterParser {
	static Logger logger = Logger.getLogger(DblpUpdaterParser.class.getName());
	private static final String INCOLLECTION = "incollection";
	private static final String INPROCEEDINGS = "inproceedings";
	private static final String PROCEEDINGS = "proceedings";
	private static final String ARTICLE = "article";
	private static final String BOOK = "book";
	
	private static final String ARTICLE_TABLE ="Article";
	private static final String ARTICLE_ID ="publicationId";
	private static final String BOOK_TABLE ="Book";
	private static final String BOOK_ID ="bookId";
	private static final String INCOLLECTION_TABLE ="InCollection";
	private static final String INCOLLECTION_ID ="publicationId";
	private static final String INPROCEEDINGS_TABLE ="InProceedings";
	private static final String INPROCEEDINGS_ID ="publicationId";
	private static final String JOURNAL_TABLE ="Journal";
	private static final String JOURNAL_ID ="journalId";
	private static final String PROCEEDINGS_TABLE ="Proceedings";
	private static final String PROCEEDINGS_ID ="proceedingsId";
	private static final String DBLP_KEY = "dblpKey";
	
	private Connection con;
	private String currElement="";
	private FileWriter output;

	private FileReader input;
	private StringBuffer buffer;
	private String dblpKey="";
	private String url="";
	private String year="";
	private String volume="";
	private PreparedStatement findInProceedings;
	private PreparedStatement findArticle;
	private PreparedStatement findInCollection;
	private PreparedStatement findBook;
	private PreparedStatement findProceedings;
	private PreparedStatement findJournal;
	private int newEntries = 0;
	
	/**
	 * The constructor to be used.
	 * @param con a connection trough the database
	 * @param input the input XML file
	 * @param output
	 * @throws IOException
	 */
	public DblpUpdaterParser(Connection con, File input, File output) throws IOException{
		this.con = con;
		this.output = new FileWriter(output);
		this.input = new FileReader(input);
		if (this.con==null)
			throw new IOException("null connection in DblpUpdaterHandler constructor");
		this.buffer = new StringBuffer();
		//note that we cannot look for a Book, Journal or Proceedings only trough its
		//key (the url) because the book|Journal|Proceedings entry could have been 
		//created after finding a incollection|article|proccedings.
		//If that's the case we shall need to update the book|Journal|Proceedings, 
		//if not we can skip this update.
		//Therefore we look for a book|Journal|Proceedings by its url but only
		//if its dblpKey is not null
		try{
			this.findInProceedings = 
				con.prepareStatement(
					"SELECT count(*) as elements FROM "+INPROCEEDINGS_TABLE+
					" WHERE " + INPROCEEDINGS_ID+
					"=?");
			this.findArticle = 
				con.prepareStatement(
					"SELECT count(*) as elements FROM "+ARTICLE_TABLE+
					" WHERE " + ARTICLE_ID +
					"=?");
			this.findInCollection = 
				con.prepareStatement(
					"SELECT count(*) as elements FROM "+INCOLLECTION_TABLE+
					" WHERE " + INCOLLECTION_ID+
					"=?");
			this.findBook = 
				con.prepareStatement(
					"SELECT count(*) as elements FROM "+BOOK_TABLE+
					" WHERE " + BOOK_ID +
					"=? AND "+DBLP_KEY+"<> ''");
			 this.findProceedings = 
				con.prepareStatement(
					"SELECT count(*) as elements FROM "+PROCEEDINGS_TABLE+
					" WHERE " + PROCEEDINGS_ID+
					"=? AND "+DBLP_KEY+"<> ''");
			 this.findJournal = 
					con.prepareStatement(
						"SELECT count(*) as elements FROM "+JOURNAL_TABLE+
						" WHERE " + JOURNAL_ID +
						"=? AND "+DBLP_KEY+"<> ''");
		}
		catch(Exception e){
			logger.error("Error creating prepared satements: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Reads a stream of chars from the input XML file.
	 * 
	 * @throws IOException
	 */
	public void parse() throws IOException{
		char c;
		String text="";
		String tagname="";
		
		//the first 3 lines of the dblp xml are supposed to be:
		//<?xml version="1.0" encoding="ISO-8859-1"?>
		//<!DOCTYPE dblp SYSTEM "dblp.dtd">
		//<dblp>
		boolean read=true;
		while (read){
			c = (char)input.read();
			text = text+c;
			if (c=='>'){
				if (text.contains("<dblp>")){
					read = false;
				}
			}
		}
		buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
				"\n<!DOCTYPE dblp SYSTEM \"dblp.dtd\">" +
				"\n<dblp>\n");
		flushBuffer();
		text="";
		boolean skip=true;
		boolean closingTag=false;
		int a;
		//read the document body
		while ((a =input.read()) != -1){
			c = (char)a;
			//read an xml tag
			if(c=='<'){
				text = text+c;
				c = (char)input.read();
				if (c=='/'){
					closingTag=true;
				}
				else{
					tagname = tagname+c;
					closingTag=false;
				}
				text = text+c;
				//read the whole tag name
				boolean readName=true;
				while (c!='>'){
					c = (char)input.read();
					if (c==' ' || c=='>')
						readName = false;
					if (readName){
						tagname = tagname+c;
					}
					text = text+c;
				}
				tagname.toLowerCase();
				//here in text we have an XML tag: <...>, it can be an opening tag
				//or a closing tag (to know it evaluate the boolean variable closingTag
				if (tagname.compareTo("article")==0 ||
						tagname.compareTo("inproceedings")==0 ||
						tagname.compareTo("proceedings")==0 ||
						tagname.compareTo("book")==0 ||
						tagname.compareTo("incollection")==0 ||
						tagname.compareTo("phdthesis")==0 ||
						tagname.compareTo("mastersthesis")==0 ||
						tagname.compareTo("www")==0||
						tagname.compareTo("dblp")==0){
					//if text contains a "main" tag it is sent to parseString
					buffer.append(text+"\n");
					parseString(text);
					text="";
					tagname="";
				}
				else{
					//if the tag is an opening tag set the boolean variable 
					//skip to false
					if (!closingTag)
						skip=false;
					else{
						//the tag is a closing tag: send the string to parseString
						//set skip=true
						buffer.append(text+"\n");
						parseString(text);
						text="";
						skip=true;
					}
					tagname="";
				}
			}else{
				if (!skip)
					text = text+c;
			}	
		}
		this.input.close();
		this.output.close();
		System.out.println("Found "+newEntries+" new entries");
	}
	
	
	private int findExistingElements() throws IOException{
		int existingElements=0;
		if (this.url.contains("&amp;"))
			this.url = this.url.replaceAll("&amp;", "&");
		
		if(this.currElement.equalsIgnoreCase(INCOLLECTION)){
			//WORKAROUND caused by books/mit/PF91/Piatetsky91 entry
			if (this.url.contains("PiatetskyF91.html")&&!this.url.contains("#"))
				this.url=this.url+"#"+this.dblpKey.substring((this.dblpKey.lastIndexOf("/") + 1));
			try{
				
				//lookup if the current in-collection already exists
				if(this.url.compareTo("")==0){
					if (this.dblpKey.compareTo("")==0)
						logger.error("Both url and key are empty");
					else
						this.findInCollection.setString(1, this.dblpKey);
				}
				else
					this.findInCollection.setString(1, this.url);
				ResultSet rs = this.findInCollection.executeQuery();
				if (rs.next()){
					existingElements = rs.getInt("elements");
				}
				rs.close();
				
			}
			catch(Exception e){
				logger.error("Error accessing the database "+e.getMessage());
				throw new IOException(e);
			}
		}else if(this.currElement.equalsIgnoreCase(INPROCEEDINGS)){
//			//WORKAROUND caused by some entries which share the same url
//			if( this.url.contains("http://theory.lcs.mit.edu/")){
//				//we need to change these urls into a standard form. 
//				//For example the url http://theory.lcs.mit.edu/~dmjones/FOCS/focs81.html
//				//found in an InProceedings with the following tags
//				//<inproceedings mdate="2002-01-03" key="conf/focs/Kannan81b">
//				//<crossref>conf/focs/FOCS22</crossref>
//				//<year>1981</year>
//				//<booktitle>FOCS</booktitle>
//				//<url>http://theory.lcs.mit.edu/~dmjones/FOCS/focs81.html</url>
//				//should be changed to 
//				//db/conf/focs/focs81.html#Kannan81b
//				//where: focs is the proceedings name found in the url, between the last two '/' and changed to lowercase
//				//81 is the year (2 digits)
//				//Kannan81b is the last part of the key
//				String oldUrl = this.url;
//				
//				String cName=oldUrl.substring(oldUrl.lastIndexOf("~"));
//				cName = cName.substring(cName.indexOf("/")+1,cName.lastIndexOf("/"));
//				cName = cName.toLowerCase();
//				
//				String cYear = this.year;
//				
//				String currentKey = this.dblpKey;
//				String type = currentKey;
//				type = type.substring(0,type.indexOf("/"));
//				
//				String keyPart = currentKey.substring(currentKey.lastIndexOf("/")+1);
//				String newUrl="db/"+type+"/"+cName+"/"+cName+cYear.substring(2)
//				+".html#"+keyPart;
//				
//				this.url = newUrl;
//			}
			try{
				//an InProccedings element can be in the db as both an InProccedings
				//or an article
				
				//lookup if the current in-proceedings already exists
				
//				if(this.url.compareTo("")==0){
//					if (this.dblpKey.compareTo("")==0)
//						logger.error("Both url and key are empty");
//					else
//						this.findInProceedings.setString(1, this.dblpKey);
//				}
//				else
//					this.findInProceedings.setString(1, this.url);
				
				if(this.url.compareTo("")==0 && this.dblpKey.compareTo("")==0){	
					logger.error("Both url and key are empty");
					return 0;
				}		
				String id = DblpIdUtility.createInProceedingsId(this.url, 
						this.dblpKey, 
						this.year.substring(2),
						INPROCEEDINGS);
				this.findInProceedings.setString(1, id);
				ResultSet rs = this.findInProceedings.executeQuery();
				if (rs.next()){
					existingElements = rs.getInt("elements");
				}
				rs.close();
				if (existingElements==0){
					//If no InProceedings are found, look for an Article
					if(this.url.compareTo("")==0){
						if (this.dblpKey.compareTo("")==0)
							logger.error("Both url and key are empty");
						else
							this.findArticle.setString(1, this.dblpKey);
					}
					else
						this.findArticle.setString(1, this.url);
					
					rs = this.findArticle.executeQuery();
					if (rs.next()){
						existingElements = rs.getInt("elements");
					}
					rs.close();
				}
				
			}
			catch(Exception e){
				logger.error("Error accessing the database "+e.getMessage());
				throw new IOException(e);
			}
			
		}else if(this.currElement.equalsIgnoreCase(ARTICLE)){
			
//			//WORKAROUND caused by some entries which share the same url
//			if( this.url.contains("http://theory.lcs.mit.edu/")){
//				//we need to change these urls into a standard form. 
//				//For example the url http://theory.lcs.mit.edu/~iandc/ic71.html
//				//found in an article with the following tags
//				//<article mdate="2005-05-03" key="journals/iandc/PatrickL71a">
//				//<journal>Information and Control</journal>
//				//<month>March</month>
//				//<year>1971</year>
//				//<volume>18</volume>
//				//should be changed to 
//				//db/journals/iandc/iandc18.html#PatrickL71a
//				//where: iandc is the journal name found in the url, between '~' and '/'
//				//18 is the volume number
//				//PatrickL71a is the last part of the key
//				String oldUrl = this.url;
//				String jName=oldUrl.substring(oldUrl.lastIndexOf("~")+1, oldUrl.lastIndexOf("/"));
//				String currentKey = this.dblpKey;
//				String type = currentKey;
//				type = type.substring(0,type.indexOf("/"));
//				String jVolume = this.volume;
//				
//				String keyPart = currentKey.substring(currentKey.lastIndexOf("/")+1);
//				String newUrl="db/"+type+"/"+jName+"/"+jName+
//				jVolume+".html#"+keyPart;
//				
//				this.url = newUrl;
//			}
//			
			try{
				//lookup if the current article already exists
//				if(this.url.compareTo("")==0){
//					if (this.dblpKey.compareTo("")==0)
//						logger.error("Both url and key are empty");
//					else
//						this.findArticle.setString(1, this.dblpKey);
//				}
//				else
//					this.findArticle.setString(1, this.url);
				if(this.url.compareTo("")==0 && this.dblpKey.compareTo("")==0){	
					logger.error("Both url and key are empty");
					return 0;
				}		
				String id = DblpIdUtility.createInProceedingsId(this.url, 
						this.dblpKey, 
						this.volume,
						ARTICLE);
				this.findArticle.setString(1, id);
				ResultSet rs = this.findArticle.executeQuery();
				if (rs.next()){
					existingElements = rs.getInt("elements");
				}
				rs.close();
				
			}
			catch(Exception e){
				logger.error("Error accessing the database "+e.getMessage());
				throw new IOException(e);
			}
		}else if(this.currElement.equalsIgnoreCase(BOOK)){
			try{
				//lookup if the current book already exists
				if(this.url.compareTo("")==0){
					if (this.dblpKey.compareTo("")==0)
						logger.error("Both url and key are empty");
					else
						this.findBook.setString(1, this.dblpKey);
				}
				else
					this.findBook.setString(1, this.url);
				
				ResultSet rs = this.findBook.executeQuery();
				if (rs.next()){
					existingElements = rs.getInt("elements");
				}
				rs.close();
				
			}catch(Exception e){
				logger.error("Error accessing the database "+e.getMessage());
				throw new IOException(e);
			}
			
		}else if(this.currElement.equalsIgnoreCase(PROCEEDINGS)){
			try{
				//a Proccedings element can be in the db as both a Proccedings
				//or a Journal
				
				//lookup if the current proceedings already exists
				
//				if(this.url.compareTo("")==0){
//					if (this.dblpKey.compareTo("")==0)
//						logger.error("Both url and key are empty");
//					else
//						this.findProceedings.setString(1, this.dblpKey);;
//				}
//				else
//					this.findProceedings.setString(1, this.url);
				if(this.url.compareTo("")==0 && this.dblpKey.compareTo("")==0){	
					logger.error("Both url and key are empty");
					return 0;
				}		
				String id = DblpIdUtility.createProceedingsId(this.url, 
						this.dblpKey);
				
				this.findProceedings.setString(1, id);
				ResultSet rs = this.findProceedings.executeQuery();
				if (rs.next()){
					existingElements = rs.getInt("elements");
				}
				rs.close();
				if (existingElements==0){
					//If no Proceedings are found, look for a Journal
					
					if(this.url.compareTo("")==0){
						if (this.dblpKey.compareTo("")==0)
							logger.error("Both url and key are empty");
						else
							findJournal.setString(1, this.dblpKey);
					}
					else
						findJournal.setString(1, this.url);
					
					
					rs = findJournal.executeQuery();
					if (rs.next()){
						existingElements = rs.getInt("elements");
					}
					rs.close();
				}
				
			}
			catch(Exception e){
				logger.error("Error accessing the database "+e.getMessage());
				throw new IOException(e);
			}
			
		}
		return existingElements;
	}
	
	
	private void flushBuffer() throws IOException{
		this.output.write(this.buffer.toString());
		this.buffer=new StringBuffer();
	}
	
	private void parseString(String text) throws IOException{
		if(text.contains("<url>") && text.contains("</url>") ){
			//read an url tag
			String url = text;
			url = url.replaceAll(" ", "");
			url = url.substring(url.indexOf("<url>"), url.indexOf("</url>"));
			url = url.replaceAll("<url>", "");
			url = url.replaceAll("\t", "");
			url = url.replaceAll("\n", "");
			this.url = url;
		}else if(text.contains("<year>") && text.contains("</year>")){
			//read a year tag
			String year = text;
			year = year.substring(year.indexOf("<year>"), year.indexOf("</year>"));
			year = year.replaceAll("<year>", "");
			year = year.replaceAll("\t", "");
			year = year.replaceAll("\n", "");	
			year = year.replaceAll(" ", "");	
			this.year=year;
		}else if(text.contains("<volume>") && text.contains("</volume>")){
			//read a volume tag
			String volume = text;
			volume = volume.substring(volume.indexOf("<volume>"), volume.indexOf("</volume>"));
			volume = volume.replaceAll("<volume>", "");
			volume = volume.replaceAll("\t", "");
			volume = volume.replaceAll("\n", "");	
			volume = volume.replaceAll(" ", "");	
			this.volume = volume;
		}else if (text.contains("</article>") ||
				text.contains("</inproceedings>") ||
				text.contains("</proceedings>") ||
				text.contains("</book>") ||
				text.contains("</incollection>") ||
				text.contains("</phdthesis>") ||
				text.contains("</mastersthesis>") ||
				text.contains("</www>")){
			if(text.contains("</phdthesis>") ||
					text.contains("</mastersthesis>") ||
					text.contains("</www>")){
				this.buffer=new StringBuffer();
			}else {
			//when the end of a main tag is reached
				if (findExistingElements()==0){
					//if the element is not in the db the buffer is written 
					//into the output
					//else the buffer is deleted
					flushBuffer();
					newEntries++;
				}else
					this.buffer=new StringBuffer();
			}
			//clear temporary informations
			this.volume="";
			this.year="";
			this.url="";
			this.dblpKey="";
			
		}else if (text.contains("</dblp>")){
			//end of document: flush the buffer
			flushBuffer();
		}else if (text.contains("<article ") ||
				text.contains("<inproceedings ") ||
				text.contains("<proceedings ") ||
				text.contains("<book ") ||
				text.contains("<incollection ") ||
				text.contains("<phdthesis ") ||
				text.contains("<mastersthesis ") ||
				text.contains("<www ")){
			//check if a "main" tag is opened
			//Set the current entity
			if (text.contains("<article "))
				this.currElement = "article";
			else if (text.contains("<proceedings " ))
				this.currElement = "proceedings";
			else if (text.contains("<book "))
				this.currElement = "book";
			else if (text.contains("<incollection "))
				this.currElement = "incollection";
			else if (text.contains("<inproceedings "))
				this.currElement = "inproceedings";
			else if (text.contains("<phdthesis "))
				this.currElement = "phdthesis";
			else if (text.contains("<mastersthesis "))
				this.currElement = "mastersthesis";
			else if (text.contains("<www "))
				this.currElement = "www";
			
			//Set the dblp Key
			String key = text;
			key=key.substring(key.indexOf("key"));
			key=key.substring(key.indexOf("\"")+1);
			key=key.substring(0,key.indexOf("\""));
			key=key.replaceAll(" ", "");
			this.dblpKey = key;
		}
		
	}
}
