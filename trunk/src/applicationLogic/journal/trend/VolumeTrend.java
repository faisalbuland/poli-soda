package applicationLogic.journal.trend;


import java.util.ArrayList;
import java.util.List;

import applicationLogic.graph.GraphManager;
import applicationLogic.journal.Transaction;
import applicationLogic.statistics.AuthorsStatistics;
import applicationLogic.statistics.GraphStatistics;
import applicationLogic.statistics.PublicationsStatistics;

import dblp.social.exceptions.SodaHibernateException;
import dblp.social.hibernate.entities.DblpPublication;
import dblp.social.hibernate.entities.Journal;
import dblp.social.hibernate.entities.Person;
/**
 * This class provides a set of methods to analyze the trend trough volumes of a given Transaction object.
 * 
 * @author Staffiero
 *
 */
public class VolumeTrend {
	/**
	 * A Transaction object representing the current transaction.
	 */
	private Transaction tran;
	
	/**
	 * The constructor to be used
	 * 
	 * @param transaction a {@link Transaction} object representing the transaction to be analyzed.
	 * @throws NullPointerException if conf==null
	 */
	public VolumeTrend(Transaction transaction){
		if (transaction==null)
			throw new NullPointerException("Cannot initialize the VolumeTrend object with a null reference");
		this.tran = transaction;
	}
	
	
	/**
	 * Returns a set of Person. 
	 * This set contains all the authors who authored a publication in the given volume, considering the given transaction.
	 * 
	 * @param volume the volume number of interest.
	 * @return the set of authors for the given volume number
	 * @throws SodaHibernateException
	 */
	public List<Person> getAuthorsPerVolume(int volume) throws SodaHibernateException{
		Journal journal = this.tran.getJournalByVolume(volume);
				
		List<Person> authors = new ArrayList<Person>();
		List<DblpPublication> pubs = journal.getArticles();
		for (DblpPublication pb : pubs){
			List<Person> ipAuthors = pb.getAuthors();
			for(Person a : ipAuthors){
				if (!authors.contains(a)){
					authors.add(a);
				}
			}
		}
		
//		System.out.println("Found "+authors.size()+ " authors");
		return authors;
	}
	
	/**
	 * Returns a list of Person containing all the new authors for the given volume number.
	 * An author is a new author if he does not appear in the list previousAuthors.
	 * 
	 * @param year the year of interest.
	 * @param previousAuthors a list of "old" authors. Each author in this list will not be included in the returned list.
	 * @return a list of new authors.
	 * @throws SodaHibernateException
	 */
	public List<Person> getNewAuthorsPerVolume(int volume, List<Person> previousAuthors) throws SodaHibernateException{
		Journal journal = this.tran.getJournalByVolume(volume);
		
		List<Person> authors = new ArrayList<Person>();
		List<DblpPublication> pubs = journal.getArticles();
		for (DblpPublication pb : pubs){
			List<Person> pbAuthors = pb.getAuthors();
			
			for(Person a : pbAuthors){
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
		
//		System.out.println("Found "+authors.size()+ " new authors");
		return authors;
	}
	
	
	/**
	 * Returns a list of AuthorsStatistics, which contains, for each volume number: 
	 * the number of total authors, the number of new authors and the number of total authors  until the current volume number
	 * 
	 * @return a list of {@link AuthorsStatistics}.
	 * @throws SodaHibernateException
	 */
	public List<AuthorsStatistics> getAuthorsVolumeTrend() throws SodaHibernateException{
		List<AuthorsStatistics> authorsTrend = new ArrayList<AuthorsStatistics>();
		List<Person> authors = new ArrayList<Person>();
		
		for (Integer v : this.tran.getVolumes()){
			List<Person> aotv = this.getAuthorsPerVolume(v);
			List<Person> naotv = this.getNewAuthorsPerVolume(v, authors);
			authors.addAll(naotv);
			authorsTrend.add(new AuthorsStatistics(v,aotv.size(),naotv.size(), authors.size()));
			
		}		
		
		return authorsTrend;
	}
	
	/**
	 * Returns a list of AuthorsStatistics, within a given volumes range.
	 * for each year we have: the number of total authors, the number of new authors and the number of total authors  (until the current year).
	 * 
	 * 
	 * @param firstVolume the first volume of the interval of interest.
	 * @param lastVolume the last volume of the interval of interest.
	 * @return null if firstVolume>=LastVolume, a list of {@link AuthorsStatistics} otherwise
	 * @throws SodaHibernateException
	 */
	public List<AuthorsStatistics> getAuthorsVolumeWindowTrend(int firstVolume, int lastVolume) throws SodaHibernateException{
		if (firstVolume>=lastVolume)
			return null;
		
		List<Integer> volumesWindow = new ArrayList<Integer>();
		
		for (Integer volume : this.tran.getVolumes()){
			if (volume>=firstVolume && volume <=lastVolume)
				volumesWindow.add(volume);
		}
		
		List<AuthorsStatistics> authorsTrend = new ArrayList<AuthorsStatistics>();
		List<Person> authors = new ArrayList<Person>();
		
		
		for (Integer v : volumesWindow){
			List<Person> aotv = this.getAuthorsPerVolume(v);
			List<Person> naotv = this.getNewAuthorsPerVolume(v, authors);
			authors.addAll(naotv);
			authorsTrend.add(new AuthorsStatistics(v,aotv.size(),naotv.size(), authors.size()));
			
		}		
		
		return authorsTrend;
	}
	
	
	
	
	
	/**
	 * Returns the total number of publications for a certain volume number, considering the given transaction.
	 * 
	 * @param volume the volume of interest.
	 * @return the total number of publications for the given volume.
	 * @throws SodaHibernateException
	 */
	public int getPublicationsPerVolume(int volume) throws SodaHibernateException{
		int publicationsNumber = 0;
		Journal journal = this.tran.getJournalByVolume(volume);
		
		publicationsNumber = journal.getArticles().size();  	
		
		return publicationsNumber;
	}

	/**
	 * Returns a list of PublicationStatistics, containing the number of publications for each volume number. 
	 * 
	 * @return a list of {@link PublicationsStatistics}.
	 * @throws SodaHibernateException
	 */
	public List<PublicationsStatistics> getPublicationsVolumeTrend() throws SodaHibernateException{
		List<PublicationsStatistics> publicationsTrend = 
			new ArrayList<PublicationsStatistics>();
		
		for (Integer v : this.tran.getVolumes()){
			publicationsTrend.add(new PublicationsStatistics(v, this.getPublicationsPerVolume(v) ));
			
		}		
		
		return publicationsTrend;
	}
	
	/**
	 * Returns a list of PublicationStatistics.
	 * The list contains the number of publications for each volume, for a given volume numbers range. 
	 * 
	 * @param firstVolume the first volume of the interval of interest.
	 * @param lastVolume the last volume of the interval of interest.
	 * @return a list of {@link PublicationsStatistics}, containing the number of publications for each year.
	 * @throws SodaHibernateException
	 */
	public List<PublicationsStatistics> getPublicationsVolumeWindowTrend(int firstVolume, int lastVolume) throws SodaHibernateException{
		if (firstVolume>=lastVolume)
			return null;
		
		List<Integer> volumesWindow = new ArrayList<Integer>();
		
		for (Integer vol : this.tran.getVolumes()){
			if (vol>=firstVolume&& vol<=lastVolume)
				volumesWindow.add(vol);
		}
		
		List<PublicationsStatistics> publicationsTrend = 
			new ArrayList<PublicationsStatistics>();
		
		for (Integer v : volumesWindow){
			publicationsTrend.add(new PublicationsStatistics(v, this.getPublicationsPerVolume(v) ));
			
		}		
		
		return publicationsTrend;
	}
	
	/**
	 * Returns informations about the co-authorship graph. 
	 * The graph is built with all the authors and co-authorship relations of the given volume number.
	 * For each volume we have informations about: the graph size, 
	 * the number of connected components in the graph (connected sub-graphs),
	 * the size of the largest connected component,
	 * the size of the second largest connected component,
	 * the average clustering coefficient of the largest connected component,
	 * the characteristic path length of the largest connected component,
	 * the diameter of the largest connected component.
	 * 
	 * @param volume the volume number of interest.
	 * @return a {@link GraphStatistics} object containing informations about the co-authorship graph for the given volume.
	 * @throws SodaHibernateException
	 */
	public GraphStatistics getGraphStatisticsByVolume(int volume) throws SodaHibernateException{
		GraphStatistics stats =  new GraphStatistics(volume);
		Journal journ = this.tran.getJournalByVolume(volume);
		
		
		GraphManager gm = new GraphManager(journ);
		
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
	 * Returns informations about the co-authorship graph for each Journal of the transaction.
	 * A separate graph is built for each volume, not considering the graphs of the previous volumes.
	 * 
	 * @see getGraphStatisticsByVolume(int volume)
	 * @return a {@link GraphStatistics} object containing informations about the co-authorship graph for each volume.
	 * @throws SodaHibernateException
	 */
	public List<GraphStatistics> getGraphStatisticsVolumeTrend() throws SodaHibernateException{
		List<GraphStatistics> graphTrend = new ArrayList<GraphStatistics>();
		
		for (Integer v : this.tran.getVolumes())
			graphTrend.add(getGraphStatisticsByVolume(v));
			
		return graphTrend;
	}
	
	/**
	 * Returns informations about the co-authorship graph for each Journal of the transaction.
	 * Each graph is built on the graph of the previous year therefore the graph of the last volume will be the whole conference graph.
	 * 
	 * @return a {@link GraphStatistics} object containing informations about the co-authorship graph for each volume number.
	 * @throws SodaHibernateException
	 * @see getGraphStatisticsByVolume(int volume)
	 */
	public List<GraphStatistics> getIncrementalGraphStatistics() throws SodaHibernateException{
		List<GraphStatistics> graphTrend = new ArrayList<GraphStatistics>();
		GraphManager gm = new GraphManager();
		List<Integer> volumes = tran.getVolumes();
		
		for (int i=0; i<volumes.size();i++){
			gm.expandGraph(tran.getJournalByVolume(volumes.get(i)));
			GraphStatistics stats =  new GraphStatistics(volumes.get(i));
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
	 * Returns the productivity rate, i.e. the average number of papers per author, for each volume of the transaction.
	 * For each year the productivity rate is computed considering also old authors who did not publish anything at the given volume.
	 * @return a list of {@link PublicationsStatistics}. For each object only the year and productivity fields are initialized.
	 * @throws SodaHibernateException
	 */
	public List<PublicationsStatistics> getProductivityTrend() throws SodaHibernateException{
		ArrayList<PublicationsStatistics> prod = new ArrayList<PublicationsStatistics>();
		List<Integer> vols = this.tran.getVolumes();
		List<Person> authors = new ArrayList<Person>();
		for (int i = 0; i<vols.size(); i++){
			int v = vols.get(i);
			PublicationsStatistics ps = new PublicationsStatistics(v);
			List<Person> newAuthors = getNewAuthorsPerVolume(v, authors);
			authors.addAll(newAuthors);
			//Calculate productivity rate for this volume
			int pubNumber= getPublicationsPerVolume(v);
			double product = (double)pubNumber / (double)authors.size();
			ps.setProductivity(product);
			prod.add(ps);
		}
		return prod;
	}
	
	/**
	 * Returns the productivity rate, i.e. the average number of papers per author, for each volume of the transaction.
	 * For each volume the productivity rate is computed considering only active authors, i.e. who published at least one paper, at the given volume.
	 * @return a list of {@link PublicationsStatistics}. For each object only the year and productivity fields are initialized.
	 * @throws SodaHibernateException
	 */
	public List<PublicationsStatistics> getProductivityTrendActiveAuthors() throws SodaHibernateException{
		ArrayList<PublicationsStatistics> prod = new ArrayList<PublicationsStatistics>();
		List<Integer> vols = this.tran.getVolumes();
		for (int i = 0; i<vols.size(); i++){
			int v = vols.get(i);
			PublicationsStatistics ps = new PublicationsStatistics(v);

			int authorsNumber = getAuthorsPerVolume(v).size();
			//Calculate productivity rate for this volume
			int pubNumber= getPublicationsPerVolume(v);
			double product = (double)pubNumber / (double)authorsNumber;
			ps.setProductivity(product);
			prod.add(ps);
		}
		return prod;
	}
}
