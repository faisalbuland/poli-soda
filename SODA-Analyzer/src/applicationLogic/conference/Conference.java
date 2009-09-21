package applicationLogic.conference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import applicationLogic.statistics.Author;
import applicationLogic.statistics.utils.PublicationComparator;


import dblp.social.exceptions.SodaHibernateException;
import dblp.social.hibernate.ISodaHibernateSession;
import dblp.social.hibernate.entities.InProceedings;
import dblp.social.hibernate.entities.Person;
import dblp.social.hibernate.entities.Proceedings;
/**
 * This class is a data type which holds informations on a given conference.
 * A conference is a set of proceedings. Each stored proceedings is ordered by its year.
 * This class also provides a set of methods to handle parts of the conference and to and to extract some properties, as Authors-Publications and Publication-Authors histograms.
 * 
 * @author Staffiero
 *
 */
public class Conference {
	private ISodaHibernateSession session;
	private HashMap<Integer, ArrayList<Proceedings>> years_procs = null;
	private ArrayList<Integer> storedYears = null;
	private ArrayList<Integer> initializedYears;
	private String conferenceName="";
	
	
	/**
	 * This constructor initializes the Conference object loading all the proceedings of the given conference.
	 * All the Proceedings are lazily fetched, i.e. relation fields will not be available without a further query to the database.
	 * 
	 * @param session a SodaHibernateSession used to query the database.
	 * @param conferenceName the conference short name. For example icse, aamas, sigmod, ecc...
	 * @throws SodaHibernateException 
	 */
	public Conference(ISodaHibernateSession session, String conferenceName) throws SodaHibernateException{
		this.session=session;
		List<Proceedings> procs = null;
		
		this.conferenceName = conferenceName;
		
		this.session.open();
		
		String hqlQuery = "FROM Proceedings p where p.id like '%/"+conferenceName+"/%' order by year desc";
		
//		System.out.println("Query: "+hqlQuery);
		procs = this.session.query(hqlQuery, Proceedings.class);
		
		this.session.close();
		
		this.groupByYear(procs);
		 
	}
	/**
	 * This constructor initializes the Conference object loading all the proceedings of the given conference.
	 * In this case the conference is represented by a set of strings, the first string of the list becomes the conference name.
	 * All the Proceedings are lazily fetched, i.e. relation fields will not be available without a further query to the database.
	 * 
	 * @param session a SodaHibernateSession used to query the database.
	 * @param conferenceNames a set of short names representing the whole conference. For example, for aamas the set should be {"aamas", "atal","ecaiw"}
	 * @throws SodaHibernateException
	 */
	public Conference(ISodaHibernateSession session, List<String> conferenceNames) throws SodaHibernateException{
		this.session=session;
		List<Proceedings> procs = null;
		
		this.conferenceName = conferenceNames.get(0);
		
		this.session.open();
		
		String hqlQuery = "FROM Proceedings p where ";
		
		for (int i=0; i<conferenceNames.size(); i++ ){
			String title = conferenceNames.get(i);
			hqlQuery = hqlQuery + "p.id like '%/"+title+"/%' "; 
			if (i<conferenceNames.size()-1)
				hqlQuery = hqlQuery +"or ";
		}		
		hqlQuery = hqlQuery + "order by year desc";
		
		System.out.println("Query: "+hqlQuery);
		procs = this.session.query(hqlQuery, Proceedings.class);
		
		this.session.close();
		
		this.groupByYear(procs);
		 
	}
	/**
	 * Returns the conference short name
	 * @return conference name
	 */
	public String getConferenceName() {
		return conferenceName;
	}

	/**
	 * Initializes a year for this conference, i.e. all the relations of all the proceedings and inProceedings of the given year are initialized.
	 * @param year the year to initialize
	 * @throws SodaHibernateException
	 */
	@SuppressWarnings("unchecked")
	public void initializeYear(int year) throws SodaHibernateException{
		if (!yearExists(year))
			throw new SodaHibernateException("Invalid year selection");
		
		if (this.initializedYears.contains(year))
			return;
		
		if (!this.session.isOpen())
			this.session.open();
		List<Proceedings> procs = this.years_procs.get(year);
		for (int i=0; i<procs.size(); i++){
			Proceedings p = procs.get(i);
			List<Proceedings> existingProceedings = 
				session.getNamedQuery("findProceedingsById")
				.setString("proceedingsId", p.getId())
				.list();
			if (existingProceedings.size()==1){
				procs.remove(i);
				procs.add(i,existingProceedings.get(0));
				p=existingProceedings.get(0);
//				p.getInProceedings().size();
				for (InProceedings inp : p.getInProceedings()){
					inp.getAuthors().size();
				}
			}
			
		}
		this.initializedYears.add(year);
		this.session.close();
	}

	/**
	 * Returns the list of years in which the current conference has been held
	 * @return a list of years
	 */
	public List<Integer> getYears(){
		return this.storedYears;
	}
	
	/**
	 * Returns the list of all the proceedings of the current conference
	 * @return
	 */
	public List<Proceedings> getAllProceedings() {
		List<Proceedings> procs = new ArrayList<Proceedings>();
		if (this.storedYears==null || this.years_procs==null)
			return null;
		for (Integer y : this.storedYears){
			procs.addAll(this.years_procs.get(y));
		}
		
		return procs;
	}
	
	/**
	 * Returns the total number of proceedings of the current conference.
	 * @return the total number of proceedings
	 */
	public int getProceedingsNumber() {
		int number = 0;
		for (Integer y : this.storedYears){
			number += this.years_procs.get(y).size();
		}
		return number;
	}
	
	/**
	 * Returns the proceedings of the conference for the given year
	 * @param year the year of interest
	 * @return the list of proceedings of the given year
	 */
	public List<Proceedings> getProceedingsByYear(int year) {
		if (yearExists(year))
			return this.years_procs.get(year);
		else 
			return null;
	}
	
	/**
	 * Returns the proceedings of the conference from the beginning until the given year
	 * @param year year the year of interest
	 * @return the list of proceedings until the given year
	 */
	public List<Proceedings> getProceedingsUntilYear(int year){
		List<Proceedings> procs = new ArrayList<Proceedings>();
		if (yearExists(year)){
			for (Integer y : this.storedYears){
				if (y<=year)
					procs.addAll(this.years_procs.get(y));
			}
			return procs;
		}
		else 
			return null;
	}
	
	/** 
	 * Tests if the current conference is empty
	 * @return true if the conference is empty, false otherwise
	 */
	public boolean isEmpty(){
		if (this.storedYears.size()==0)
			return true;
		else
			return false;
	}
	
	/**
	 * Initializes all the years of the current conference.
	 * 
	 * @see Conference.initializeYear(int year)
	 * @throws SodaHibernateException
	 */
	public void initializeAll() throws SodaHibernateException{
		
		for (Integer y: this.storedYears){
			initializeYear(y);
		}
	}
	
	/**
	 * Returns a List of integer representing a histogram in which for a given number of co-authors we can find the respective number of publications.
	 * I.e. we can know how many publications are authored by a certain number of co-authors.
	 * The whole set of publications and authors of the current conference is considered.
	 * 
	 * @return a List of integer representing the Authors-Publications histogram.
	 * @throws SodaHibernateException
	 */
	public List<Integer> getAuthorsPublications() throws SodaHibernateException{
		List<Integer> authorsPublications = new ArrayList<Integer>();
		int maxCoAuthors = 0;
		this.initializeAll();
		List<Proceedings> procs = this.getAllProceedings();
		
		for (Proceedings proc : procs){
			for (InProceedings ip : proc.getInProceedings()){
				if (ip.getAuthors().size()>maxCoAuthors)
					maxCoAuthors = ip.getAuthors().size();
			}		
		}
		
		
		for (int i=0; i<=maxCoAuthors; i++){
			authorsPublications.add(new Integer(0));
		}
		
		for (Proceedings proc : procs){
			for (InProceedings ip : proc.getInProceedings()){
				int index = ip.getAuthors().size();
				int stats = authorsPublications.get(index);
				authorsPublications.set(index, new Integer(stats+1));
			}		
		}
		
		
		return authorsPublications;
	}
	
	/**
	 * Returns a List of integer representing a histogram in which for a given number of publications we can find the respective number of authors.
	 * I.e. we can know how many authors authored a certain number of publications.
	 * The whole set of publications and authors of the current conference is considered.
	 * 
	 * @return a List of integer representing the Publications-Authors histogram.
	 * @throws SodaHibernateException
	 */
	public List<Integer> getPublicationsAuthors() throws SodaHibernateException{
		int maxPublications = 1;
		List<Integer> publicationsAuthors = new ArrayList<Integer>();
		this.initializeAll();
		List<Proceedings> procs = this.getAllProceedings();
		
		HashMap<String, Integer> authorsPubs = new HashMap<String, Integer>();
		for (Proceedings proc: procs){
			for (InProceedings ip : proc.getInProceedings()){
				for (Person p : ip.getAuthors()){
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
	 * Returns a List of integer representing the Authors-Publications histogram considering only the given year.
	 * @param year the year of interest
	 * @return a List of integer representing the Authors-Publications histogram.
	 * @throws SodaHibernateException
	 */
	public List<Integer> getAuthorsPublicationsPerYear(int year) throws SodaHibernateException{
		List<Integer> authorsPublications = new ArrayList<Integer>();
		int maxCoAuthors = 0;
		this.initializeYear(year);
		List<Proceedings> procs = this.getProceedingsByYear(year);
		
		for (Proceedings proc : procs){
			for (InProceedings ip : proc.getInProceedings()){
				if (ip.getAuthors().size()>maxCoAuthors)
					maxCoAuthors = ip.getAuthors().size();
			}		
		}
		
		
		for (int i=0; i<=maxCoAuthors; i++){
			authorsPublications.add(new Integer(0));
		}
		
		for (Proceedings proc : procs){
			for (InProceedings ip : proc.getInProceedings()){
				int index = ip.getAuthors().size();
				int stats = authorsPublications.get(index);
				authorsPublications.set(index, new Integer(stats+1));
			}		
		}
		
		
		return authorsPublications;
	}
	
	/**
	 * Returns a List of integer representing the Authors-Publications histogram considering all the proceedings until the given year.
	 * @param year the year of interest
	 * @return a List of integer representing the Authors-Publications histogram.
	 * @throws SodaHibernateException
	 */
	public List<Integer> getAuthorsPublicationsUntilYear(int year) throws SodaHibernateException{
		List<Integer> authorsPublications = new ArrayList<Integer>();
		int maxCoAuthors = 0;
		List<Proceedings> procs = this.getProceedingsUntilYear(year);
		
		for (Integer y : this.storedYears){
			if (y<=year)
				this.initializeYear(y);
		}
		
		for (Proceedings proc : procs){
			for (InProceedings ip : proc.getInProceedings()){
				if (ip.getAuthors().size()>maxCoAuthors)
					maxCoAuthors = ip.getAuthors().size();
			}		
		}
		
		
		for (int i=0; i<=maxCoAuthors; i++){
			authorsPublications.add(new Integer(0));
		}
		
		for (Proceedings proc : procs){
			for (InProceedings ip : proc.getInProceedings()){
				int index = ip.getAuthors().size();
				int stats = authorsPublications.get(index);
				authorsPublications.set(index, new Integer(stats+1));
			}		
		}
		
		
		return authorsPublications;
	}
	
	/**
	 * Returns sorted array of authors. For each author the number of publications is set.
	 * All the publications of the current conference are considered, from the first to the last year.
	 * @return a List of {@link Author} sorted by publications number.
	 * @throws SodaHibernateException
	 */
	public List<Author> getAuthorsPublicationsNumber() throws SodaHibernateException{
		HashMap<String,Integer> authorsScore = new HashMap<String, Integer>();
		this.initializeAll();
		List<Proceedings> procs = this.getAllProceedings();
		for(Proceedings pr : procs){
			List<InProceedings> ips = pr.getInProceedings();
			for(InProceedings i : ips){
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
	 * Groups all the given proceedings by their year. 
	 * The result is stored in an HashMap whose key is the year and whose value is an ArrayList of Proceedings.
	 * 
	 * 
	 * @param procs the list of proceedings to be grouped
	 */
	private void groupByYear(List<Proceedings> procs){
		this.years_procs = new HashMap<Integer, ArrayList<Proceedings>>(); 
		this.storedYears = new ArrayList<Integer>();
		this.initializedYears = new ArrayList<Integer>();
		for (Proceedings p : procs){
			String year =  parseYear(p.getId());
			int procYear = Integer.parseInt(year);
			if (!this.storedYears.contains(procYear))
				this.storedYears.add(procYear);
		}
		
		Collections.sort(this.storedYears);
		
		for (Integer y : this.storedYears){
			ArrayList<Proceedings> procsOfYear = new ArrayList<Proceedings>();
			
			for (Proceedings p : procs){
				String year =  parseYear(p.getId());
				int procYear = Integer.parseInt(year);
				if (procYear == y){
					procsOfYear.add(p);
				}
			}
			
			this.years_procs.put(y,procsOfYear);
		}
		
		
//		//TEST CODE
//		System.out.println("\nYears List:\n ");
//		for (Integer i : this.storedYears){
//			System.out.println(i);
//		}
//		
//		
//		System.out.println("\nHash map:\n");
//		for (Integer i : this.storedYears){
//			System.out.println("\nYear: "+i+" Proceedings for this year:");
//			ArrayList<Proceedings> poy = this.years_procs.get(i);
//			for (Proceedings p: poy)
//				System.out.println(p.getId());
//		}
		
	}

	
	/**
	 * Extracts the year from the proceedings id. This is necessary because the field year may be different from the actual proceedings year.
	 * 
	 * @param s the proceedings id
	 * @return a string containing the year in (four digits)
	 */
	private String parseYear(String s){
		String res ="";
		boolean sequenceStarted = false;
		boolean stopSequence = false;
		
		char[] charArray = s.toCharArray();
		for(int i=0; i< charArray.length; i++){
			if (Character.isDigit(charArray[i])){
				//if it is the first digit we find
				if (!sequenceStarted){
					sequenceStarted=true;
					res=res+charArray[i];
				}
				else if(!stopSequence){
					res=res+charArray[i];
				}
			}
			else{
				if (sequenceStarted)
					stopSequence = true;
			}
		}
		Calendar now = Calendar.getInstance();
		String thisYear = ""+now.get(Calendar.YEAR);
		String twoDigitsYear = thisYear.substring(2);
	
		int cy = Integer.parseInt(twoDigitsYear);
		int ytc = Integer.parseInt(res);
		
		if (res.length()==2){
			if (ytc<=cy)
				res = "20"+res;
			else
				res = "19"+res;
		}
		
		return res;
	}
	
	/**
	 * Checks if a year exists in which this conference had held.
	 * @param year the year to check
	 * @return true if the conference had held in the given year, false otherwise.
	 */
	private boolean yearExists(int year){
		boolean yearExists=false;
		
		if (this.storedYears.contains((Integer)year))
				return true;
		
		return yearExists;
	}
}
