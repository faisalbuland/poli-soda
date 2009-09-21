package applicationLogic.journal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import applicationLogic.statistics.Author;
import applicationLogic.statistics.utils.PublicationComparator;

import dblp.social.exceptions.SodaHibernateException;
import dblp.social.hibernate.ISodaHibernateSession;
import dblp.social.hibernate.WorkArounds;
import dblp.social.hibernate.entities.DblpPublication;
import dblp.social.hibernate.entities.Journal;
import dblp.social.hibernate.entities.Person;
/**
 * This class is a data type which holds informations on a given transaction.
 * A transaction is a set of journals. Each stored journal is ordered by its volume number.
 * This class also provides a set of methods to handle parts of the transaction and to and to extract some properties, as Authors-Publications and Publication-Authors bar charts.
 * 
 * @author Staffiero
 *
 */

public class Transaction {
	private ISodaHibernateSession session;
	private String transactionName;
	private List<Journal> journals;
	private List<Integer> storedVolumes;
//	private ArrayList<Integer> initializedVolumes;
	
	/**
	 * This constructor initializes the Transaction object loading all the journals of the given transaction.
	 * All the Journals are lazily fetched, i.e. relation fields will not be available without a further query to the database.
	 *
	 * @param session a SodaHibernateSession used to query the database.
	 * @param transactionName the transaction short name. For example tse, tosem, ecc...
	 * @throws SodaHibernateException 
	 * 
	 */
	public Transaction(ISodaHibernateSession session, String transactionName) throws SodaHibernateException{
		this.session=session;
		this.transactionName = transactionName;
		
		this.session.open();
		String hqlQuery = "FROM Journal j where j.id like '%/"+transactionName+"/%' order by year desc";
		List<Journal> jlist = this.session.query(hqlQuery, Journal.class);
		this.session.close();
		
		this.journals = new ArrayList<Journal>();
		
		for(Journal jrnl : jlist){
			Journal fj = new Journal();
			fj.setId(jrnl.getId());
			fj.setDblpKey(jrnl.getDblpKey());
			fj.setIsbn(jrnl.getIsbn());
			fj.setNumber(jrnl.getNumber());
			fj.setPublisher(jrnl.getPublisher());
			fj.setTitle(jrnl.getTitle());
			fj.setVolume(jrnl.getVolume());
			fj.setYear(jrnl.getYear());
			this.journals.add(fj);
		}
		
		this.storedVolumes = new ArrayList<Integer>();
		
		
		
		for (Journal j : this.journals)
			this.storedVolumes.add(Integer.parseInt(j.getVolume()));
		
		Collections.sort(this.storedVolumes);
		
		for (Journal j : this.journals){
			this.session.open();
			List<DblpPublication> pubs = WorkArounds.getJournalArticles(j.getId(), session);
			for (DblpPublication p : pubs)
				p.getAuthors().size();
			this.session.close();
			
			j.setArticles(pubs);
		}
		
		//To use the workaround WorkArounds.getJournalArticles we need detached objects
		
//		this.initializedVolumes = new ArrayList<Integer>();
	}
	
	/**
	 * 
	 * @return the transaction short name
	 */
	public String getTransactionName() {
		return transactionName;
	}
	
//	@SuppressWarnings("unchecked")
//	public void initializeVolume(int volume) throws SodaHibernateException{
//		if (!volumeExists(volume))
//			throw new SodaHibernateException("Invalid volume selection");
//		
//		if (this.initializedVolumes.contains(volume))
//			return;
//		if (!this.session.isOpen())
//			this.session.open();
//		for (int index = 0; index<this.journals.size();index++){
//			Journal j = this.journals.get(index);
//			if (j.getVolume()!=""){
//				if (Integer.parseInt(j.getVolume())==volume){
//					
//					List<Journal> existingJournals= 
//						session.getNamedQuery("findJournalById")
//						.setString("journalId", j.getId())
//						.list();
//					if (existingJournals.size()==1){
//						this.journals.remove(index);
//						this.journals.add(index,existingJournals.get(0));
//						j=existingJournals.get(0);
//						
//						for (DblpPublication pub : j.getArticles()){
//							pub.getAuthors().size();
//						}
//					}
//					
//				}
//			}
//		}
//		this.initializedVolumes.add(volume);
//		this.session.close();
//			
//	}
	/**
	 * @return the list of volumes for the current transaction.
	 */
	public List<Integer> getVolumes(){
		return this.storedVolumes;
	}
	
	/**
	 * 
	 * @return the whole list of Journals of the current transaction.
	 */
	public List<Journal> getAllJournals() {
		return this.journals;
	}
	
	/**
	 * 
	 * @return the number of Journals of the current transaction.
	 */
	public int getJournalsNumber() {
		return this.journals.size();
	}
	/**
	 * 
	 * @param volume a volume number.
	 * @return the Journal of this transaction matching the given volume number, or null if such a Journal does not exist.
	 */
	public Journal getJournalByVolume(int volume) {
		if (volumeExists(volume)) {
			for (Journal j : this.journals){
				if (j.getVolume()!=""){
					if (Integer.parseInt(j.getVolume())==volume){
						return j;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param volume a volume number
	 * @return a list of Journals whose number is lesser or equal than the given volume number or null if there is no Journal matching the given volume number 
	 */
	public List<Journal> getJournalsUntilVolume(int volume){
		List<Journal> journs = new ArrayList<Journal>();
		if (volumeExists(volume)){
			for (Integer v : this.storedVolumes){
				if (v<=volume)
					journs.add(this.getJournalByVolume(v));
			}
			return journs;
		}
		else 
			return null;
	}
	
	
	/**
	 * Test if the current transaction contains at least one Journal
	 * @return true if at least one Journal exists in the current transaction, false otherwise.
	 */
	public boolean isEmpty(){
		if (this.storedVolumes.size()==0)
			return true;
		else
			return false;
	}
	
//	public void initializeAll() throws SodaHibernateException{
//		
//		for (Integer v: this.storedVolumes){
//			initializeVolume(v);
//		}
//	}
	
	/**
	 * Returns a List of integer representing a bar chart in which for a given number of co-authors we can find the respective number of publications.
	 * I.e. we can know how many publications are authored by a certain number of co-authors.
	 * The whole set of publications and authors of the current transaction is considered.
	 * 
	 * @return a List of integer representing the Authors-Publications bar chart.
	 * @throws SodaHibernateException
	 */
	public List<Integer> getAuthorsPublications() throws SodaHibernateException{
		List<Integer> authorsPublications = new ArrayList<Integer>();
		int maxCoAuthors = 0;
	
		for (Journal journ : this.journals){
			for (DblpPublication pub : journ.getArticles()){
				if (pub.getAuthors().size()>maxCoAuthors)
					maxCoAuthors = pub.getAuthors().size();
			}		
		}
		
		
		for (int i=0; i<=maxCoAuthors; i++){
			authorsPublications.add(new Integer(0));
		}
		
		for (Journal journ : this.journals){
			for (DblpPublication pub : journ.getArticles()){
				int index = pub.getAuthors().size();
				if (index!=0){
					int value = authorsPublications.get(index);
					value = value+1;
					authorsPublications.set(index, new Integer(value));
				}
			}		
		}
		
		
		return authorsPublications;
	}
	
	/**
	 * Returns a List of integer representing a bar chart in which for a given number of publications we can find the respective number of authors.
	 * I.e. we can know how many authors authored a certain number of publications.
	 * The whole set of publications and authors of the current transaction is considered.
	 * 
	 * @return a List of integer representing the Publications-Authors bar chart.
	 * @throws SodaHibernateException
	 */
	public List<Integer> getPublicationsAuthors() throws SodaHibernateException{

		int maxPublications = 1;
		List<Integer> publicationsAuthors = new ArrayList<Integer>();
		
		HashMap<String, Integer> authorsPubs = new HashMap<String, Integer>();
		for (Journal journ : this.journals){
			for (DblpPublication pub : journ.getArticles()){
				for (Person p : pub.getAuthors()){
					String name = p.getName();
					if (!authorsPubs.containsKey(name)){
						authorsPubs.put(name, new Integer(1));
					}
					else{
						int pn = authorsPubs.get(name);
						authorsPubs.remove(name);
						pn = pn+1;
						if (pn>maxPublications)
							maxPublications=pn;
						authorsPubs.put(name, new Integer(pn));
					}
				}
			}		
		}
		
		for (int i = 0; i<=maxPublications; i++){
			publicationsAuthors.add(new Integer(0));
		}
		
		
		for (Integer i :authorsPubs.values()){
			int value = publicationsAuthors.get(i);
			value= value+1;
			publicationsAuthors.set(i, new Integer(value));
		}
//		for (String name : authorsPubs.keySet()){
//			int index = authorsPubs.get(name);
//			int value = publicationsAuthors.get(index);
//			totPubs2 = totPubs2 +index;
//			value= value+1;
//			publicationsAuthors.set(index, new Integer(value));
//		}
		return publicationsAuthors;
	}
	
	/**
	 * Returns a List of integer representing the Authors-Publications bar chart considering only the given volume number.
	 * 
	 * @param volume the volume number of interest
	 * @return a List of integer representing the Authors-Publications histogram.
	 * @throws SodaHibernateException
	 */
	public List<Integer> getAuthorsPublicationsPerVolume(int volume) throws SodaHibernateException{
		List<Integer> authorsPublications = new ArrayList<Integer>();
		int maxCoAuthors = 0;
		Journal journ = this.getJournalByVolume(volume);
		
		for (DblpPublication pub : journ.getArticles()){
			if (pub.getAuthors().size()>maxCoAuthors)
				maxCoAuthors = pub.getAuthors().size();
		}		
		
		
		
		for (int i=0; i<=maxCoAuthors; i++){
			authorsPublications.add(new Integer(0));
		}
		
		for (DblpPublication pub : journ.getArticles()){
			int index = pub.getAuthors().size();
			int stats = authorsPublications.get(index);
			authorsPublications.set(index, new Integer(stats+1));
		}		
		
		
		
		return authorsPublications;
	}
	
	/**
	 * Returns a List of integer representing the Authors-Publications histogram considering all the proceedings until the given volume number.
	 * @param volume the volume number of interest
	 * @return a List of integer representing the Authors-Publications histogram.
	 * @throws SodaHibernateException
	 */
	public List<Integer> getAuthorsPublicationsUntilVolume(int volume) throws SodaHibernateException{
		List<Integer> authorsPublications = new ArrayList<Integer>();
		int maxCoAuthors = 0;
		List<Journal> journs = this.getJournalsUntilVolume(volume);
		
		
		for (Journal j : journs){
			for (DblpPublication pub : j.getArticles()){
				if (pub.getAuthors().size()>maxCoAuthors)
					maxCoAuthors = pub.getAuthors().size();
			}		
		}
		
		
		for (int i=0; i<=maxCoAuthors; i++){
			authorsPublications.add(new Integer(0));
		}
		
		for (Journal j: journs){
			for (DblpPublication pub : j.getArticles()){
				int index = pub.getAuthors().size();
				int stats = authorsPublications.get(index);
				authorsPublications.set(index, new Integer(stats+1));
			}		
		}
		
		
		return authorsPublications;
	}
	
	/**
	 * Returns sorted array of authors. For each author the number of publications is set.
	 * All the publications of the current transaction are considered, from the first to the last volume.
	 * @return a List of {@link Author} sorted by publications number.
	 * @throws SodaHibernateException
	 */
	public List<Author> getAuthorsPublicationsNumber() throws SodaHibernateException{
		HashMap<String,Integer> authorsScore = new HashMap<String, Integer>();
		List<Journal> jrs = this.getAllJournals();
		for(Journal jr : jrs){
			List<DblpPublication> pubs = jr.getArticles();
			for(DblpPublication i : pubs){
				//for each author ++ in pubs
				for (Person p: i.getAuthors()){
					//if the author name is not in the map it is added with score = 1
					//else score ++
					String name = p.getName();
					if(authorsScore.containsKey(name)){
						int oldVal = authorsScore.get(name);
						int val = oldVal+1;
						authorsScore.put(name, val);
					}
					else{
						authorsScore.put(name, 1);
					}
				}
			}
		}
		
		//create and populate a list of Author with the data stored in the map
		List<Author> aa = new ArrayList<Author>();
		
		for(String n : authorsScore.keySet()){
			Author auth = new Author(n);
			auth.setPublicationsNumber(authorsScore.get(n));
			aa.add(auth);
		}
		
		Collections.sort(aa, new PublicationComparator());
		
		return aa;
	}
	
	
	/**
	 * Test if a Journal with the given volume exists in the current transaction.
	 * @param volume the volume number to check.
	 * @return true if a Journal with the given volume exists, false otherwise.
	 */
	private boolean volumeExists(int volume){
		boolean volumeExists=false;
		
		if (this.storedVolumes.contains((Integer)volume))
				return true;
		
		return volumeExists;
	}
	
}