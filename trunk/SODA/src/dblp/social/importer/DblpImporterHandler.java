package dblp.social.importer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dblp.social.exceptions.ThreadException;
import dblp.social.hibernate.ISodaHibernateSession;
import dblp.social.hibernate.entities.Article;
import dblp.social.hibernate.entities.Book;
import dblp.social.hibernate.entities.DblpPublication;
import dblp.social.hibernate.entities.InCollection;
import dblp.social.hibernate.entities.InProceedings;
import dblp.social.hibernate.entities.Journal;
import dblp.social.hibernate.entities.Person;
import dblp.social.hibernate.entities.Proceedings;
import dblp.social.loader.PartFile;

/**
 * This class defines the handler that allows the SAX XML Reader to parse, model and store the dblp data using Hibernate.
 * @see org.xml.sax.helpers.DefaultHandler
 * 
 * @author Staffiero
 *
 */
public class DblpImporterHandler extends DefaultHandler{

	static Logger logger = Logger.getLogger(DblpImporterHandler.class.getName());
	private static final String INCOLLECTION = "incollection";
	private static final String INPROCEEDINGS = "inproceedings";
	private static final String PROCEEDINGS = "proceedings";
	private static final String ARTICLE = "article";
	private static final String JOURNAL = "journal";
	private static final String BOOK = "book";
	private static final String TITLE = "title";
	private static final String BOOKTITLE = "booktitle";
	private static final String YEAR = "year";
	private static final String ISBN = "isbn";
	private static final String PUBLISHER = "publisher";
	private static final String AUTHOR = "author";
	private static final String PAGES = "pages";
	private static final String EDITOR = "editor";
	private static final String URL = "url";
	private static final String NUMBER = "number";
	private static final String VOLUME = "volume";
	private static final String EE = "ee";
	private static final String PHDTHESIS = "phdthesis";
	private static final String MASTERTHESIS= "mastersthesis";
	private static final String WWW = "www";
	private static final String CHECKPOINT = "checkpoint";
	
	
	private String currDblpElement = "";
	private ISodaHibernateSession session;
	private Object currEntity = null;
	private Journal tempJournal = new Journal();
	private ArrayList<String> fakeProceedings = new ArrayList<String>();
	private ArrayList<Person> currAuthors = new ArrayList<Person>();
	private ArrayList<Person> currEditors = new ArrayList<Person>();
	private ArrayList<String> currElementFragments = new ArrayList<String>();
	private int startingCheckpoint=0;
	private int currCheckpoint=0;
	private int startingOffset=0;
	private int currOffset=0;
	private PartFile partFile;
	private ThreadMonitor monitor=null;
	/**
	 * The constructor to be used when parsing each part file sequentially.
	 * @param session the ISodaHibernateSession through which the dblp data extracted will be made persistent
	 * @param partFile the PartFile object representing a single _partFile
	 */
	public DblpImporterHandler(ISodaHibernateSession session, PartFile partFile){
		super();
		this.startingCheckpoint = partFile.getLastCheckpoint();
		this.startingOffset = partFile.getLastOffset()+1;	
		this.partFile=partFile;
		this.session = session;
	}
	
	/**
	 * The constructor to be used when parsing each part file within a thread.
	 * @param session  the ISodaHibernateSession through which the dblp data extracted will be made persistent
	 * @param partFile the PartFile object representing a single _partFile
	 * @param monitor the thread monitor, used to coordinate threads
	 */
	public DblpImporterHandler(ISodaHibernateSession session, PartFile partFile, ThreadMonitor monitor){
		super();
		this.startingCheckpoint = partFile.getLastCheckpoint();
		this.startingOffset = partFile.getLastOffset()+1;	
		this.partFile=partFile;
		this.session = session;
		this.monitor = monitor;
	}

	/**
	 * Method called every time the parser enters a new XML element.
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		//Increment offset counter (always when entering a main dblp tag)
		if(localName.equalsIgnoreCase(BOOK)|| 
				localName.equalsIgnoreCase(PROCEEDINGS) ||
				localName.equalsIgnoreCase(INCOLLECTION) ||
				localName.equalsIgnoreCase(INPROCEEDINGS) ||
				localName.equalsIgnoreCase(ARTICLE) ||
				localName.equalsIgnoreCase(PHDTHESIS) ||
				localName.equalsIgnoreCase(MASTERTHESIS) ||
				localName.equalsIgnoreCase(WWW)){
			this.currOffset++;
		}
		
		
		/*
		 * If the current checkpoint is higher than the starting checkpoint 
		 * or if the current checkpoint is equal to the starting checkpoint and the current 
		 * offset is greater or equal to the starting offset
		 * the parse proceeds normally
		 * otherwise the parser only increments the current checkpoint counter
		 * 
		 */
		
		if((this.currCheckpoint>this.startingCheckpoint) || 
				(this.currCheckpoint==this.startingCheckpoint && 
						this.currOffset>=this.startingOffset)){
			//Here I check what node I'm entering
			//First I check if it's what I call a "Dblp element" (article, inProceedings, book chapter, book, etc.)
			if(localName.equalsIgnoreCase(BOOK)){
				logger.debug("Entering a dblp book element " + attributes.getValue("key"));
				logger.debug("Creating a Book instance");			
				this.currEntity = new Book();
				((Book)this.currEntity).setDblpKey(attributes.getValue("key"));
				((Book)this.currEntity).setId(attributes.getValue("key"));
				this.currDblpElement = localName;
				
			}
			else if(localName.equalsIgnoreCase(PROCEEDINGS)){
				logger.debug("Entering a dblp proceedings element " + attributes.getValue("key"));
				logger.debug("Creating a Proceedings instance");
				this.currEntity = new Proceedings();
				((Proceedings)this.currEntity).setDblpKey(attributes.getValue("key"));
				((Proceedings)this.currEntity).setId(attributes.getValue("key"));
				this.currDblpElement = localName;
			}	
			else if(localName.equalsIgnoreCase(INCOLLECTION)){
				logger.debug("Entering a dblp incollection element " + attributes.getValue("key"));
				logger.debug("Creating an InCollection instance");
				this.currEntity = new InCollection();
				((InCollection)this.currEntity).setDblpKey(attributes.getValue("key"));
				((InCollection)this.currEntity).setId(attributes.getValue("key"));
				this.currDblpElement = localName;
			}	
			else if(localName.equalsIgnoreCase(INPROCEEDINGS)){
				logger.debug("Entering a dblp inproceedings element " + attributes.getValue("key"));
				logger.debug("Creating an InProceedings instance");
				this.currEntity = new InProceedings();
				((InProceedings)this.currEntity).setDblpKey(attributes.getValue("key"));
				((InProceedings)this.currEntity).setId(attributes.getValue("key"));
				this.currDblpElement = localName;
			}	
			else if(localName.equalsIgnoreCase(ARTICLE)){
				logger.debug("Entering a dblp article element " + attributes.getValue("key"));
				logger.debug("Creating an Article instance");
				this.currEntity = new Article();
				((Article)this.currEntity).setDblpKey(attributes.getValue("key"));
				((Article)this.currEntity).setId(attributes.getValue("key"));
				this.currDblpElement = localName;
			}
			//Code added to handle checkpoints
			else if (localName.equalsIgnoreCase(CHECKPOINT)){
				logger.debug("Entering a checkpoint element");
				int chptNumber = Integer.parseInt(attributes.getValue("number"));
				this.partFile.setLastCheckpoint(chptNumber);
				this.partFile.setLastOffset(0);
				this.currCheckpoint = chptNumber;
				this.currOffset=0;
				if (chptNumber>this.startingCheckpoint){
					try{
						//Saves the current progress
						this.partFile.setLastCheckpoint(this.currCheckpoint);
						this.partFile.setLastOffset(this.currOffset);
						//if (this.monitor!=null)
						//	monitor.getLockOnDescriptor();
						//this.partFile.updateDescriptor();
						//if (this.monitor!=null)
						//	monitor.releaseLockOnDescriptor();
						
						//if the application is running in multi-thread mode
						//checks if the current thread has to be closed
						if (this.monitor!=null){
							if(this.monitor.closeThreads()){
								this.monitor.getLockOnDescriptor();
								this.partFile.updateDescriptor();
								this.monitor.releaseLockOnDescriptor();
								this.session.close();
								throw new ThreadException("Forcing thread termination");
							}
								
						}
						//Checks the available memory. 
						Runtime r = Runtime.getRuntime();
						long totalMemory =r.totalMemory();
						long maxMemory = r.maxMemory();
						long freeMemory =r.freeMemory();
						//if the application is running in multi-thread mode
						if (this.monitor!=null){
							//dynamically calibrates the number of running threads
							try{
								//when calibrateThreads is called from within
								//a thread (with the "true" argument)
								//the method will throw a ThreadException if the
								//current thread has to be closed
								monitor.calibrateThreads(true);
							}
							catch (ThreadException te){
								//before exiting the parser we need to save the current state
								this.monitor.getLockOnDescriptor();
								this.partFile.updateDescriptor();
								this.monitor.releaseLockOnDescriptor();
								
								//forwards the exception
								throw te;
							}
							//When an OutOfMemoryError is thrown we have no guarantee that we will
							//be able to save the current state. If the current state is not saved the
							//application will fail to restart.
							//To prevent memory leaks:
							//if the amount of free memory is too low <=500kB forces each 
							//thread to terminate setting monitor.terminateProcess=true
							if (totalMemory==maxMemory&&freeMemory<(500*1024)){
								if (this.monitor!=null){
									this.monitor.setCloseThreads(true);
									this.monitor.setTerminateProcess(true);
									//before exiting the parser we need to save the current state
									this.monitor.getLockOnDescriptor();
									this.partFile.updateDescriptor();
									this.monitor.releaseLockOnDescriptor();
								}
								r.gc();
								throw new Exception("Forcing thread termination " +
										"to prevent OutOfMemoryError");
							}
						}
					}
					catch (Exception e){
						//Wraps each exception into a SAXExceprion
						throw new SAXException(e);
					}
				}
			}
			//If it's not any of those elements, It must be what I call a "Dblp attribute", which specifies a specific attribute (title, year, author, isbn, etc.) of a "Dblp element"
			else 
				logger.debug("Found" + localName + "for a " + this.currDblpElement + " element");
			
		}
		else{
			//currCheckpoint< startingCheckpoint
			//we only care to reach the next checkpoint
			if (localName.equalsIgnoreCase(CHECKPOINT)){
				int chptNumber = Integer.parseInt(attributes.getValue("number"));
				this.currCheckpoint = chptNumber;
				this.currOffset=0;
				this.partFile.setLastCheckpoint(this.currCheckpoint);
			}
			else 
				this.partFile.setLastOffset(this.currOffset);
		}
	}

	/**
	 * Method called every time the parser exits an element.
	 */
	@SuppressWarnings("unchecked")
	public void endElement(String uri, String localName, String qName) throws SAXException{
		/*
		 * If the current checkpoint is higher than the starting checkpoint 
		 * or if the current checkpoint is the starting checkpoint and the current 
		 * offset is greater or equal to the starting offset
		 * the parse proceeds normally
		 * otherwise the parser does nothing on endElement
		 * 
		 */
		if((this.currCheckpoint>this.startingCheckpoint) || 
				(this.currCheckpoint==this.startingCheckpoint && 
						this.currOffset>=this.startingOffset)){
			if(localName.equalsIgnoreCase(PROCEEDINGS)){
				logger.debug("Exiting the proceedings dblp element " + ((Proceedings)this.currEntity).getDblpKey() + ", proceeding to save its hibernate entity");
				try{
					//Set the Proceedings id
					String id = DblpIdUtility.createProceedingsId(((Proceedings)this.currEntity).getId(),
							((Proceedings)this.currEntity).getDblpKey());
					if (id==null)
						throw new SAXException("Could not create a valid id for the inproceedings element"+((Proceedings)this.currEntity).getDblpKey());
					((Proceedings)this.currEntity).setId(id);		
				}
				catch (Exception e){
					try{
						//Before exiting the parser updates the xml parts descriptor in order to persist 
						//the current state 
						if (monitor!=null)
							monitor.getLockOnDescriptor();
						this.partFile.updateDescriptor();
						if (monitor!=null)
							monitor.releaseLockOnDescriptor();
						//when a thread is closed after an exception forces the 
						//termination of each other thread
						if (monitor!=null)
							this.monitor.setCloseThreads(true);
						this.session.clear();
						this.session.close();
						logger.error("Error setting id for a Proceedings element:\n" + e.getMessage());
					}
					catch(Exception ne){
						throw new SAXException(ne);
					}
					throw new SAXException(e);
				}
				writeProceedings();
				logger.debug("Hibernate entity for proceedings element " + ((Proceedings)this.currEntity).getDblpKey() + " saved on the DB");
				
			}	
			else if(localName.equalsIgnoreCase(BOOK)){
				logger.debug("Exiting the book dblp element " + ((Book)this.currEntity).getDblpKey() + ", proceeding to save its hibernate entity");
				writeBook();
				logger.debug("Hibernate entity for book element " + ((Book)this.currEntity).getDblpKey() + " saved on the DB");
			}
			else if(localName.equalsIgnoreCase(INCOLLECTION)){
				logger.debug("Exiting the incollection element " + ((DblpPublication)this.currEntity).getDblpKey() + ", proceeding to save its hibernate entity");
				writeInCollection();
				logger.debug("Hibernate entity for incollection element " + ((InCollection)this.currEntity).getDblpKey() + " saved on the DB");
			}
			else if(localName.equalsIgnoreCase(INPROCEEDINGS)){
				logger.debug("Exiting the inproceedings element " + ((DblpPublication)this.currEntity).getDblpKey() + ", proceeding to save its hibernate entity");
				
				try{
					//Set the InProceedings id
					String id = DblpIdUtility.createInProceedingsId(((InProceedings)this.currEntity).getUrl(), 
						((DblpPublication)this.currEntity).getDblpKey(),
						(((InProceedings)this.currEntity).getYear()).substring(2),
						INPROCEEDINGS);
					if (id==null)
						throw new SAXException("Could not create a valid id for the inproceedings element"+((DblpPublication)this.currEntity).getDblpKey());
					((InProceedings)this.currEntity).setId(id);
				}
				catch (Exception e){
					try{
						//Before exiting the parser updates the xml parts descriptor in order to persist 
						//the current state 
						if (monitor!=null)
							monitor.getLockOnDescriptor();
						this.partFile.updateDescriptor();
						if (monitor!=null)
							monitor.releaseLockOnDescriptor();
						//when a thread is closed after an exception forces the 
						//termination of each other thread
						if (monitor!=null)
							this.monitor.setCloseThreads(true);
						this.session.clear();
						this.session.close();
						logger.error("Error setting id for an InProceedings element:\n" + e.getMessage());
					}
					catch(Exception ne){
						throw new SAXException(ne);
					}
					throw new SAXException(e);
				}
				writeInProceedings();
				logger.debug("Hibernate entity for inproceedings element " + ((InProceedings)this.currEntity).getDblpKey() + " saved on the DB");
			}
			else if(localName.equalsIgnoreCase(ARTICLE)){
				logger.debug("Exiting the article element " + ((DblpPublication)this.currEntity).getDblpKey() + ", proceeding to save its hibernate entity");
				try{
					//Set the Article id
					String id = DblpIdUtility.createInProceedingsId(((Article)this.currEntity).getUrl(),
							((DblpPublication)this.currEntity).getDblpKey(),
							this.tempJournal.getVolume(),
							ARTICLE);
					if (id==null)
						throw new SAXException("Could not create a valid id for the article element"+((DblpPublication)this.currEntity).getDblpKey());
					((Article)this.currEntity).setId(id);
				}
				catch (Exception e){
					try{
						//Before exiting the parser updates the xml parts descriptor in order to persist 
						//the current state 
						if (monitor!=null)
							monitor.getLockOnDescriptor();
						this.partFile.updateDescriptor();
						if (monitor!=null)
							monitor.releaseLockOnDescriptor();
						//when a thread is closed after an exception forces the 
						//termination of each other thread
						if (monitor!=null)
							this.monitor.setCloseThreads(true);
						this.session.clear();
						this.session.close();
						logger.error("Error setting id for an Article element:\n" + e.getMessage());
					}
					catch(Exception ne){
						throw new SAXException(ne);
					}
					throw new SAXException(e);
				}
				writeArticle();
				logger.debug("Hibernate entity for article element " + ((Article)this.currEntity).getDblpKey() + " saved on the DB");
			}
			else if(localName.equalsIgnoreCase(AUTHOR)){
				String authorName = "";
				for(String frag : this.currElementFragments)
					authorName = authorName + frag;
				try{
					Person author = null;
					List authors = 
						session.getNamedQuery("findPersonByName")
						.setString("personName", authorName)
						.list();
					if(authors!=null && !authors.isEmpty()){
						author = (Person)authors.get(0);
					}
					else{
						author = new Person(authorName);
						this.currAuthors.add(author);
					}
					if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION)||
							this.currDblpElement.equals(INPROCEEDINGS)||
							this.currDblpElement.equals(ARTICLE)){
						if (!((DblpPublication)this.currEntity).getAuthors().contains(author)){
							//if the current entity does not already contain the given author the author
							//object is added to the authors list
							((DblpPublication)this.currEntity).addAuthor(author);
							logger.debug("Added author: " + authorName + " to " + this.currEntity.getClass().getSimpleName() + " " + ((DblpPublication)this.currEntity).getId());
						}
					}
					else if(this.currDblpElement.equals(BOOK)){
						if (!((Book)this.currEntity).getAuthors().contains(author)){
							//if the current entity does not already contain the given author the author
							//object is added to the authors list
							((Book)this.currEntity).addAuthor(author);
							logger.debug("Added author: " + authorName + " to a book");
						}
					}
				}   //Handles each exception in the same way
				catch (Exception e){
					try{
						//Rollbacks the current transaction
						this.session.rollbackTransaction();
						//Before exiting the parser updates the xml parts descriptor in order to persist 
						//the current state 
						if (monitor!=null)
							monitor.getLockOnDescriptor();
						this.partFile.updateDescriptor();
						if (monitor!=null)
							monitor.releaseLockOnDescriptor();
						//when a thread is closed after an exception forces the 
						//termination of each other thread
						if (monitor!=null)
							this.monitor.setCloseThreads(true);
						this.session.clear();
						this.session.close();
						logger.error("Error processing an author element:\n" + e.getMessage());
					}
					catch(Exception ne){
						throw new SAXException(ne);
					}
					throw new SAXException(e);
				}
			}
			else if(localName.equalsIgnoreCase(TITLE)){
				String title = "";
				for(String frag : this.currElementFragments)
					title = title + frag;
				title = cleanStringFormat(title);
				//Now I check to which Dblp element does this title belong to
				if(this.currDblpElement.equalsIgnoreCase(BOOK)){
					((Book)this.currEntity).setTitle(title);
					logger.debug("Added title: " + title + " to Book " + ((Book)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(PROCEEDINGS)){
					((Proceedings)this.currEntity).setTitle(title);
					logger.debug("Added title: " + title + " to Proceedings " + ((Proceedings)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION) ||
						this.currDblpElement.equalsIgnoreCase(INPROCEEDINGS) ||
						this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					((DblpPublication)this.currEntity).setTitle(title);
					logger.debug("Added title: " + title + " to " + this.currEntity.getClass().getSimpleName() + " " + ((DblpPublication)this.currEntity).getDblpKey());
				}
			}
			else if(localName.equalsIgnoreCase(BOOKTITLE)){
				String bookTitle = "";
				for(String frag : this.currElementFragments)
					bookTitle = bookTitle + frag;
				//Now I check to which Dblp element does this booktitle belong to
				if(this.currDblpElement.equalsIgnoreCase(BOOK)){
					((Book)this.currEntity).setBooktitle(bookTitle);
					logger.debug("Added booktitle: " + bookTitle + " to Book " + ((Book)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(INPROCEEDINGS)){
					((InProceedings)this.currEntity).setBooktitle(bookTitle);
					logger.debug("Added booktitle: " + bookTitle + " to InProceedings " + ((DblpPublication)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION)){
					((InCollection)this.currEntity).setBooktitle(bookTitle);
					logger.debug("Added booktitle: " + bookTitle + " to InCollection " + ((DblpPublication)this.currEntity).getDblpKey());
				}
			}
			else if(localName.equalsIgnoreCase(YEAR)){
				String year = "";
				for(String frag : this.currElementFragments)
					year = year + frag;
				//Now I check to which Dblp element does this year belong to
				if(this.currDblpElement.equalsIgnoreCase(BOOK)){
					((Book)this.currEntity).setYear(year);
					logger.debug("Added year: " + year + " to Book " + ((Book)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(PROCEEDINGS)){
					((Proceedings)this.currEntity).setYear(year);
					logger.debug("Added year: " + year + " to Proceedings " + ((Proceedings)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION) ||
						this.currDblpElement.equalsIgnoreCase(INPROCEEDINGS) ||
						this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					((DblpPublication)this.currEntity).setYear(year);
					logger.debug("Added year: " + year + " to " + this.currEntity.getClass().getSimpleName() + " " + ((DblpPublication)this.currEntity).getDblpKey());
				}
			}//TODO 
			else if(localName.equalsIgnoreCase(URL)){
				String url = "";
				for(String frag : this.currElementFragments)
					url = url + frag;
				
				if(this.currDblpElement.equalsIgnoreCase(INPROCEEDINGS) ||
						this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					((DblpPublication)this.currEntity).setUrl(url);
				}else if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION)){
					((DblpPublication)this.currEntity).setUrl(url);
					((DblpPublication)this.currEntity).setId(url);
					logger.debug("Added internal url and id: " + url + " to " + this.currEntity.getClass().getSimpleName() + " " + ((DblpPublication)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(BOOK)){				
					((Book)this.currEntity).setId(url);
					logger.debug("Added id: " + url + " to " + this.currEntity.getClass().getSimpleName() + " " + ((Book)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(PROCEEDINGS)){
					((Proceedings)this.currEntity).setId(url);
					logger.debug("Added id: " + url + " to " + this.currEntity.getClass().getSimpleName() + " " + ((Proceedings)this.currEntity).getDblpKey());
				}
			}
			else if(localName.equalsIgnoreCase(EDITOR)){
				String editorName = "";
				for(String frag : this.currElementFragments)
					editorName = editorName + frag;
				//Now I check to which Dblp element does this publisher belong to
				try {
					//First I check whether that Author already exists
					//If not, I create a new one, otherwise I fetch the existing one.
					Person editor = null;
					List persons = 
						session.getNamedQuery("findPersonByName")
						.setString("personName", editorName)
						.list();
					
					if(persons!=null && !persons.isEmpty())
						editor = (Person)persons.get(0);
					else{
						editor = new Person(editorName);
						this.session.startTransaction();
						this.session.saveObject(editor);
						this.session.endTransaction();
						this.session.flush();
					}
					if(this.currDblpElement.equalsIgnoreCase(BOOK)){
						((Book)this.currEntity).addEditor(editor);
						logger.debug("Added editor: " + editorName + " to a book");
					}
				} catch (Exception e) {
					try{
						if (monitor!=null)
							monitor.getLockOnDescriptor();
						this.partFile.updateDescriptor();
						if (monitor!=null)
							monitor.releaseLockOnDescriptor();
						//when a thread is closed forces the termination of each other thread
						if (monitor!=null)
							this.monitor.setCloseThreads(true);
						logger.error("SodaHibernateException encountered while fetching a user named " + editorName + " from the db.\n" + e.getMessage());
						this.session.rollbackTransaction();
					}
					catch(Exception ne){
						throw new SAXException(ne);
					}
					throw new SAXException(e);
				}
			}
			else if(localName.equalsIgnoreCase(PAGES)){
				String pages = "";
				for(String frag : this.currElementFragments)
					pages = pages + frag;
				if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION) || 
						this.currDblpElement.equalsIgnoreCase(INPROCEEDINGS) ||
						this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					((DblpPublication)this.currEntity).setPageRange(pages);
					logger.debug("Added pages: " + pages + " to " + this.currEntity.getClass().getSimpleName() + " " + ((DblpPublication)this.currEntity).getDblpKey());
				}
				else
					logger.error("The parser ended up in a <pages> element without coming from an InCollection, InProceedings or Article!");
			}	
			else if(localName.equalsIgnoreCase(EE)){
				String ee = "";
				for(String frag : this.currElementFragments)
					ee = ee + frag;
				if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION) || 
						this.currDblpElement.equalsIgnoreCase(INPROCEEDINGS) ||
						this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					((DblpPublication)this.currEntity).setEe(ee);
					logger.debug("Added ee: " + ee + " to " + this.currEntity.getClass().getSimpleName() + " " + ((DblpPublication)this.currEntity).getDblpKey());
				}
			}
			else if(localName.equalsIgnoreCase(JOURNAL)){
				String journalTitle = "";
				for(String frag : this.currElementFragments)
					journalTitle = journalTitle + frag;
				if(this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					this.tempJournal.setTitle(journalTitle);
				}
			}
			else if(localName.equalsIgnoreCase(NUMBER)){
				String number = "";
				for(String frag : this.currElementFragments)
					number = number + frag;
				if(this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					this.tempJournal.setNumber(number);
				}
			}
			else if(localName.equalsIgnoreCase(VOLUME)){
				String volume = "";
				for(String frag : this.currElementFragments)
					volume = volume + frag;
				if(this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					this.tempJournal.setVolume(volume);
				}	
			}
			else if(localName.equalsIgnoreCase(PUBLISHER)){
				String publisher = "";
				for(String frag : this.currElementFragments)
					publisher = publisher + frag;
				//Now I check to which Dblp element does this publisher belong to
				if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION) || 
						this.currDblpElement.equalsIgnoreCase(INPROCEEDINGS) ||
						this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					((DblpPublication)this.currEntity).setPublisher(publisher);
					logger.debug("Added publisher: " + publisher + " to " + this.currEntity.getClass().getSimpleName() + " " + ((DblpPublication)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equalsIgnoreCase(BOOK)){
					((Book)this.currEntity).setPublisher(publisher);
					logger.debug("Added publisher: " + publisher + " to Book " + ((Book)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equals(JOURNAL)){
					((Journal)this.currEntity).setPublisher(publisher);
					logger.debug("Added publisher: " + publisher + " to a journal " + ((Journal)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equals(PROCEEDINGS)){
					((Proceedings)this.currEntity).setPublisher(publisher);
					logger.debug("Added publisher: " + publisher + " to a proceedings " + ((Proceedings)this.currEntity).getDblpKey());
				}
			}
			else if(localName.equalsIgnoreCase(ISBN)){
				String isbn = "";
				for(String frag : this.currElementFragments)
					isbn = isbn + frag;
				if(this.currDblpElement.equalsIgnoreCase(INCOLLECTION) || 
						this.currDblpElement.equalsIgnoreCase(INPROCEEDINGS) ||
						this.currDblpElement.equalsIgnoreCase(ARTICLE)){
					((DblpPublication)this.currEntity).setIsbn(isbn);
					logger.debug("Added isbn: " + isbn + " to " + this.currEntity.getClass().getSimpleName() + " " + ((DblpPublication)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equals(BOOK)){
					((Book)this.currEntity).setIsbn(isbn);
					logger.debug("Added isbn: " + isbn + " to a book " + ((Book)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equals(JOURNAL)){
					((Journal)this.currEntity).setIsbn(isbn);
					logger.debug("Added isbn: " + isbn + " to a journal " + ((Journal)this.currEntity).getDblpKey());
				}
				else if(this.currDblpElement.equals(PROCEEDINGS)){
					((Proceedings)this.currEntity).setIsbn(isbn);
					logger.debug("Added isbn: " + isbn + " to a proceedings" + ((Proceedings)this.currEntity).getDblpKey());
				}
			}
			this.currElementFragments = new ArrayList<String>();
		}//end if
	}
	/**
	 * Method called when the parser begins to read new XML document.
	 */
	public void startDocument(){
		this.currCheckpoint=0;
		this.currOffset=0;
		logger.debug("Starting the parsing of "+this.partFile.getFile().getName()+" (entered the actual xml parser)");
	}
	/**
	 *  Method called when the parser reaches the end of document.
	 */
	public void endDocument(){		
		try{
			this.partFile.setLastCheckpoint(this.currCheckpoint);
			this.partFile.setLastOffset(this.currOffset);
			this.partFile.setComplete(true);
			if (monitor!=null)
				monitor.getLockOnDescriptor();
			this.partFile.updateDescriptor();
			if (monitor!=null)
				monitor.releaseLockOnDescriptor();
			logger.debug("Parsing of "+this.partFile.getFile().getName()+
					" file done");
			if (monitor!=null){
				monitor.setCloseThreads(true);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method reading the data inside an XML element.
	 */
	public void characters (char ch[], int start, int length){
		//With this call I get the value of the current element I'm reading.
		//For example, I get the value of a title element.
		//Since only the elements that I call "Dblp attributes" (title, year, isbn etc.) contain useful data,
		//I will only fetch data if the current element is one of those.
		
		//Moreover it is useless to store strings that will be skipped, therefore
		//we will store strings in this.currElementFragments only if the 
		//current state(checkpoint, offset) is higher than the starting state
		//(startingCheckpoint, startingOffset)
		
		if((this.currCheckpoint>this.startingCheckpoint) || 
				(this.currCheckpoint==this.startingCheckpoint && 
						this.currOffset>=this.startingOffset)){
			String nodeValue = new String(ch,start,length);
			this.currElementFragments.add(nodeValue);
		}
	}
	
	private String cleanStringFormat(String in){
		String out = in;
		if (out.contains(" & "))
			out.replaceAll(" & ", " and ");
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private void writeProceedings() throws SAXException{
		String proceedingsId="";
		try {
			proceedingsId = ((Proceedings)this.currEntity).getId();
			//if the parser is running in multi-thread mode first of all 
			//gets the lock on the proceedings element
			if (this.monitor!=null)
				this.monitor.getLockOnProceedings(proceedingsId);
			//checks if the current book element already exists in the db
			List existingProceedings = 
				session.getNamedQuery("findProceedingsById")
				.setString("proceedingsId", proceedingsId)
				.list();
			this.session.startTransaction();
			if(existingProceedings.size()==1){
				//if the proceedings already exists updates the existing object
				Proceedings tempProc = (Proceedings)existingProceedings.get(0);
				//refreshes the proceedings element read from the db
				session.refresh(tempProc);
				//updates the proceedings element
				tempProc.setDblpKey(((Proceedings)this.currEntity).getDblpKey());
				tempProc.setIsbn(((Proceedings)this.currEntity).getIsbn());
				tempProc.setPublisher(((Proceedings)this.currEntity).getPublisher());
				tempProc.setTitle(((Proceedings)this.currEntity).getTitle());
				tempProc.setYear(((Proceedings)this.currEntity).getYear());
				tempProc.setEditors(((Proceedings)this.currEntity).getEditors());
				this.session.update(tempProc);
			}	
			else if(existingProceedings.size()==0){
				this.session.saveObject(this.currEntity);
			}	
			else if(existingProceedings.size()>1)
				logger.error("Found more than one book with the same id " + proceedingsId);
			//Clean up some temp info I stored
			this.session.endTransaction();
			this.session.flush();
			//if the parser is running in multi-thread mode releases the lock on 
			//the book element
			if (this.monitor!=null)
				this.monitor.releaseLockOnProceedings(proceedingsId);
			//updates the current partFile state (offset)
			this.partFile.setLastOffset(this.currOffset);
			this.currDblpElement = "";
		} 
		//Handles each exception and Errors in the same way
		catch (Exception e){
			try{
				//Rollbacks the current transaction
				this.session.rollbackTransaction();
				//if an exception occurred, before exiting the parser, updates the 
				//xml parts descriptor in order to persist 
				//the current state. If the parser is running in multi-thread mode
				//gets and releases the lock on the partsInfo file.
				if (monitor!=null)
					monitor.getLockOnDescriptor();
				this.partFile.updateDescriptor();
				if (monitor!=null)
					monitor.releaseLockOnDescriptor();
				//when a thread is closed after an error forces the termination of 
				//each other thread
				if (monitor!=null)
					this.monitor.setCloseThreads(true);
				this.session.clear();
				this.session.close();
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while saving a Proceedings object:\n" + e.getMessage());
			}
			catch(Exception ne){
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while handling exception. Error message\n"+ne.getMessage());
				//if the parser is running in multi-thread mode releases the lock on 
				//the proceedings element
				if (this.monitor!=null)
					this.monitor.releaseLockOnProceedings(proceedingsId);
				//forwards the exception to terminate the thread
				throw new SAXException(ne);
			}
			//forwards the exception to terminate the thread
			throw new SAXException(e);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private void writeBook() throws SAXException{
		String bookId="";
		try {
			bookId = ((Book)this.currEntity).getId();			
			//if the parser is running in multi-thread mode first of all 
			//gets the lock on the book element
			if (this.monitor!=null)
				this.monitor.getLockOnBook(bookId);
			//checks if the current book element already exists in the db
			List existingBooks = 
				session.getNamedQuery("findBookById")
				.setString("bookId", bookId)
				.list();
			this.session.startTransaction();
			for(Person p : this.currAuthors)
				this.session.saveObject(p);
			for(Person p : this.currEditors)
				this.session.saveObject(p);
			if(existingBooks.size()==1){
				//if the book already exists updates the existing object
				Book tempBook = (Book)existingBooks.get(0);
				//refreshes the book element read from the db
				session.refresh(tempBook);
				//updates the book element
				tempBook.setDblpKey(((Book)this.currEntity).getDblpKey());
				tempBook.setIsbn(((Book)this.currEntity).getIsbn());
				tempBook.setPublisher(((Book)this.currEntity).getPublisher());
				tempBook.setTitle(((Book)this.currEntity).getTitle());
				tempBook.setYear(((Book)this.currEntity).getYear());
				tempBook.setBooktitle(((Book)this.currEntity).getBooktitle());
				tempBook.setEditors(((Book)this.currEntity).getEditors());
				tempBook.setAuthors(((Book)this.currEntity).getAuthors());
				//re-attach the book element to the session
				this.session.update(tempBook);
			}	
			else if(existingBooks.size()==0){
				//Need to check the id, as for "classic" books, the url doesn't exists
				//In this case the id will be the dblpkey
				if(((Book)this.currEntity).getId()==null||((Book)this.currEntity).getId().equalsIgnoreCase(""))
					((Book)this.currEntity).setId(((Book)this.currEntity).getDblpKey());
				this.session.saveObject(this.currEntity);
			}
			else if(existingBooks.size()>1)
				logger.error("Found more than one book with the same id " + bookId);
			//Clean up some temp info I stored
			this.session.endTransaction();
			this.session.flush();
			//if the parser is running in multi-thread mode releases the lock on 
			//the book element
			if (this.monitor!=null)
				this.monitor.releaseLockOnBook(bookId);
			//updates the current partFile state (offset)
			this.partFile.setLastOffset(this.currOffset);
			this.currDblpElement = "";
		} //Handles each exception in the same way
		catch (Exception e){
			try{
				//Rolls back the current transaction
				this.session.rollbackTransaction();
				//if an exception occurred, before exiting the parser, updates the 
				//xml parts descriptor in order to persist 
				//the current state. If the parser is running in multi-thread mode
				//gets and releases the lock on the partsInfo file.
				if (monitor!=null)
					monitor.getLockOnDescriptor();
				this.partFile.updateDescriptor();
				if (monitor!=null)
					monitor.releaseLockOnDescriptor();
				//when a thread is closed after an error forces the termination of 
				//each other thread
				if (monitor!=null)
					this.monitor.setCloseThreads(true);
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while saving a book object:\n" + e.getMessage());
				this.session.clear();
				this.session.close();
			}
			catch(Exception ne){
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while handling exception. Error message\n"+ne.getMessage());
				//if the parser is running in multi-thread mode releases the lock on 
				//the book element
				if (this.monitor!=null)
					this.monitor.releaseLockOnBook(bookId);
				//forwards the exception to terminate the thread
				throw new SAXException(ne);
			}
			//forwards the exception to terminate the thread
			throw new SAXException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeInCollection() throws SAXException{
		String bookId = "";
		try {						
			if(((InCollection)this.currEntity).getId().contains(".html#"))
				bookId = ((InCollection)this.currEntity).getId().substring(0,((InCollection)this.currEntity).getId().lastIndexOf("#"));
			else
				bookId = ((InCollection)this.currEntity).getId();
			//if the parser is running in multi-thread mode first of all 
			//gets the lock on the book element
			if (this.monitor!=null)
				this.monitor.getLockOnBook(bookId);
			//checks if the book to be updated already exists in the db
			List<Book> existingBooks = 
				session.getNamedQuery("findBookById")
				.setString("bookId", bookId)
				.list();
			this.session.startTransaction();
			for(Person p : this.currAuthors)
				this.session.saveObject(p);
			if(existingBooks.size()==0){
				//if the book does not exists creates a new book element
				Book book = new Book();
				book.setId(bookId);
				book.setBooktitle(((InCollection)this.currEntity).getBooktitle());
				book.setYear(((InCollection)this.currEntity).getYear());
				book.setPublisher(((InCollection)this.currEntity).getPublisher());
				book.addChapter((InCollection)this.currEntity);
				book.setIsbn(((InCollection)this.currEntity).getIsbn());
				this.session.saveObject(book);
			}	
			else if(existingBooks.size()==1){
				//if the book already exists updates the existing object
				Book book = existingBooks.get(0);
				//refreshes the book element read from the db
				session.refresh(book);
				//WORKAROUND caused by books/mit/PF91/Piatetsky91 entry
				if(((InCollection)this.currEntity).getId().compareTo(book.getId())==0)
					((InCollection)this.currEntity).setId(((InCollection)this.currEntity).getId() + "#" + ((InCollection)this.currEntity).getDblpKey().substring(((InCollection)this.currEntity).getDblpKey().lastIndexOf("/") + 1));
				if(!((InCollection)this.currEntity).getBooktitle().equalsIgnoreCase("") && book.getBooktitle().equalsIgnoreCase(""))
					book.setBooktitle(((InCollection)this.currEntity).getBooktitle());
				if(!((InCollection)this.currEntity).getYear().equalsIgnoreCase("") && book.getYear().equalsIgnoreCase(""))
					book.setYear(((InCollection)this.currEntity).getYear());
				if(!((InCollection)this.currEntity).getPublisher().equalsIgnoreCase("") && book.getPublisher().equalsIgnoreCase(""))
					book.setPublisher(((InCollection)this.currEntity).getPublisher());
				if(!((InCollection)this.currEntity).getIsbn().equalsIgnoreCase("") && book.getIsbn().equalsIgnoreCase(""))
					book.setIsbn(((InCollection)this.currEntity).getIsbn());
				//updates the book element
				book.addChapter((InCollection)this.currEntity);
				logger.debug("Added chapter " + ((InCollection)this.currEntity).getId() + " to " + book.getId());
				this.session.update(book);
			}
			else
				logger.error("Found two books with the same id: " + bookId);
			this.session.endTransaction();
			this.session.flush();
			//if the parser is running in multi-thread mode releases the lock on 
			//the book element
			if (this.monitor!=null)
				this.monitor.releaseLockOnBook(bookId);
			//updates the partFile state (offset)
			this.partFile.setLastOffset(this.currOffset);
			this.currDblpElement = "";
			this.currAuthors = new ArrayList<Person>();
		}
		catch (Exception e){
			try{
				/*
				 * Here, in front of some error, we would rollback the current transaction
				 * but the rollback call on a Hibernate session is pretty useless 
				 * as it is only a SQL level rollback.
				 * Therefore the chosen solution is to check in the database whether the currEntity has
				 * been saved or not and, in the first case, delete it. 
				 * In fact even if the incollection 
				 * exists we have no guarantees that it was correctly saved
				 * in the database (relations with book, authors, ecc..). Plus if we
				 * do not delete the incollection the application will fail to restart.
				 * However a rollback is executed to free the transaction
				 */  
				this.session.rollbackTransaction();
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while saving an incollection object:\n"+e.getMessage());
				//Before exiting the parser updates the xml parts descriptor in order to persist 
				//the current state. If the parser is running in multi-thread mode
				//gets and releases the lock on the partsInfo file. 
				this.partFile.setLastCheckpoint(this.currCheckpoint);
				this.partFile.setLastOffset(this.currOffset-1);
				if (monitor!=null)
					monitor.getLockOnDescriptor();
				this.partFile.updateDescriptor();
				if (monitor!=null)
					monitor.releaseLockOnDescriptor();
				//when a thread is closed after an exception it forces the termination of each other thread
				if (monitor!=null)
					this.monitor.setCloseThreads(true);
				//checks if the current InCollection has been saved in the db
				List existingInCollection = session.getNamedQuery("findInCollectionById")
				.setString("inCollectionId", ((InCollection)this.currEntity).getId())
				.list();
				if (existingInCollection.size()==1){
					//if if the current InCollection has been saved in the db deletes it
					this.session.clear();
					this.session.flush();
					this.session.delete((InCollection)this.currEntity);
				}	
				this.session.flush();
				this.session.close();
				//if the parser is running in multi-thread mode releases the lock on 
				//the book element
				if (this.monitor!=null)
					this.monitor.releaseLockOnBook(bookId);
				
			}
			catch(Exception ne){
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while handling exception. Error message\n"+ne.getMessage());
				//if the parser is running in multi-thread mode releases the lock on 
				//the book element
				if (this.monitor!=null)
					this.monitor.releaseLockOnBook(bookId);
				//forwards the exception to terminate the thread
				throw new SAXException(ne);
			}
			//forwards the exception to terminate the thread
			throw new SAXException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeInProceedings() throws SAXException{
		String proceedingsId = "";
		
			
//			//WORKAROUND caused by some entries which share the same url
//			if( ((InProceedings)this.currEntity).getUrl().contains("http://theory.lcs.mit.edu/")){
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
//				String currentKey = ((InProceedings)this.currEntity).getDblpKey();
//				
//				String cName=currentKey.substring(currentKey.indexOf("/")+1);
//				cName = cName.substring(0,cName.indexOf("/"));
//				
//				String cYear = ((InProceedings)this.currEntity).getYear();
//				
//				String type = currentKey;
//				type = type.substring(0,type.indexOf("/"));
//				
//				String keyPart = currentKey.substring(currentKey.lastIndexOf("/")+1);
//				String newUrl="db/"+type+"/"+cName+"/"+cName+cYear.substring(2)
//				+".html#"+keyPart;
//				
//				((InProceedings)this.currEntity).setId(newUrl);
//				((InProceedings)this.currEntity).setUrl(newUrl);
//			}
				
			
		try {	
			if(((InProceedings)this.currEntity).getId().contains(".html#"))
				proceedingsId = ((InProceedings)this.currEntity).getId().substring(0, ((InProceedings)this.currEntity).getId().lastIndexOf("#"));
			else
				proceedingsId = ((InProceedings)this.currEntity).getId();
				
			
			
			//if the parser is running in multi-thread mode first of all 
			//gets the lock on the Proceedings element
			if (this.monitor!=null)
				this.monitor.getLockOnProceedings(proceedingsId);
			//checks if the proceedings to be updated already exists in the db
			List existingProceedings = 
				session.getNamedQuery("findProceedingsById")
				.setString("proceedingsId", proceedingsId)
				.list();
			this.session.startTransaction();
			for(Person p : this.currAuthors)
				this.session.saveObject(p);
			if(existingProceedings.size()==1){
				//if the proceedings element already exists updates the existing object
				Proceedings proceedings = (Proceedings)existingProceedings.get(0);
				//refreshes the proceedings element read from the db
				this.session.refresh(proceedings);
				//updates the proceedings element
				if(!((InProceedings)this.currEntity).getYear().equalsIgnoreCase("") && proceedings.getYear().equalsIgnoreCase(""))
					proceedings.setYear(((InProceedings)this.currEntity).getYear());
				if(!((InProceedings)this.currEntity).getBooktitle().equalsIgnoreCase("") && proceedings.getTitle().equalsIgnoreCase(""))
					proceedings.setTitle(((InProceedings)this.currEntity).getBooktitle());
				if(!((InProceedings)this.currEntity).getIsbn().equalsIgnoreCase("") && proceedings.getIsbn().equalsIgnoreCase(""))
					proceedings.setIsbn(((InProceedings)this.currEntity).getIsbn());
				if(!((InProceedings)this.currEntity).getPublisher().equalsIgnoreCase("") && proceedings.getPublisher().equalsIgnoreCase(""))
					proceedings.setPublisher(((InProceedings)this.currEntity).getPublisher());
				proceedings.addInProceedings((InProceedings)this.currEntity);
				this.session.update(proceedings);
			}
			else if (existingProceedings.size()==0){
				//checks if a journal with the given id exists
				List existingJournals = 
					session.getNamedQuery("findJournalById")
					.setString("journalId", proceedingsId)
					.list();
				if(existingJournals.size()==0){
					//if nor a journal nor a proceedings with the given id exists
					//creates a new proceedings
					Proceedings proceedings = new Proceedings();
					proceedings.setId(proceedingsId);
					proceedings.setYear(((InProceedings)this.currEntity).getYear());
					proceedings.setTitle(((InProceedings)this.currEntity).getBooktitle());
					proceedings.setIsbn(((InProceedings)this.currEntity).getIsbn());
					proceedings.setPublisher(((InProceedings)this.currEntity).getPublisher());
					proceedings.addInProceedings((InProceedings)this.currEntity);
					this.session.saveObject(proceedings);
				}
				else if (existingJournals.size()==1){
					//if a journal with the given id exists updates the existing object
					Journal journal = (Journal)existingJournals.get(0);
					//refreshes the journal element read from the db
					this.session.refresh(journal);
					//updates the journal element
					journal.addArticle((DblpPublication)this.currEntity);
					this.session.update(journal);
				}
				else
					logger.error("Two Journals with the same id: " + proceedingsId);
			}
			else
				logger.error("Two Proceedings with the same id: " + proceedingsId);	
			//saves the current inProceedings
			this.session.saveOrUpdate(this.currEntity);
			this.session.endTransaction();
			this.session.flush();
			//if the parser is running in multi-thread mode releases the lock on 
			//the proceedings element
			if (this.monitor!=null)
				this.monitor.releaseLockOnProceedings(proceedingsId);
			//updates the partFile state (offset)
			this.partFile.setLastOffset(this.currOffset);
			this.currDblpElement = "";
			this.currAuthors = new ArrayList<Person>();
		}  
		catch (Exception e){
			try{
				/*
				 * Here, in front of some error, we would rollback the current transaction
				 * but the rollback call on a Hibernate session is pretty useless 
				 * as it is only a SQL level rollback.
				 * Therefore the chosen solution is to check in the database whether the currEntity has
				 * been saved or not and, in the first case, delete it. 
				 * In fact even if the inproceedings 
				 * exists we have no guarantees that it was correctly saved
				 * in the database (relations with proceedings, authors, ecc..). Plus if we
				 * do not delete the inproceedings the application will fail to restart.
				 * However a rollback is executed to free the transaction
				 */  
				this.session.rollbackTransaction();
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while saving an inproceedings object:\n" + e.getMessage());
				
				//Before exiting the parser updates the xml parts descriptor in order to persist 
				//the current state. If the parser is running in multi-thread mode
				//gets and releases the lock on the partsInfo file. 
				this.partFile.setLastCheckpoint(this.currCheckpoint);
				this.partFile.setLastOffset(this.currOffset-1);
				if (monitor!=null)
					monitor.getLockOnDescriptor();
				this.partFile.updateDescriptor();
				if (monitor!=null)
					monitor.releaseLockOnDescriptor();
				//when a thread is closed after an exception it forces the termination of each other thread
				if (monitor!=null)
					this.monitor.setCloseThreads(true);
				//checks if the current inProceedings has been saved in the db
				List existingInProceedings = session.getNamedQuery("findInProceedingsById")
				.setString("inProceedingsId", ((InProceedings)this.currEntity).getId())
				.list();
				if (existingInProceedings.size()==1){
					//if if the current InCollection has been saved in the db deletes it
					this.session.clear();
					this.session.flush();
					this.session.delete((InProceedings)this.currEntity);
				}	
				this.session.flush();
				this.session.close();
				//if the parser is running in multi-thread mode releases the lock on 
				//the proceedings element
				if (this.monitor!=null)
					this.monitor.releaseLockOnProceedings(proceedingsId);
			}
			catch(Exception ne){
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while handling exception. Error message: "+ne.getMessage());
				//if the parser is running in multi-thread mode releases the lock on 
				//the book element
				if (this.monitor!=null)
					this.monitor.releaseLockOnProceedings(proceedingsId);
				//forwards the exception to terminate the thread
				throw new SAXException(ne);
			}
			//forwards the exception to terminate the thread
			throw new SAXException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeArticle() throws SAXException{
		String journalId = "";
		
		
//			//WORKAROUND caused by some entries which share the same url
//			if( ((Article)this.currEntity).getUrl().contains("http://theory.lcs.mit.edu/")){
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
//				String currentKey = ((Article)this.currEntity).getDblpKey();
//				
//				String jName=currentKey.substring(currentKey.indexOf("/")+1);
//				jName = jName.substring(0,jName.indexOf("/"));
//				
//				String jVolume = this.tempJournal.getVolume();
//				String type = currentKey;
//				type = type.substring(0,type.indexOf("/"));
//				
//				String keyPart = currentKey.substring(currentKey.lastIndexOf("/")+1);
//				String newUrl="db/"+type+"/"+jName+"/"+jName+
//				jVolume+".html#"+keyPart;
//				
//				((Article)this.currEntity).setId(newUrl);
//				((Article)this.currEntity).setUrl(newUrl);
//			}
		try {	
			if(((Article)this.currEntity).getId().contains(".html#"))
				journalId = ((Article)this.currEntity).getId().substring(0, ((Article)this.currEntity).getId().lastIndexOf("#"));
			else
				journalId = ((Article)this.currEntity).getId();
			
			
			//if the parser is running in multi-thread mode first of all 
			//gets the lock on the journal element (same lock for proceedings element)
			if (this.monitor!=null)
				this.monitor.getLockOnProceedings(journalId);
			//checks if the journal to be updated already exists in the db
			List existingJournals = 
				session.getNamedQuery("findJournalById")
				.setString("journalId", journalId)
				.list();
			this.session.startTransaction();
			for(Person p : this.currAuthors)
				this.session.saveObject(p);
			if(existingJournals.size()==0){
				//if the journal does not exists tries to create a new journal element
				this.tempJournal.setId(journalId);
				this.tempJournal.setYear(((Article)this.currEntity).getYear());
				this.session.saveObject(this.tempJournal);
				this.tempJournal.addArticle((Article)this.currEntity);
				logger.debug("A new journal " + this.tempJournal.getTitle() + " with id " + journalId + " has been found. Searching for a fake Proceedings...");
				if(this.fakeProceedings.contains(journalId)){
					logger.debug("No fake proceedings found");
				}
				else{
					//checks if the journal already exists in the db as a proceedings
					List existingFakeProceedings = 
						session.getNamedQuery("findProceedingsById")
						.setString("proceedingsId", journalId)
						.list();
					this.fakeProceedings.add(journalId);
					//if no fake proceedings are found, do nothing
					if(existingFakeProceedings.size()==0)
						logger.debug("No fake proceedings found");
					else if(existingFakeProceedings.size()==1){
						//if a fake proccedings is found
						logger.debug("Fake proceedings found");
						//updates the fake proceedings 
						this.session.refresh((Proceedings)existingFakeProceedings.get(0));
						List<InProceedings> toBeModified = ((Proceedings)existingFakeProceedings.get(0)).getInProceedings();
						//moves the inproceedings frome the fake inProceedings to the journal
						for(InProceedings i : toBeModified){
							this.tempJournal.addArticle(i);
							logger.debug("InProceedings " + i.getId() + " detached from its fake Proceedings and attached to journal " + journalId);
						}
						//saves the journal and deletes the fake proceedings
						this.session.saveOrUpdate(this.tempJournal);
						this.session.delete(existingFakeProceedings.get(0));
					}
				}
			}
			else if(existingJournals.size()==1){
				//if a journal with the given id exists updates the existing object
				Journal journal = (Journal)existingJournals.get(0);
				//refreshes the journal element read from the db
				this.session.refresh(journal);
				//updates the journal element
				journal.addArticle((Article)this.currEntity);
				this.session.update(journal);
			}
			else
				logger.error("Found two proceedings with the same id: " + journalId);
			//saves the current article
			this.session.saveOrUpdate(this.currEntity);
			this.session.endTransaction();
			this.session.flush();
			//if the parser is running in multi-thread mode releases the lock on 
			//the proceedings element
			if (this.monitor!=null)
				this.monitor.releaseLockOnProceedings(journalId);
			//updates the partFile state (offset)
			this.partFile.setLastOffset(this.currOffset);
			this.currDblpElement = "";
			this.tempJournal = new Journal();
			this.currAuthors = new ArrayList<Person>();
		}  
		catch (Exception e){
			try{
				/*
				 * Here, in front of some error, we would rollback the current transaction
				 * but the rollback call on a Hibernate session is pretty useless 
				 * as it is only a SQL level rollback.
				 * Therefore the chosen solution is to check in the database whether the currEntity has
				 * been saved or not and, in the first case, delete it. 
				 * In fact even if the inproceedings 
				 * exists we have no guarantees that it was correctly saved
				 * in the database (relations with proceedings, authors, ecc..). Plus if we
				 * do not delete the inproceedings the application will fail to restart.
				 * However a rollback is executed to free the transaction.
				 */  
				this.session.rollbackTransaction();
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while saving an article object:\n" + e.getMessage());
				//Before exiting the parser updates the xml parts descriptor in order to persist 
				//the current state. If the parser is running in multi-thread mode
				//gets and releases the lock on the partsInfo file. 
				this.partFile.setLastCheckpoint(this.currCheckpoint);
				this.partFile.setLastOffset(this.currOffset-1);
				if (monitor!=null)
					monitor.getLockOnDescriptor();
				this.partFile.updateDescriptor();
				if (monitor!=null)
					monitor.releaseLockOnDescriptor();
				//when a thread is closed after an exception it forces the termination of each other thread
				if (monitor!=null)
					this.monitor.setCloseThreads(true);
				//checks if the current article has been saved in the db
				List existingInArticle = session.getNamedQuery("findArticleById")
				.setString("articleId", ((Article)this.currEntity).getId())
				.list();
				if (existingInArticle.size()==1){
					//if if the current article has been saved in the db deletes it
					this.session.clear();
					this.session.flush();
					this.session.delete((Article)this.currEntity);
				}	
				this.session.flush();
				this.session.close();
				//if the parser is running in multi-thread mode releases the lock on 
				//the journal element
				if (this.monitor!=null)
					this.monitor.releaseLockOnProceedings(journalId);
			}
			catch(Exception ne){
				logger.error("Error in thread parsing "+this.partFile.getFile().getName()+
						" while handling exception. Error message: "+ne.getMessage());
				//if the parser is running in multi-thread mode releases the lock on 
				//the journal element
				if (this.monitor!=null)
					this.monitor.releaseLockOnProceedings(journalId);
				//forwards the exception to terminate the thread
				throw new SAXException(ne);
			}
			//forwards the exception to terminate the thread
			throw new SAXException(e);
		}
	}

}
