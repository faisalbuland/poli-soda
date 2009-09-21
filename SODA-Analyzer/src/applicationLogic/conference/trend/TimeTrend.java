package applicationLogic.conference.trend;



import java.util.ArrayList;
import java.util.List;

import applicationLogic.conference.Conference;
import applicationLogic.graph.GraphManager;
import applicationLogic.statistics.AuthorsStatistics;
import applicationLogic.statistics.GraphStatistics;
import applicationLogic.statistics.PublicationsStatistics;


import dblp.social.exceptions.SodaHibernateException;
import dblp.social.hibernate.entities.InProceedings;
import dblp.social.hibernate.entities.Person;
import dblp.social.hibernate.entities.Proceedings;
/**
 * This class provides a set of methods to analyze the temporal trend of a given Conference object.
 * 
 * @author Staffiero
 *
 */
public class TimeTrend {
	/**
	 * A Conference object representing the current conference.
	 */
	private Conference conf;
	
	/**
	 * The constructor to be used
	 * 
	 * @param conf a Conference object representing the conference to be analyzed.
	 * @throws NullPointerException if conf==null
	 */
	public TimeTrend(Conference conf){
		if (conf == null)
			throw new NullPointerException("Cannot initialize the TimeTrend object with a null reference");
		this.conf = conf;
	}
	
	
	/**
	 * Returns a set of Person. 
	 * This set contains all the authors who authored a publication in the given year, considering the given conference.
	 * 
	 * @param year the year of interest.
	 * @return the set of authors for the given year
	 * @throws SodaHibernateException
	 */
	public List<Person> getAuthorsPerYear(int year) throws SodaHibernateException{
		this.conf.initializeYear(year);
		List<Proceedings> procs = this.conf.getProceedingsByYear(year);
		
//		//TEST CODE
//		for (Proceedings p: procs){
//			System.out.println("found "+p.getInProceedings().size()+" InProceedings for Proceedings: "+p.getId());
//			int auth =0;
//			for (InProceedings i: p.getInProceedings()){
//				auth += i.getAuthors().size();
//			}
//			System.out.println("found "+auth+" authors for Proceedings: "+p.getId());
//		}
//		//END OF TEST CODE
		
		List<Person> authors = new ArrayList<Person>();
		for (Proceedings p : procs){
			List<InProceedings> inprocs = p.getInProceedings();
			for (InProceedings i : inprocs){
				List<Person> ipAuthors = i.getAuthors();
				for(Person a : ipAuthors){
					if (!authors.contains(a)){
						authors.add(a);
					}
				}
			}
		}
//		System.out.println("Found "+authors.size()+ " authors");
		return authors;
	}
	
	/**
	 * Returns a list of Person containing all the new authors for the given year.
	 * An author is a new author if he does not appear in the list previousAuthors.
	 * 
	 * @param year the year of interest.
	 * @param previousAuthors a list of "old" authors. Each author in this list will not be included in the returned list.
	 * @return a list of new authors.
	 * @throws SodaHibernateException
	 */
	public List<Person> getNewAuthorsPerYear(int year, List<Person> previousAuthors) throws SodaHibernateException{
		this.conf.initializeYear(year);
		List<Proceedings> procs = this.conf.getProceedingsByYear(year);
		
		List<Person> authors = new ArrayList<Person>();
		for (Proceedings p : procs){
			List<InProceedings> inprocs = p.getInProceedings();
			for (InProceedings i : inprocs){
				List<Person> ipAuthors = i.getAuthors();
				for(Person a : ipAuthors){
					//if author does not contain a we need to check if he is a new author
					if (!authors.contains(a)){
						boolean previous=false;
						for (Person pa : previousAuthors){
							if (pa.getName().compareTo(a.getName())==0)
								previous=true;
						}
						if(!previous){
							authors.add(a);
						}
					}
				}
			}
		}
//		System.out.println("Found "+authors.size()+ " new authors");
		return authors;
	}
	
	
	/**
	 * Returns a list of AuthorsStatistics, which contains, for each year: 
	 * the number of total authors, the number of new authors and the number of total authors until the current year 
	 *
	 * @return a list of {@link AuthorsStatistics} .
	 * @throws SodaHibernateException
	 */
	public List<AuthorsStatistics> getAuthorsTimeTrend() throws SodaHibernateException{
		List<AuthorsStatistics> authorsTrend = new ArrayList<AuthorsStatistics>();
		List<Person> authors = new ArrayList<Person>();
		
		for (Integer y : this.conf.getYears()){
			List<Person> aoty = this.getAuthorsPerYear(y);
			List<Person> naoty = this.getNewAuthorsPerYear(y, authors);
			authors.addAll(naoty);
			authorsTrend.add(new AuthorsStatistics(y,aoty.size(),naoty.size(), authors.size()));
			
		}		
		
		return authorsTrend;
	}
	
	/**
	 * Returns a list of AuthorsStatistics, within a given years range.
	 * for each year we have: the number of total authors, the number of new authors and the number of total authors  (until the current year).
	 * 
	 * 
	 * @param firstYear the first year of the interval of interest.
	 * @param lastYear the last year of the interval of interest.
	 * @return null if firstYear>=LastYear, a list of {@link AuthorsStatistics} otherwise
	 * @throws SodaHibernateException
	 */
	public List<AuthorsStatistics> getAuthorsTimeWindowTrend(int firstYear, int lastYear) throws SodaHibernateException{
		if (firstYear>=lastYear)
			return null;
		
		List<Integer> yearsWindow = new ArrayList<Integer>();
		
		for (Integer year : this.conf.getYears()){
			if (year>=firstYear && year<=lastYear)
				yearsWindow.add(year);
		}
		
		List<AuthorsStatistics> authorsTrend = new ArrayList<AuthorsStatistics>();
		List<Person> authors = new ArrayList<Person>();
		
		
		for (Integer y : yearsWindow){
			List<Person> aoty = this.getAuthorsPerYear(y);
			List<Person> naoty = this.getNewAuthorsPerYear(y, authors);
			authors.addAll(naoty);
			authorsTrend.add(new AuthorsStatistics(y,aoty.size(),naoty.size(), authors.size()));
			
		}		
		
		return authorsTrend;
	}
	
	/**
	 * Returns the total number of publications for a certain year, considering the given conference.
	 * 
	 * @param year the year of interest.
	 * @return the total number of publications for the given year.
	 * @throws SodaHibernateException
	 */
	public int getPublicationsPerYear(int year) throws SodaHibernateException{
		int publicationsNumber = 0;
		this.conf.initializeYear(year);
		List<Proceedings> procs = this.conf.getProceedingsByYear(year);
		
		for (Proceedings p: procs){
			publicationsNumber += p.getInProceedings().size();  	
		}
		
		return publicationsNumber;
	}

	/**
	 * Returns a list of PublicationStatistics, containing the number of publications for each year. 
	 * 
	 * @return a list of {@link PublicationsStatistics}.
	 * @throws SodaHibernateException
	 */
	public List<PublicationsStatistics> getPublicationsTimeTrend() throws SodaHibernateException{
		List<PublicationsStatistics> publicationsTrend = 
			new ArrayList<PublicationsStatistics>();
		
		for (Integer y : this.conf.getYears()){
			publicationsTrend.add(new PublicationsStatistics(y, this.getPublicationsPerYear(y) ));
			
		}		
		
		return publicationsTrend;
	}
	
	/**
	 * Returns a list of PublicationStatistics.
	 * The list contains the number of publications for each year, for a given years range. 
	 * 
	 * @param firstYear the first year of the interval of interest.
	 * @param lastYear the last year of the interval of interest.
	 * @return a list of {@link PublicationsStatistics} , containing the number of publications for each year.
	 * @throws SodaHibernateException
	 */
	public List<PublicationsStatistics> getPublicationsTimeWindowTrend(int firstYear, int lastYear) throws SodaHibernateException{
		if (firstYear>=lastYear)
			return null;
		
		List<Integer> yearsWindow = new ArrayList<Integer>();
		
		for (Integer year : this.conf.getYears()){
			if (year>=firstYear && year<=lastYear)
				yearsWindow.add(year);
		}
		
		List<PublicationsStatistics> publicationsTrend = 
			new ArrayList<PublicationsStatistics>();
		
		for (Integer y : yearsWindow){
			publicationsTrend.add(new PublicationsStatistics(y, this.getPublicationsPerYear(y) ));
			
		}		
		
		return publicationsTrend;
	}
	
	/**
	 * Returns informations about the co-authorship graph. 
	 * The graph is built with all the authors and co-authorship relations of the given year.
	 * For each year we have informations about: the graph size, 
	 * the number of connected components in the graph (connected sub-graphs),
	 * the size of the largest connected component,
	 * the size of the second largest connected component,
	 * the average clustering coefficient of the largest connected component,
	 * the characteristic path length of the largest connected component,
	 * the diameter of the largest connected component.
	 * 
	 * @param year the year of interest.
	 * @return a {@link GraphStatistics} object containing informations about the co-authorship graph for the given year.
	 * @throws SodaHibernateException
	 */
	public GraphStatistics getGraphStatisticsByYear(int year) throws SodaHibernateException{
		GraphStatistics stats =  new GraphStatistics(year);
		this.conf.initializeYear(year);
		List<Proceedings> procs = this.conf.getProceedingsByYear(year);
		
		GraphManager gm = new GraphManager(procs);
		
		stats.setConnectedComponentsNumber(gm.getConnectedComponentsNumber());
		stats.setGraphSize(gm.getGraphSize());
		stats.setLargestComponentAVGCC(gm.getGraphClusteringCoefficient());
		stats.setLargestComponentCPL(gm.getGraphCharacteristicPathLength());
		stats.setLargestComponentDiameter(gm.getGraphDiameter());
		stats.setLargestConnectedComponentSize(gm.getLargestConnectedComponentSize());
		stats.setSecondLargestConnectedComponentSize(gm.getSecondLargestConnectedComponentSize());

		return stats;
	}
	
	/**
	 * Returns informations about the co-authorship graph for each year of the conference.
	 * A separate graph is built for each year, not considering the graphs of the previous years.
	 * 
	 * @see getGraphStatisticsByYear(int year)
	 * @return a {@link GraphStatistics} object containing informations about the co-authorship graph for each year.
	 * @throws SodaHibernateException
	 */
	public List<GraphStatistics> getGraphStatisticsTimeTrend() throws SodaHibernateException{
		List<GraphStatistics> graphTrend = new ArrayList<GraphStatistics>();
		
		for (Integer y : this.conf.getYears())
			graphTrend.add(getGraphStatisticsByYear(y));
			
		return graphTrend;
	}
	
	/**
	 * Returns informations about the co-authorship graph for each year of the conference.
	 * Each graph is built on the graph of the previous year therefore the graph of the last year will be the whole conference graph.
	 * 
	 * @return a {@link GraphStatistics} object containing informations about the co-authorship graph for each year.
	 * @throws SodaHibernateException
	 * @see getGraphStatisticsByYear(int year) 
	 */
	public List<GraphStatistics> getIncrementalGraphStatistics() throws SodaHibernateException{
		List<GraphStatistics> graphTrend = new ArrayList<GraphStatistics>();
		GraphManager gm = new GraphManager();
		this.conf.initializeAll();
		List<Integer> years = conf.getYears();
		
		for (int i=0; i<years.size();i++){
			gm.expandGraph(conf.getProceedingsByYear(years.get(i)));
			GraphStatistics stats =  new GraphStatistics(years.get(i));
			stats.setConnectedComponentsNumber(gm.getConnectedComponentsNumber());
			stats.setGraphSize(gm.getGraphSize());
			stats.setLargestComponentAVGCC(gm.getGraphClusteringCoefficient());
			stats.setLargestComponentCPL(gm.getGraphCharacteristicPathLength());
			stats.setLargestComponentDiameter(gm.getGraphDiameter());
			stats.setLargestConnectedComponentSize(gm.getLargestConnectedComponentSize());
			stats.setSecondLargestConnectedComponentSize(gm.getSecondLargestConnectedComponentSize());
			stats.setAverageDegree(gm.getAverageDegreeWholeGraph());
			graphTrend.add(stats);
		}
		
		return graphTrend;
	}
	
	
	/**
	 * Returns the productivity rate, i.e. the average number of papers per author, for each year of the conference.
	 * For each year the productivity rate is computed considering also old authors who did not publish anything at the given year.
	 * @return a list of {@link PublicationsStatistics}. For each object only the year and productivity fields are initialized.
	 * @throws SodaHibernateException
	 */
	public List<PublicationsStatistics> getProductivityTrend() throws SodaHibernateException{
		ArrayList<PublicationsStatistics> prod = new ArrayList<PublicationsStatistics>();
		List<Integer> years = conf.getYears();
		List<Person> authors = new ArrayList<Person>();
		for (int i = 0; i<years.size(); i++){
			int y = years.get(i);
			PublicationsStatistics ps = new PublicationsStatistics(y);
			List<Person> newAuthors = getNewAuthorsPerYear(y, authors);
			authors.addAll(newAuthors);
			//Calculate productivity rate for this year
			int pubNumber= getPublicationsPerYear(y);
			double product = (double)pubNumber / (double)authors.size();
			ps.setProductivity(product);
			prod.add(ps);
		}
		return prod;
	}
	
	/**
	 * Returns the productivity rate, i.e. the average number of papers per author, for each year of the conference.
	 * For each year the productivity rate is computed considering only active authors, i.e. who published at least one paper, at the given year.
	 * @return a list of {@link PublicationsStatistics}. For each object only the year and productivity fields are initialized.
	 * @throws SodaHibernateException
	 */
	public List<PublicationsStatistics> getProductivityTrendActiveAuthors() throws SodaHibernateException{
		ArrayList<PublicationsStatistics> prod = new ArrayList<PublicationsStatistics>();
		List<Integer> years = conf.getYears();
		for (int i = 0; i<years.size(); i++){
			int y = years.get(i);
			PublicationsStatistics ps = new PublicationsStatistics(y);
			//this contains all the authors until year y
			int authorsNumber = getAuthorsPerYear(y).size();
			//Calculate productivity rate for this year
			int pubNumber= getPublicationsPerYear(y);
			double product = (double)pubNumber / (double)authorsNumber;
			ps.setProductivity(product);
			prod.add(ps);
		}
		return prod;
	}
}
