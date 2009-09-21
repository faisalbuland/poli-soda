package applicationLogic.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.FactoryUtils;

import applicationLogic.conference.Conference;
import applicationLogic.graph.utils.ClosenessComparator;
import applicationLogic.graph.utils.ClusteringComparator;
import applicationLogic.graph.utils.DegreeComparator;
import applicationLogic.graph.utils.GraphEdge;
import applicationLogic.journal.Transaction;
import applicationLogic.statistics.Author;
import dblp.social.exceptions.SodaHibernateException;
import dblp.social.hibernate.entities.DblpPublication;
import dblp.social.hibernate.entities.InProceedings;
import dblp.social.hibernate.entities.Journal;
import dblp.social.hibernate.entities.Person;
import dblp.social.hibernate.entities.Proceedings;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
/**
 * This class provides a set of methods to create and analyze the co-authorship graph of a given Conference or Transaction object.
 * 
 * @author Staffiero
 *
 */
public class GraphManager {
	
	private Graph<String, GraphEdge> graph = null;
	
	/**
	 * Empty constructor, creates a new empty graph
	 * 
	 */
	public GraphManager(){
		this.graph = new SparseMultigraph<String, GraphEdge>();
	}
	
	
	/**
	 * Creates a new graph from a given conference.
	 * 
	 * @param conference the conference.
	 * @throws SodaHibernateException
	 */
	public GraphManager(Conference conference) throws SodaHibernateException{
		conference.initializeAll();
		initializeGraphFromConference(conference.getAllProceedings());
	}
	/**
	 * Creates a new graph from a given transaction.
	 * 
	 * @param transaction the transaction
	 */
	public GraphManager(Transaction transaction){
		initializeGraphFromTransaction(transaction.getAllJournals());
	}
	/**
	 * Creates a new graph from a given list of both Proceedings or Journal
	 * @param list the list from which the new graph is created.
	 * @throws ClassCastException if the given list is not a list of proceedings or journal
	 */
	@SuppressWarnings("unchecked")
	public GraphManager(List list){
		String clazz = list.get(0).getClass().getName();
		if (clazz.compareTo(Proceedings.class.getName())==0)
			this.initializeGraphFromConference((List<Proceedings>)list);
		else if (clazz.compareTo(Journal.class.getName())==0)
			this.initializeGraphFromTransaction((List<Journal>)list);
		else 
			throw new ClassCastException("The given list is not a list of Proceedings or Journal");
	}
	/**
	 * Creates a new graph from a single journal.
	 * 
	 * @param journal the journal
	 */
	public GraphManager(Journal journal){
		this.graph = new SparseMultigraph<String, GraphEdge>();
		int edgeId = 0;
		List<DblpPublication> alist = journal.getArticles();
		for (DblpPublication pub : alist){
			
			List<Person> authors = pub.getAuthors();
			for (Person a : authors){
				this.graph.addVertex(a.getName());
			}
			for (Person a1 : authors){
				
				for (Person a2 : authors){
					if ( !(a1.getName().compareTo(a2.getName())==0) ){
						Collection<String> neighbors = 
							this.graph.getNeighbors(a1.getName());
						
						if (!neighbors.contains(a2.getName())){
							this.graph.addEdge(new GraphEdge(edgeId), 
									a1.getName(), a2.getName());
							edgeId++;
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Private method which creates a new graph from a list of Proceedings.
	 * 
	 * @param procs the proceedings list
	 */
	private void initializeGraphFromConference(List<Proceedings> procs){
		this.graph = new SparseMultigraph<String, GraphEdge>();
		int edgeId = 0;
		for ( Proceedings p : procs){
			List<InProceedings> ilist = p.getInProceedings();
			for (InProceedings i : ilist){
				
				List<Person> authors = i.getAuthors();
				for (Person a : authors){
					this.graph.addVertex(a.getName());
				}
				for (Person a1 : authors){
					
					for (Person a2 : authors){
						if ( !(a1.getName().compareTo(a2.getName())==0) ){
							Collection<String> neighbors = 
								this.graph.getNeighbors(a1.getName());
							
							if (!neighbors.contains(a2.getName())){
								this.graph.addEdge(new GraphEdge(edgeId), 
										a1.getName(), a2.getName());
								edgeId++;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Private method which creates a new graph from a list of Journal.
	 * 
	 * @param journals the journals list
	 */
	private void initializeGraphFromTransaction(List<Journal> journals){
		this.graph = new SparseMultigraph<String, GraphEdge>();
		int edgeId = 0;
		for ( Journal j : journals){
			List<DblpPublication> alist = j.getArticles();
			for (DblpPublication pub : alist){
				
				List<Person> authors = pub.getAuthors();
				for (Person a : authors){
					this.graph.addVertex(a.getName());
				}
				for (Person a1 : authors){
					
					for (Person a2 : authors){
						if ( !(a1.getName().compareTo(a2.getName())==0) ){
							Collection<String> neighbors = 
								this.graph.getNeighbors(a1.getName());
							
							if (!neighbors.contains(a2.getName())){
								this.graph.addEdge(new GraphEdge(edgeId), 
										a1.getName(), a2.getName());
								edgeId++;
							}
						}
					}
				}
			}
		}
	}
	/**
	 * Adds to the current graph all the authors and co-authors relations of the given conference.
	 * 
	 * @param conf the conference to add to the current graph
	 * @return true if the graph has been expanded without errors, false otherwise
	 * @throws SodaHibernateException
	 */
	public boolean expandGraph(Conference conf) throws SodaHibernateException{
		if (this.graph == null)
			return false;
		conf.initializeAll();
		boolean result = expandGraphWithConference(conf.getAllProceedings());
		return result;
	}
	
	/**
	 * Adds to the current graph all the authors and co-authors relations of the given transaction.
	 * 
	 * @param tran the transaction to add to the current graph
	 * @return true if the graph has been expanded without errors, false otherwise
	 * @throws SodaHibernateException
	 */
	public boolean expandGraph(Transaction tran){
		if (this.graph == null)
			return false;
		boolean result = expandGraphWithTransaction(tran.getAllJournals());
		return result;
	}
	/**
	 * Adds to the current graph all the authors and co-authors relations of a given list of proceedings or journal.
	 * 
	 * @param list the list of proceedings or journal to add to the graph.
	 * @return true if the graph has been expanded without errors, false otherwise
	 * @throws ClassCastException if the given list is not a list of proceedings or journal
	 */
	@SuppressWarnings("unchecked")
	public boolean expandGraph(List list){
		if (this.graph == null)
			return false;
		
		String clazz = list.get(0).getClass().getName();
		
		boolean result = false;
		
		if (clazz.compareTo(Proceedings.class.getName())==0)
			result = this.expandGraphWithConference((List<Proceedings>)list);
		else if (clazz.compareTo(Journal.class.getName())==0)
			result = this.expandGraphWithTransaction((List<Journal>)list);
		else throw new ClassCastException("The given list is not a list of Proceedings or Journal");
		
		return result;
	}
	
	/**
	 * Adds to the current graph all the authors and co-authors relations of the given journal.
	 * @param journal the journal to add to the graph.
	 * @return true if the graph has been expanded without errors, false otherwise
	 */
	public boolean expandGraph(Journal journal){
		if (this.graph == null)
			return false;
		boolean result = false;
		int edgeId = this.graph.getEdgeCount()-1;
		List<DblpPublication> alist = journal.getArticles();
		for (DblpPublication pub : alist){
			
			List<Person> authors = pub.getAuthors();
			for (Person a : authors){
				this.graph.addVertex(a.getName());
			}
			for (Person a1 : authors){
				
				for (Person a2 : authors){
					if ( !(a1.getName().compareTo(a2.getName())==0) ){
						Collection<String> neighbors = 
							this.graph.getNeighbors(a1.getName());
						
						if (!neighbors.contains(a2.getName())){
							this.graph.addEdge(new GraphEdge(edgeId), 
									a1.getName(), a2.getName());
							edgeId++;
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Adds to the current graph all the authors and co-authors relations of the given list of Proceedings.
	 * @param procs the list of proceedings to add to the graph.
	 * @return true if the graph has been expanded without errors, false otherwise
	 */
	
	private boolean expandGraphWithConference(List<Proceedings> procs){
		int edgeId = this.graph.getEdgeCount()-1;
		
		for ( Proceedings p : procs){
			List<InProceedings> ilist = p.getInProceedings();
			for (InProceedings i : ilist){
				
				List<Person> authors = i.getAuthors();
				for (Person a : authors){
					this.graph.addVertex(a.getName());
				}
				for (Person a1 : authors){
					
					for (Person a2 : authors){
						if ( !(a1.getName().compareTo(a2.getName())==0) ){
							Collection<String> neighbors = 
								this.graph.getNeighbors(a1.getName());
							
							if (!neighbors.contains(a2.getName())){
								this.graph.addEdge(new GraphEdge(edgeId), 
										a1.getName(), a2.getName());
								edgeId++;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Adds to the current graph all the authors and co-authors relations of the given list of Journal.
	 * @param journals the list of journal to add to the graph.
	 * @return true if the graph has been expanded without errors, false otherwise
	 */
	private boolean expandGraphWithTransaction(List<Journal> journals){
		int edgeId = this.graph.getEdgeCount()-1;
		
		for ( Journal j : journals){
			List<DblpPublication> alist = j.getArticles();
			for (DblpPublication pub : alist){
				
				List<Person> authors = pub.getAuthors();
				for (Person a : authors){
					this.graph.addVertex(a.getName());
				}
				for (Person a1 : authors){
					
					for (Person a2 : authors){
						if ( !(a1.getName().compareTo(a2.getName())==0) ){
							Collection<String> neighbors = 
								this.graph.getNeighbors(a1.getName());
							
							if (!neighbors.contains(a2.getName())){
								this.graph.addEdge(new GraphEdge(edgeId), 
										a1.getName(), a2.getName());
								edgeId++;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns the current graph.
	 * @return the current graph.
	 */
	public Graph<String, GraphEdge> getGraph() {
		return graph;
	}

	
	/**
	 * Extracts all the connected subgraphs from the current graph.
	 * 
	 * @return a collection of connected subgraphs.
	 */
	private Collection<Graph<String, GraphEdge>> extractConnectedSubgraphs(){
		WeakComponentClusterer<String, GraphEdge> wcc = 
			new WeakComponentClusterer<String, GraphEdge>();
		Set<Set<String>> components = wcc.transform(this.graph);
		Collection<Graph<String, GraphEdge>> subGraphs = 
			 FilterUtils.createAllInducedSubgraphs(components, this.graph);
		return subGraphs;
	}
	
	/**
	 * Extracts the largest connected subgraph from the current graph.
	 * 
	 * @return the largest connected subgraph.
	 */
	private Graph<String, GraphEdge> extractLargestConnectedSubgraph(){
		WeakComponentClusterer<String, GraphEdge> wcc = 
			new WeakComponentClusterer<String, GraphEdge>();
		Set<Set<String>> components = wcc.transform(this.graph);
		
		long largestSize = 0;
		Set<String> largestSet = null;
		
		for (Set<String> nodesSet : components){
			if (nodesSet.size()>largestSize){
				largestSize = nodesSet.size();
				largestSet = nodesSet;
			}	
		}
		
		Graph<String, GraphEdge> subGraph = 
			 FilterUtils.createInducedSubgraph(largestSet, this.graph);
		return subGraph;
	}
	
	/**
	 * Returns the size of the current graph.
	 * @return the size of the current graph.
	 */
	public int getGraphSize(){
		return this.graph.getVertexCount();
	}
	
	/**
	 * Returns the size of the largest connected component  of the current graph.
	 * @return the size of the largest connected subgraph.
	 */
	public int getLargestConnectedComponentSize(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		return lcs.getVertexCount();
	}
	/**
	 * Returns the size of the second largest connected component of the current graph.
	 * @return the size of the second largest connected subgraph.
	 */
	public int getSecondLargestConnectedComponentSize(){
		WeakComponentClusterer<String, GraphEdge> wcc = 
			new WeakComponentClusterer<String, GraphEdge>();
		Set<Set<String>> components = wcc.transform(this.graph);
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		for (Set<String> nodesSet : components)
			sizes.add(nodesSet.size());
		
		Collections.sort(sizes);
		
		return sizes.get(sizes.size()-2);
	}
	/**
	 * 
	 * @return the number of connected subgraphs.
	 */
	public int getConnectedComponentsNumber(){
		Collection<Graph<String, GraphEdge>> graphs = extractConnectedSubgraphs();
		return graphs.size();
	}
	/**
	 * 
	 * @return the current graph diameter
	 */
	@SuppressWarnings("unused")
	public int getGraphDiameter(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		
		ArrayList<String> nodes = new ArrayList<String>();
		nodes.addAll(lcs.getVertices());
		int maxLength = 0;
		String source = "";
		String dest = "";
		for (String node1 :  lcs.getVertices()){
			UnweightedShortestPath<String, GraphEdge> usp = 
				new UnweightedShortestPath<String, GraphEdge>(lcs);
			
			Map<String, Number> distances = usp.getDistanceMap(node1);
			for (int i=0; i<nodes.size();i++){
				String node2 = nodes.get(i);
				if (node1.compareTo(node2)!=0){
					int dist = (Integer)distances.get(node2);
					if (dist>maxLength){
						maxLength=dist;
						source = node1;
						dest = node2;
					}
				}
			}
			nodes.remove(node1);
		}
		
//		System.out.println("Longest path: "+maxLength+" from "+ source+" to "+dest);
		
		return maxLength;
	}
	
	/**
	 * 
	 * @return the characteristic path length of the largest connected subgraph.
	 */
	public double getGraphCharacteristicPathLength(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		
		ArrayList<String> nodes = new ArrayList<String>();
		nodes.addAll(lcs.getVertices());
		double sum = 0;
		int cont = 0;
		for (String node1 :  lcs.getVertices()){
			UnweightedShortestPath<String, GraphEdge> usp = 
				new UnweightedShortestPath<String, GraphEdge>(lcs);
			
			Map<String, Number> distances = usp.getDistanceMap(node1);
			for (int i=0; i<nodes.size();i++){
				String node2 = nodes.get(i);
				if (node1.compareTo(node2)!=0){
					sum +=(Integer)distances.get(node2);
					cont++;
				}
			}
			nodes.remove(node1);
		}
		
		double cpl = sum/cont;
		return cpl;
	}
	
	
	/**
	 * Returns the normalized degree distribution of the largest connected subgraph as a List of Double. 
	 * The degree is the index of the list.
	 * @return a list of Double representing a bar chart of the normalized degree distribution.
	 */
	public ArrayList<Double> getNormalizedDegreeDistribution(){
		ArrayList<Double> degreeDistribution = new ArrayList<Double>();
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		
		int maxDegree=0;
		for (String node : lcs.getVertices()){
			int nodeDegree = lcs.degree(node);
			if (nodeDegree>maxDegree)
				maxDegree=nodeDegree;
		}
		
		for  (int i=0; i<=maxDegree; i++)
			degreeDistribution.add(new Double(0));
		
		for (String node : lcs.getVertices()){
			int nodeDegree = lcs.degree(node);
			Double curr = degreeDistribution.get(nodeDegree);
			curr = degreeDistribution.get(nodeDegree) +1;
			degreeDistribution.set(nodeDegree, curr);
		}
		
		for (int i=0; i<degreeDistribution.size(); i++){
			Double curr = degreeDistribution.get(i);
			curr = degreeDistribution.get(i)/lcs.getVertexCount();
			degreeDistribution.set(i, curr);
		}
		
		
		return degreeDistribution;
	}
	
	/**
	 * Returns the normalized degree distribution of the largest connected subgraph as a List of Integer. 
	 * The degree is the index of the list.
	 * @return a list of Integer representing a histogram of the degree distribution.
	 */
	public ArrayList<Integer> getDegreeDistribution(){
		ArrayList<Integer> degreeDistribution = new ArrayList<Integer>();
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		
		int maxDegree=0;
		for (String node : lcs.getVertices()){
			int nodeDegree = lcs.degree(node);
			if (nodeDegree>maxDegree)
				maxDegree=nodeDegree;
		}
		
		for  (int i=0; i<=maxDegree; i++)
			degreeDistribution.add(new Integer(0));
		
		for (String node : lcs.getVertices()){
			int nodeDegree = lcs.degree(node);
			Integer curr = degreeDistribution.get(nodeDegree);
			curr = degreeDistribution.get(nodeDegree) +1;
			degreeDistribution.set(nodeDegree, curr);
		}
		
		
		return degreeDistribution;
	}
	
	/**
	 * 
	 * @return the average degree of the largest connected subgraph.
	 */
	public double getAverageDegree(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		
		int degSum=0;
		for (String node : lcs.getVertices()){
			int nodeDegree = lcs.degree(node);
			degSum += nodeDegree;
		}
		double avgdg = (double)degSum/(double)lcs.getVertexCount();
		
		return avgdg;
	}
	
	/**
	 * 
	 * @return the average degree of the whole graph.
	 */
	public double getAverageDegreeWholeGraph(){
		
		int degSum=0;
		for (String node : this.graph.getVertices()){
			int nodeDegree = this.graph.degree(node);
			degSum += nodeDegree;
		}
		double avgdg = (double)degSum/(double)this.graph.getVertexCount();
		
		return avgdg;
	}
	
	public double getGraphClusteringCoefficient(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		Map<String,Double> scoreMap = Metrics.clusteringCoefficients(lcs);
		double sum = 0;
		for (String node : lcs.getVertices()){
			sum += scoreMap.get(node);
		}
		double acc = sum/lcs.getVertexCount();
		return acc;
	}
	
	
	
	/**
	 * Computes the Closeness Centrality score for each author in the largest connected subgraph.
	 * 
	 * @return a list of authors. Each author object contains the author's name and the closeness centrality score.
	 * Other fields are not initialized 
	 */
	public List<Author> getAuthorsClosenessCentrality(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		
		List<Author> authors = new ArrayList<Author>();
		for (String node : lcs.getVertices()){
			Author a = new Author(node);
			ClosenessCentrality<String, GraphEdge> cc = new ClosenessCentrality<String, GraphEdge>(lcs);
			a.setClosenessCentrality(cc.getVertexScore(node));
			authors.add(a);
		}
		
		Collections.sort(authors,new ClosenessComparator());
		
		return authors;
	}
	
	
	/**
	 * Calculates the Betweenness Centrality score for each author in the largest connected subgraph.
	 * 
	 * @return a list of authors. Each author object contains the author's name and the betweenness centrality score
	 * Other fields are not initialized 
	 */
	public List<Author> getAuthorsBetweennessCentrality(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		
		List<Author> authors = new ArrayList<Author>();
		for (String node : lcs.getVertices()){
			Author a = new Author(node);
			BetweennessCentrality<String, GraphEdge> bc = new BetweennessCentrality<String, GraphEdge>(lcs);
			a.setClosenessCentrality(bc.getVertexScore(node));
			authors.add(a);
		}
		
		return authors;
	}
	
	
	/**
	 * Calculates the degree for each author in the largest connected subgraph.
	 * 
	 * @return a list of authors. Each author object contains the author's name and his degree.
	 * Other fields are not initialized 
	 */
	public List<Author> getAuthorsDegree(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		List<Author> authors = new ArrayList<Author>();
		for (String node : lcs.getVertices()){
			Author a = new Author(node);
			a.setDegree(lcs.degree(node));
			authors.add(a);
		}
		
		Collections.sort(authors,new DegreeComparator());
		
		return authors;
	}
	
	/**
	 * Calculates the clustering coefficient for each author in the largest connected subgraph.
	 * 
	 * @return a list of authors. Each author object contains the author's name and the clustering coefficient score.
	 * Other fields are not initialized 
	 */
	public List<Author> getAuthorsClusteringCoefficient(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		List<Author> authors = new ArrayList<Author>();

		Map<String,Double> scoreMap = Metrics.clusteringCoefficients(lcs);
		
		for (String node : lcs.getVertices()){
			Author a = new Author(node);
			a.setClusteringCoefficient(scoreMap.get(node));
			authors.add(a);
		}
		
		Collections.sort(authors,new ClusteringComparator());
		
		return authors;
	}
	
	
	
	/**
	 * Calculates the Eigenvector Centrality score for each author in the largest connected subgraph.
	 * 
	 * @return a list of authors. Each author object contains the author's name and the Eigenvector centrality score
	 * Other fields are not initialized 
	 */
	public List<Author> getAuthorsEigenvectorCentrality(){
		Graph<String, GraphEdge> lcs = extractLargestConnectedSubgraph();
		
		List<Author> authors = new ArrayList<Author>();
		for (String node : lcs.getVertices()){
			Author a = new Author(node);
			EigenvectorCentrality<String, GraphEdge> ec = new EigenvectorCentrality<String, GraphEdge>(lcs);
			a.setEigenvectorCentrality(ec.getVertexScore(node));
			authors.add(a);
		}
		
		return authors;
	}
	
//	public List<Author> getAuthorsProperties(){
//		List<Author> authors = new ArrayList<Author>();
//		Graph<String, GraphEdge> graph = extractLargestConnectedSubgraph();
//		
////		Map<String,Double> scoreMap = Metrics.clusteringCoefficients(graph);
//		
//		for (String node : graph.getVertices()){
////			ClosenessCentrality<String, GraphEdge> cc = new ClosenessCentrality<String, GraphEdge>(graph);
//			BetweennessCentrality<String, GraphEdge> bc = new BetweennessCentrality<String, GraphEdge>(graph);
//			EigenvectorCentrality<String, GraphEdge> ec = new EigenvectorCentrality<String, GraphEdge>(graph);
//			Author a = new Author(node);
////			a.setClosenessCentrality(cc.getVertexScore(node));
////			a.setDegree(graph.degree(node));
//			a.setBetweennessCentrality(bc.getVertexScore(node));
////			a.setEigenvectorCentrality((Double)ec.getVertexScore(node));
////			a.setClusteringCoefficient(scoreMap.get(node));
//			authors.add(a);
//		}
//		
//		return authors;
//	} 
}
