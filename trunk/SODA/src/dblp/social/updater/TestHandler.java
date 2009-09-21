package dblp.social.updater;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TestHandler extends DefaultHandler{
	
	static Logger logger = Logger.getLogger(TestHandler.class.getName());
	private static final String INCOLLECTION = "incollection";
	private static final String INPROCEEDINGS = "inproceedings";
	private static final String PROCEEDINGS = "proceedings";
	private static final String ARTICLE = "article";
	private static final String BOOK = "book";
	private static final String URL = "url";
	private static final String DBLP = "dblp";
	private static final String YEAR = "year";
	private static final String VOLUME = "volume";
	private static final String PHDTHESIS = "phdthesis";
	private static final String MASTERSTHESIS = "mastersthesis";
	private static final String WWW = "www";
	
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
	
	private int newElement;
	private Connection con;
	private String currElement="";
	private ArrayList<String> currElementFragments = new ArrayList<String>();
	private FileWriter output;
	private StringBuffer buffer;
	private boolean skipElement;
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
	
	public TestHandler(Connection con, File out) throws IOException {
		super();
		this.newElement=0;
		this.con = con;
		if (out==null)
			throw new IOException("null output file in DblpUpdaterHandler constructor");
		this.output = new FileWriter(out);
		
		if (this.con==null)
			throw new IOException("null connection in DblpUpdaterHandler constructor");
		this.skipElement=false;
		//note that we cannot look for a Book, Journal or Proceedings trough its
		//key (the url) because the book|Journal|Proceedings entry could have been 
		//created after finding a incollection|article|proccedings.
		//If that's the case we need to update the book|Journal|Proceedings, if not
		//we can skip this update.
		//Therefore we look for a book|Journal|Proceedings by its dblpKey
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

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		String nodeValue = new String(ch,start,length);
		this.currElementFragments.add(nodeValue);
		//adds the parsed chars to the buffer
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("Found "+this.newElement+" new entries");
		try{
			this.output.close();
		}
		catch(IOException e){
			throw new SAXException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
			throws SAXException{
		if(localName.equalsIgnoreCase(INCOLLECTION) ||
				localName.equalsIgnoreCase(INPROCEEDINGS) ||
				localName.equalsIgnoreCase(ARTICLE)||
				localName.equalsIgnoreCase(BOOK)||
				localName.equalsIgnoreCase(PROCEEDINGS)){
			//when a main tag is closed simply add to the buffer the closing tag
			this.buffer.append("</"+localName+">\n");
		}else{
			String content = "";
			for(String frag : this.currElementFragments)
				content = content + frag;
			this.buffer.append(cleanStringFormat(content)+"</"+localName+">\n");
		}
		
		
		if(localName.equalsIgnoreCase(URL)){
			//the url is used as the id in the db for:
			//			INCOLLECTION
			//			INPROCEEDINGS
			//			ARTICLE
			//			BOOK
			//			PROCEEDINGS
			
			this.url = "";
			for(String frag : this.currElementFragments)
				this.url = this.url + frag;

		}else if(localName.equalsIgnoreCase(YEAR)){
			
			this.year = "";
			for(String frag : this.currElementFragments)
				this.year = this.year + frag;

		}else if(localName.equalsIgnoreCase(VOLUME)){
			
			this.volume = "";
			for(String frag : this.currElementFragments)
				this.volume = this.volume + frag;

		}else if(localName.equalsIgnoreCase(INCOLLECTION) ||
				localName.equalsIgnoreCase(INPROCEEDINGS) ||
				localName.equalsIgnoreCase(ARTICLE)||
				localName.equalsIgnoreCase(BOOK)||
				localName.equalsIgnoreCase(PROCEEDINGS)){
			//when a main tag is closed
			
			if (this.url.compareTo("")==0)
				this.url=this.dblpKey;
			
			try {
				if (findExistingElements()==0)
					this.skipElement=false;
				else
					this.skipElement=true;
			} catch (IOException e) {
				e.printStackTrace();
				e.printStackTrace();
			}			
			if (!this.skipElement){
				//if the current element is not in the db
				//writes the buffer in the output file
				try {
					this.output.write(this.buffer.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.newElement++;
			}
			this.skipElement=false;
			this.volume="";
			this.year="";
			this.url="";
			this.dblpKey="";
			//flushes the buffer
			this.buffer = new StringBuffer();
		}else if(localName.equalsIgnoreCase(WWW) ||
				localName.equalsIgnoreCase(MASTERSTHESIS) ||
				localName.equalsIgnoreCase(PHDTHESIS)){
			this.skipElement=false;
			this.volume="";
			this.year="";
			this.url="";
			this.dblpKey="";
			//flushes the buffer
			this.buffer = new StringBuffer();
		}
		//clear stored temporary informations
		this.currElementFragments = new ArrayList<String>();
		
	}

	@Override
	public void startDocument() throws SAXException {
	
	}

	@Override
	public void startElement(String uri, String localName, String qName, 
			Attributes attributes) throws SAXException{
		//adds the current element starting tag to the buffer
		this.buffer.append("<"+localName);
		if (attributes.getLength()==0)
			this.buffer.append(">");
		else{
			for(int i=0; i<attributes.getLength(); i++){
				this.buffer.append(" "+cleanStringFormat(attributes.getLocalName(i))+
						"="+cleanStringFormat(attributes.getValue(i)));
			}
			this.buffer.append(">");
		}
		
				
		if(localName.equalsIgnoreCase(INCOLLECTION) ||
				localName.equalsIgnoreCase(INPROCEEDINGS) ||
				localName.equalsIgnoreCase(ARTICLE)||
				localName.equalsIgnoreCase(BOOK)||
				localName.equalsIgnoreCase(PROCEEDINGS) ||
				localName.equalsIgnoreCase(DBLP)){
			this.currElement=localName;
			this.dblpKey=attributes.getValue("key");
			this.buffer.append("\n");
		}
	}
	
	private int findExistingElements() throws IOException{
		int existingElements=0;
//		if (this.url.contains("&")){
//			//TODO Debug code
//			System.out.println("Found encoding within a url: \n"+this.url);
//		}
//		if (this.url.contains("&amp;"))
//			this.url = this.url.replaceAll("&amp;", "&");
		
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
			//WORKAROUND caused by some entries which share the same url
			if( this.url.contains("http://theory.lcs.mit.edu/")){
				//we need to change these urls into a standard form. 
				//For example the url http://theory.lcs.mit.edu/~dmjones/FOCS/focs81.html
				//found in an InProceedings with the following tags
				//<inproceedings mdate="2002-01-03" key="conf/focs/Kannan81b">
				//<crossref>conf/focs/FOCS22</crossref>
				//<year>1981</year>
				//<booktitle>FOCS</booktitle>
				//<url>http://theory.lcs.mit.edu/~dmjones/FOCS/focs81.html</url>
				//should be changed to 
				//db/conf/focs/focs81.html#Kannan81b
				//where: focs is the proceedings name found in the url, between the last two '/' and changed to lowercase
				//81 is the year (2 digits)
				//Kannan81b is the last part of the key
				String oldUrl = this.url;
				
				String cName=oldUrl.substring(oldUrl.lastIndexOf("~"));
				cName = cName.substring(cName.indexOf("/")+1,cName.lastIndexOf("/"));
				cName = cName.toLowerCase();
				
				String cYear = this.year;
				
				String currentKey = this.dblpKey;
				String type = currentKey;
				type = type.substring(0,type.indexOf("/"));
				
				String keyPart = currentKey.substring(currentKey.lastIndexOf("/")+1);
				String newUrl="db/"+type+"/"+cName+"/"+cName+cYear.substring(2)
				+".html#"+keyPart;
				
				this.url = newUrl;
			}
			try{
				//an InProccedings element can be in the db as both an InProccedings
				//or an article
				
				//lookup if the current in-proceedings already exists
				
				if(this.url.compareTo("")==0){
					if (this.dblpKey.compareTo("")==0)
						logger.error("Both url and key are empty");
					else
						this.findInProceedings.setString(1, this.dblpKey);
				}
				else
					this.findInProceedings.setString(1, this.url);
				
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
			
			//WORKAROUND caused by some entries which share the same url
			if( this.url.contains("http://theory.lcs.mit.edu/")){
				//we need to change these urls into a standard form. 
				//For example the url http://theory.lcs.mit.edu/~iandc/ic71.html
				//found in an article with the following tags
				//<article mdate="2005-05-03" key="journals/iandc/PatrickL71a">
				//<journal>Information and Control</journal>
				//<month>March</month>
				//<year>1971</year>
				//<volume>18</volume>
				//should be changed to 
				//db/journals/iandc/iandc18.html#PatrickL71a
				//where: iandc is the journal name found in the url, between '~' and '/'
				//18 is the volume number
				//PatrickL71a is the last part of the key
				String oldUrl = this.url;
				String jName=oldUrl.substring(oldUrl.lastIndexOf("~")+1, oldUrl.lastIndexOf("/"));
				String currentKey = this.dblpKey;
				String type = currentKey;
				type = type.substring(0,type.indexOf("/"));
				String jVolume = this.volume;
				
				String keyPart = currentKey.substring(currentKey.lastIndexOf("/")+1);
				String newUrl="db/"+type+"/"+jName+"/"+jName+
				jVolume+".html#"+keyPart;
				
				this.url = newUrl;
			}
			
			try{
				//lookup if the current article already exists
				if(this.url.compareTo("")==0){
					if (this.dblpKey.compareTo("")==0)
						logger.error("Both url and key are empty");
					else
						this.findArticle.setString(1, this.dblpKey);
				}
				else
					this.findArticle.setString(1, this.url);
				
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
				
				if(this.url.compareTo("")==0){
					if (this.dblpKey.compareTo("")==0)
						logger.error("Both url and key are empty");
					else
						this.findProceedings.setString(1, this.dblpKey);;
				}
				else
					this.findProceedings.setString(1, this.url);

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
	
	
	private String cleanStringFormat(String input){
		//need to convert each symbol (like &) back to their code. See DTD for more info.
		return input;
	}
}
