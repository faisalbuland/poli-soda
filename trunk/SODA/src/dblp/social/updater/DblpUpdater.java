package dblp.social.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.io.FileInputStream;


import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.apache.log4j.Logger;

import java.sql.Connection;

/**
 * This class provides methods to fetch a dblp XML file and to create another dblp XML file which contains only new entries (not already stored in the database).
 * 
 * @author Staffiero
 *
 */
public class DblpUpdater {
	
	public static final String XML_UPDATER_FILE = "database_update_file.xml";
	static Logger logger = Logger.getLogger(DblpUpdater.class.getName());
	private String dbUrl, dbDriverName, dbUser, dbPasswd; 
	private Connection con;
	
	/**
	 * The constructor to be used.
	 * 
	 * @param dbUrl
	 * @param dbDialect
	 * @param dbDriverName
	 * @param dbUser
	 * @param dbPasswd
	 */
	public DblpUpdater(String dbUrl, String dbDialect, String dbDriverName,
			String dbUser, String dbPasswd) {
		super();
		this.dbUrl = dbUrl;
		this.dbDriverName = dbDriverName;
		this.dbUser = dbUser;
		this.dbPasswd = dbPasswd;
		this.con=null;
	}

	/**
	 * Prepares the XML file which will be used to update the database. 
	 * If a file named XML_UPDATER_FILE already exists it will be deleted.
	 * 
	 * @param dblpXmlFile
	 * @return null in case of error
	 * @throws IOException 
	 */
	private File prepareUpdateXML(String dblpXmlFile) throws IOException{
		//if the given string is null returns null
		if (dblpXmlFile==null)
			return null;
		File inputFile = new File(dblpXmlFile);
		//if the file does not exists returns null
		if (!inputFile.exists())
			return null;
		File outputFile = new File(XML_UPDATER_FILE);
		//if a file named XML_UPDATER_FILE already exists first of all deletes it
		if (outputFile.exists())
			outputFile.delete();
		outputFile.createNewFile();
		return outputFile;
	}
	/**
	 * Fetches the given XML file and writes the XML_UPDATER_FILE, an XML file containing all the entries found in the given XML file but not in the database.
	 * 
	 * @param dblpXmlFile
	 * @return the absolute path of the xml update file
	 * @throws IOException
	 * @throws SAXException
	 */
	public String writeUpdateFile(String dblpXmlFile) throws IOException{
		
		String updateFilePath="";
		//checks if the source file exists
		File input = new File(dblpXmlFile);
		
		if (!input.exists())
			throw new FileNotFoundException("File "+dblpXmlFile+" not found");
		

		String databaseUrl = "jdbc:"+this.dbUrl;	
		try{
			Class.forName(this.dbDriverName);
			this.con = 
				DriverManager.getConnection(databaseUrl, this.dbUser,this.dbPasswd);
		}
		catch (Exception e){
			logger.error("Cannot open a connection to the database: "+e.getMessage());
			e.printStackTrace();
		}

		//creates an XMLReader
		File output;
		if ((output = prepareUpdateXML(dblpXmlFile))!=null){
			updateFilePath=output.getAbsolutePath();
			//Sax parser runs out of memory if used on xml files grater than few 10s of mbytes. 
			//It is caused by a bug, fixed 
			//in jre1.6_14 which is still in beta version. Plus, Sax converts each
			//encoded character , such as &egrave; or similar, leading to undesired
			//behaviors in the further parsing. Therefore it is better to use an
			//ad hoc parser			
			try {
				Calendar begin = Calendar.getInstance();
				System.out.println("Looking for new entries");
				
				//parses the given XML file		
				DblpUpdaterParser parser = new DblpUpdaterParser(this.con, input, output);
				parser.parse();
				try {
					this.con.close();
				} catch (SQLException e) {
					logger.error("Cannot close connection: "+e.getMessage());
					e.printStackTrace();
				}
				Calendar end = Calendar.getInstance();
				long timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
				System.out.println("All new entries stored in "+XML_UPDATER_FILE+
						"\nTime taken:  "+timeTaken);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
			throw new IOException("Error preparing the xml file");
		return updateFilePath;
	}
	
	/**
	 * Test method to check new entries using SAX. Due to a SAX bug, in order to run this method with a large XML file (around 20MB or larger) jre1.6.0_14 or higher is required
	 * 
	 * @param dblpXmlFile
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public String testUpdateFile(String dblpXmlFile) throws IOException, SAXException{
		//Using SAX to test behaviors
		String updateFilePath="";
		//checks if the source file exists
		File input = new File(dblpXmlFile);
		
		if (!input.exists())
			throw new FileNotFoundException("File "+dblpXmlFile+" not found");
		

		String databaseUrl = "jdbc:"+this.dbUrl;	
		try{
			Class.forName(this.dbDriverName);
			this.con = 
				DriverManager.getConnection(databaseUrl, this.dbUser,this.dbPasswd);
		}
		catch (Exception e){
			logger.error("Cannot open a connection to the database: "+e.getMessage());
			e.printStackTrace();
		}
		FileInputStream fis=null;

		//creates an XMLReader
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		File output;
		if ((output = prepareUpdateXML(dblpXmlFile))!=null){
			updateFilePath=output.getAbsolutePath();
				
			//creates an updater content handler
			DefaultHandler handl = new TestHandler(this.con, output);
			xmlReader.setContentHandler(handl);
			
			try {
				fis = new FileInputStream(input);
				Calendar begin = Calendar.getInstance();
				System.out.println("Looking for new entries");
				
				//parses the given XML file		
				xmlReader.parse(new InputSource(fis));
				fis.close();
				try {
					this.con.close();
				} catch (SQLException e) {
					logger.error("Cannot close connection: "+e.getMessage());
					e.printStackTrace();
				}
				Calendar end = Calendar.getInstance();
				long timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
				System.out.println("All new entries stored in "+XML_UPDATER_FILE+
						"\nTime taken:  "+timeTaken);
				
			} catch (Exception e) {
				fis.close();
				e.printStackTrace();
			}
		}
		else
			throw new IOException("Error preparing the xml file");
		return updateFilePath;
	}
	
}
