package dblp.social.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import dblp.social.exceptions.FileExistsException;
import dblp.social.exceptions.PoolException;
import dblp.social.exceptions.SodaHibernateException;
import dblp.social.exceptions.ThreadException;
import dblp.social.hibernate.ISodaHibernateSession;
import dblp.social.hibernate.SessionsPool;
import dblp.social.hibernate.SodaHibernateSessionFactory;
import dblp.social.hibernate.SodaModelProvider;
import dblp.social.importer.DblpParser;
import dblp.social.importer.DblpParserThread;
import dblp.social.importer.ThreadMonitor;
import dblp.social.loader.PartFile;
import dblp.social.loader.PartFilesPool;
import dblp.social.preparser.PreParser;
import dblp.social.preparser.XMLPartsInfo;
import dblp.social.updater.DblpUpdater;
import dblp.social.utility.Glitterizer;

/**
 * This class contains a set of methods needed to properly run the application.
 * 
 * @author Staffiero
 *
 */
public class Runner {
	
	private static final String DB_CONFIG_FILE = "./config/db.properties";
	private static final long AVG_PARTFILES_SIZE = 2*1024*1024; //2 MegaBytes
	private static final int CHECKPOINT_STEP= 50; 	
	private static final int MAX_THREADS = 4;
	
	private static Properties props;
	private static Logger logger = Logger.getLogger(Runner.class);
	private static String descriptor;
	
	/**
	 * Runs the parser in multi-thread mode
	 * @param dblpXMLfilePath the dblp XML file path
	 */
	public static void runImporterMultiThread(String dblpXMLfilePath){
		System.out.println("Running dblp importer (multithread)");
		try {
			loadDbConfigurations();
		} 
		catch (IOException e) {
			logger.error("Error while reading the db properties file");
			return;
		} 
		
		if (props==null){
			logger.error("No db config file found");
			return;
		}
		try{
			runPreParser(dblpXMLfilePath);
		}
		catch (Exception e){
			logger.error("Exception while running pre parser: ");
			e.printStackTrace();
		}
		boolean parseDone=false;
		try{
			parseDone = runParserMultiThread();
		}
		//TODO different exceptions can be handled in different ways
		catch (Exception e){
			e.printStackTrace();
		}
		if (parseDone)
			deletePartFiles();
	}
	
	/**
	 * Runs the parser in single-thread mode
	 * @param dblpXMLfilePath the dblp XML file path
	 */
	public static void runImporterSingleThread(String dblpXMLfilePath){
		System.out.println("Running dblp importer (single thread)");
		try {
			loadDbConfigurations();
		} 
		catch (IOException e) {
			logger.error("Error while reading the db properties file");
			return;
		} 
		
		if (props==null){
			logger.error("No db config file found");
			return;
		}
		try{
			runPreParser(dblpXMLfilePath);
		}
		catch (Exception e){
			logger.error("Exception while running pre parser: ");
			e.printStackTrace();
		}
		boolean parseDone=false;
		try{
			parseDone = runParserSingleThread();
		}
		//TODO different exceptions can be handled in different ways
		catch (Exception e){
			e.printStackTrace();
		}
		if (parseDone)
			deletePartFiles();
	}
	
	/**
	 * Runs the updater in multi-thread mode
	 * @param dblpXMLfilePath the dblp XML file path needed to update the current informations on the db
	 */
	public static void runUpdaterMultiThread(String dblpXMLfilePath){
		System.out.println("Running dblp updater (multithread)");
		try {
			loadDbConfigurations();
		} 
		catch (IOException e) {
			logger.error("Error while reading the db properties file");
			return;
		} 
		
		if (props==null){
			logger.error("No db config file found");
			return;
		}
		String xmlUpdateFile="";
		//run updater (prepare a new xml file to be imported)
		try{
			xmlUpdateFile = runUpdater(dblpXMLfilePath);
			deletePartFiles();
			runPreParser(xmlUpdateFile);
			boolean parseDone=false;
			parseDone = runParserMultiThread();
			if (parseDone)
				deletePartFiles();
			clearUpdateInfo();
			
		}
		//TODO different exceptions can be handled in different ways
		catch (Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * Runs the updater in single-thread mode
	 * @param dblpXMLfilePath the dblp XML file path needed to update the current informations on the db
	 */
	public static void runUpdaterSingleThread(String dblpXMLfilePath){
		System.out.println("Running dblp updater (single thread)");
		try {
			loadDbConfigurations();
		} 
		catch (IOException e) {
			logger.error("Error while reading the db properties file");
			return;
		} 
		
		if (props==null){
			logger.error("No db config file found");
			return;
		}
		String xmlUpdateFile="";
		//run updater (prepare a new xml file to be imported)
		try{
			xmlUpdateFile = runUpdater(dblpXMLfilePath);
			deletePartFiles();
			runPreParser(xmlUpdateFile);
			boolean parseDone=false;
			parseDone = runParserSingleThread();
			if (parseDone)
				deletePartFiles();
			clearUpdateInfo();
		}
		//TODO different exceptions can be handled in different ways
		catch (Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Runs the cleaner. It deletes every stored information.
	 */
	public static void runCleaner(){
		System.out.println("Cleaning all stored informations");
		try {
			if(props==null)
				loadDbConfigurations();
			//creates a SODA Hibernate session to clean the db
			ISodaHibernateSession session= 
				SodaHibernateSessionFactory.getSession(props.getProperty("dbUrl"),
					props.getProperty("dbDialect"),
					props.getProperty("dbDriverName"), 
					props.getProperty("dbUser"), 
					props.getProperty("dbPasswd"), 
					new SodaModelProvider());
			System.out.println("Cleaning database");
			session.dropSchema();
			System.out.println("All entries deleted");
			deletePartFiles();
			clearUpdateInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Runs the pre parser
	 * @param dblpXMLfilePath the path of the dblp XML file to be parsed
	 * @throws Exception 
	 * @throws IOException 
	 */
	private static void runPreParser(String dblpXMLfilePath) throws IOException, Exception {
		try{
			descriptor="";
			logger.debug("Trying to pre parse source file");
			File input = new File(dblpXMLfilePath);
			PreParser preParser=new PreParser(input);
			int partsNumber = Math.round(input.length()/AVG_PARTFILES_SIZE);
			if (partsNumber < 2)
				partsNumber=2;
			descriptor = preParser.preParse(partsNumber,CHECKPOINT_STEP);
			
			logger.debug("Pre-parse finished");
		}
		catch( FileExistsException ex){
			System.out.println("Skipping pre parser");
			//If the source has already been pre parsed sets the descriptor
			String descriptorName= dblpXMLfilePath.replaceAll("./", "");
			descriptorName = descriptorName.substring(0,descriptorName.lastIndexOf("."));
			descriptor =PreParser.SUBFOLDER+
			XMLPartsInfo.INFO_FILESUBFOLDER+
			"/"+descriptorName+XMLPartsInfo.INFO_FILENAME;
		}
		
	}
	
	/**
	 * Method reading the db configuration file, written following the java.util.Properties "standards".
	 * These properties are needed to create an Hibernate session to save the data that will be parsed.
	 * @see java.util.Properties
	 * @throws IOException
	 */
	private static void loadDbConfigurations() throws IOException{
		//Load db properties
		props = null;
		InputStream propertiesInputStream;
		props = new Properties();
		propertiesInputStream = new FileInputStream(new File(DB_CONFIG_FILE));
		if (propertiesInputStream != null) {
			props.load(propertiesInputStream);
		}
		propertiesInputStream.close();
	}
	
	/**
	 * Runs the parser in multi-thread mode
	 * 
	 * @return true if when the parser exits all the parts files have been completed, false otherwise.
	 * @throws SAXException
	 * @throws IOException
	 * @throws PoolException
	 * @throws SodaHibernateException
	 * @throws ThreadException
	 */
	private static boolean runParserMultiThread() throws SAXException, IOException, PoolException, SodaHibernateException, ThreadException{
		
		System.out.println("Starting dblp parser (multithread)");
		
		boolean result=false;
		//creates a pool of part files
		PartFilesPool.initialize(descriptor);
		PartFilesPool pool = PartFilesPool.getInstance();
		PartFile pf;
		
		//TODO this code can be removed
		//Checks time taken to parse - step 1: starting time
		Calendar begin = Calendar.getInstance();
		
		//Creates a threadMonitor which will take care of threads synchronization
		ThreadMonitor monitor = new ThreadMonitor();
		
		int max;
		//sets max number of concurrent threads. Note, this number has to be
		//lower or equal than the number of sessions in the pool. 
		//The best number of concurrent threads depends on many different aspects, 
		//anyway, according to experimental observations 3 is a reasonable choice.				
		if (pool.availableObjectsNumber()<=MAX_THREADS){
			max = pool.availableObjectsNumber();
		}
		else {
			max=MAX_THREADS;
		}
		if (pool.availableObjectsNumber()==0){
			System.out.println("No part files available. This happens because:\n" +
					"\tThere are no par files at all;\n" +
					"\tAll part files are already completely parsed");
			return true;
		}
		
		monitor.setThreadsNumber(max);
		monitor.setMaxThreads(max);
		//Creates a pool of Hibernate sessions: each thread will use one of these sessions
		//therefore the number of session has to be greater or equal than the number of
		//max concurrent thread. Too many sessions will lead to a memory leak, the 
		//chosen strategy is to create many sessions as the number of max 
		//concurrent thread
		SessionsPool.initialize(props.getProperty("dbUrl"),
				props.getProperty("dbDialect"),
				props.getProperty("dbDriverName"), 
				props.getProperty("dbUser"), 
				props.getProperty("dbPasswd"), 
				max);
		SessionsPool sessionPool = SessionsPool.getInstance();
		
		//Takes a session from the pool to update the database scheme,
		//then sends back the session to the pool	
		ISodaHibernateSession session = sessionPool.getSession();
		session.updateSchema();
		sessionPool.sessionBackToPool(session);
		session=null;
		
		
		//vars used to control the while loop launching separate threads to parse
		//different part files.
		//cont: a simple counter, if all the file in the pool can not be updated the 
		//threads will attempt to restart for 3 times 
		//(some errors as duplicate author tags can be overcome this way)
		int cont =0;
		boolean blockParser = false;
		
		
			
		//main loop: here threads will be launched until the parse is finished
		//or the application encounters an error that can not overcome by itself
		while (pool.availableObjectsNumber()>0 && !blockParser && !monitor.terminateProcess()){
			//if the previous iteration was stopped by the monitor (monitor.setCloseThreads(true)
			//we need to reset it.
			monitor.setCloseThreads(false);
			//we try to launch as many threads as possible without exceeding 
			//the max threads number
			for (int i=1; i<=monitor.getThreadsNumber();i++){
				pf = pool.getPartFile();
				if (pf!=null){
					//if a part file is already complete we don't need to parse it
					if (!pf.isComplete()){
						//initialize a new thread
						String name = "thread "+i;
						DblpParserThread thread = 
							new DblpParserThread(pf, name, monitor);
						monitor.addThread(name);
						logger.debug("Starting the thread parsing "+pf.getFile().getName());
						thread.start();
					}
				}
			}
			//waits for all threads to finish their tasks
			monitor.waitForThreads();	
			//If the parser finds an error that can not be overcome by restarting the threads 
			//it must be stopped
			//Note that the parser can recover form errors such as duplicated values 
			//for authors tag by simply restarting the threads
			//anyway it will attempt to auto recover 3 times
			if (!pool.hasBeenUpdated())
				cont++;
			else
				cont=0;
			if (cont > 3)
				blockParser = !pool.hasBeenUpdated();
			
			//to save some memory
			System.runFinalization();
			System.gc();
			
			//calibrates the threads number for the next restart, the false tag 
			//indicates that this method
			//is not called from within a running DblpParserThread
			monitor.calibrateThreads(false);
				
			//TODO this code can be removed
			//Debug code
			Runtime r = Runtime.getRuntime();
			logger.debug("Free memory: "+Glitterizer.clearFormatLenght(r.freeMemory()));
			logger.debug("Total memory: "+Glitterizer.clearFormatLenght(r.totalMemory()));
			logger.debug("Max memory: "+Glitterizer.clearFormatLenght(r.maxMemory()));
			logger.debug("All threads closed\nPart files pool contains "+
					pool.availableObjectsNumber()+" available objects");
			logger.debug("block parser is "+blockParser);
			
			//TODO Debug code
			System.out.println("Free memory: "+Glitterizer.clearFormatLenght(r.freeMemory()));
			System.out.println("Total memory: "+Glitterizer.clearFormatLenght(r.totalMemory()));
			System.out.println("Max memory: "+Glitterizer.clearFormatLenght(r.maxMemory()));
			System.out.println("All threads closed\nPart files pool contains "+
					pool.availableObjectsNumber()+" available objects");
			System.out.println("block parser is "+blockParser);
			
			if (!blockParser&& !pool.totalParseFinished()){
				System.out.println("Restarting all threads");
				logger.debug("Restarting all threads");
			}
		}
		if (monitor.terminateProcess()){
			System.out.println("Exit forced");
			logger.error("Exit forced");
		}
		else{
			if (pool.isComplete()){
				result = true;
				//TODO this code can be removed
				//Check time taken to parse (2)
				Calendar end = Calendar.getInstance();
				//sessionPool.closePool();
				long timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
				System.out.println("Parse done. Time taken:  "+timeTaken);
			}
			else{
				result = false;Calendar end = Calendar.getInstance();
				//sessionPool.closePool();
				long timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
				System.out.println("Process exited with errors. Time taken: "+timeTaken);
			}	
		}
		return result;
	}
	
	private static boolean runParserSingleThread() throws SodaHibernateException, SAXException, IOException, PoolException{
		
		System.out.println("Starting dblp parser (single thread)");
		
		boolean result=false;
		//Check time taken to parse (1)
		Calendar begin = Calendar.getInstance();
		
		
		//creates a SODA Hibernate session
		ISodaHibernateSession session = 
			SodaHibernateSessionFactory.getSession(props.getProperty("dbUrl"),
					props.getProperty("dbDialect"),
					props.getProperty("dbDriverName"), 
					props.getProperty("dbUser"), 
					props.getProperty("dbPasswd"), 
					new SodaModelProvider());
		
		session.updateSchema();
		
		
		//creates a pool of part files
		
		PartFilesPool.initialize(descriptor);
		PartFilesPool pool = PartFilesPool.getInstance();
		PartFile pf;
	
		
		DblpParser parser = new DblpParser(); 
		boolean blockParser=false;
		while (pool.availableObjectsNumber()>0 && !blockParser){
			
			
			pf = pool.getPartFile();
			parser.parseDbpl(pf, session);
			
			
			blockParser = !pool.hasBeenUpdated();
			if (pf.isComplete())
				pool.parseFinished(pf);
			else
				pool.giveBack(pf);
		}
		if (pool.isComplete()){
			result = true;
			//TODO this code can be removed
			//Check time taken to parse (2)
			Calendar end = Calendar.getInstance();
			//sessionPool.closePool();
			long timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
			System.out.println("Parse done. Time taken:  "+timeTaken);
		}
		else{
			result=false;
			System.out.println("Process exited with errors.");
		}
		return result;
	}
	
	
	private static String runUpdater(String dblpXmlFile) throws SodaHibernateException, IOException, SAXException{
		String fp=null;
		if (props!=null){
			DblpUpdater updater = new DblpUpdater(props.getProperty("dbUrl"),
					props.getProperty("dbDialect"),
					props.getProperty("dbDriverName"), 
					props.getProperty("dbUser"), 
					props.getProperty("dbPasswd"));
			fp = updater.writeUpdateFile(dblpXmlFile);
			
		}
		return fp;
	}
	
	private static void clearUpdateInfo(){
		File outputFile = new File(DblpUpdater.XML_UPDATER_FILE);
		//if a file named XML_UPDATER_FILE already exists first of all deletes it
		if (outputFile.exists())
			outputFile.delete();
	}
	
	private static void deletePartFiles(){
		//deletes the XMLPartFiles folder and all its content
		System.out.println("Cleaning temporary informations");
		String folder = PreParser.SUBFOLDER;
		folder.replaceAll("/", "");
		File partFilesFolder = new File(folder);
		if (partFilesFolder.exists() && partFilesFolder.isDirectory()){
			
			String[] fc = partFilesFolder.list();
			for (int i=0; i < fc.length; i++){
				File content = new File(partFilesFolder.getAbsolutePath()+"/"+fc[i]);
				if (content.isFile()){
					content.delete();
				}
				else if (content.isDirectory()){
					String[] sfc = content.list();
					for (int j=0; j < sfc.length; j++){
						File subContent = new File(content.getAbsolutePath()+"/"+sfc[j]);
						subContent.delete();
					}
					content.delete();
				}
			}
			partFilesFolder.delete();
			System.out.println("All temporary elements deleted");
		}
	}
}
