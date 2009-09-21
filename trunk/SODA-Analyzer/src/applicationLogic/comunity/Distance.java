package applicationLogic.comunity;

import java.util.ArrayList;
import java.util.List;

import dblp.social.exceptions.SodaHibernateException;
import dblp.social.hibernate.ISodaHibernateSession;
import dblp.social.hibernate.WorkArounds;
import dblp.social.hibernate.entities.DblpPublication;
import dblp.social.hibernate.entities.Person;
/**
 * This class provides two methods to find the distance between two authors in the database, in terms of co-authorship, within a given limit.
 * Starting from the first author the search method is: width-first therefore the solution will always be the optimum one.
 * The first method works on string sets. It finds the distance from two authors but does not record the solution path. 
 * The second method is designed on the standard AI research algorithm: it creates the research tree, a set of TreeNode, 
 * it uses a second set, the fringe, to store untested nodes, and it tests and expands each node in the fringe.
 * The fringe is an ordered queue, in order to implement the width-first research the fringe is realized as a FIFO queue.
 * The second method records the solution path, but because of this it uses more resources (in particular memory) and time than the first.
 * 
 * @author Staffiero
 *
 */
public class Distance {
	private ISodaHibernateSession session;
	
	/**
	 * The constructor to be used.
	 * 
	 * @param session a valid SodaHibernateSession object
	 */
	public Distance(ISodaHibernateSession session){
		this.session = session;
	}
	
	/**
	 * Calculates the distance between two authors working on String sets. 
	 * The solution path is not stored.
	 * 
	 * 
	 * @param author1 the first author
	 * @param author2 the second author
	 * @param limit the search limit
	 * @return the distance between the two authors OR
	 * -1 if the authors' distance is greater than limit OR
	 * -2 if author1 cannot be found in the database OR
	 * -3 if author2 cannot be found in the database.
	 * @throws SodaHibernateException
	 */
	@SuppressWarnings("unchecked")
	public int distance(String author1, String author2, int limit) throws SodaHibernateException{
		int distance = -1;
		
		//1 - Both authors have to be in the db
		if(!this.session.isOpen())
			this.session.open();
		
		List<Person> p1 = session.getNamedQuery("findPersonByName").setString("personName", author1).list();
		
		if (p1.size()==0)
			return -2;
		
		List<Person> p2 = session.getNamedQuery("findPersonByName").setString("personName", author2).list();
		
		if (p2.size()==0)
			System.out.println("Author "+author2+" not found");
		
		
		this.session.close();
		p1=null;
		p2=null;
		
		if (author1.compareTo(author2)==0)
			return 0;
		
		List<String> inspectedAuthors = new ArrayList<String>();
		List<String> currentLevelAuthors = new ArrayList<String>();
		List<String> nextLevelAuthors = new ArrayList<String>();
 		
		int level=0;
		currentLevelAuthors.add(author1);
		boolean found = false;
		while(level<limit && !found){
			level++;
			
			for (String cla : currentLevelAuthors){
				List<String> coAuthors = getCoauthors(cla);
				for (String coAuthor : coAuthors){
					if (coAuthor.compareTo(author2)==0)
						return level;
					if (!inspectedAuthors.contains(coAuthor) &&
							!currentLevelAuthors.contains(coAuthor))
						nextLevelAuthors.add(coAuthor);
				}
			}
			
			inspectedAuthors.addAll(currentLevelAuthors);
			currentLevelAuthors = nextLevelAuthors;
			nextLevelAuthors = new ArrayList<String>();
			
			System.out.println("Level: "+level+
					"\nInspected Authors: "+inspectedAuthors.size()+
					"\nCurrent level authors: "+currentLevelAuthors.size());
		}
		
		return distance;
	}
	
	/**
	 * Calculates the distance between two authors working on String sets. 
	 * The solution path is stored.
	 * 
	 * @param author1 the first author
	 * @param author2 the second author
	 * @param limit the search limit
	 * @return the distance between the two authors OR
	 * -1 if the authors' distance is greater than limit OR
	 * -2 if author1 cannot be found in the database OR
	 * -3 if author2 cannot be found in the database.
	 * @throws SodaHibernateException
	 */
	//Algoritmo ricerca-albero stile I.A.
	@SuppressWarnings("unchecked")
	public int distanceAI(String author1, String author2, int limit) throws SodaHibernateException{
		//1 - Both authors have to be in the db
		if(!this.session.isOpen())
			this.session.open();
		
		List<Person> p1 = session.getNamedQuery("findPersonByName").setString("personName", author1).list();
		
		if (p1.size()==0)
			return -2;
		
		List<Person> p2 = session.getNamedQuery("findPersonByName").setString("personName", author2).list();
		
		
		if (p2.size()==0)
			return -3;
		
		this.session.close();
		p1=null;
		p2=null;
		
		//The author who have the lower co-authors number will be the first one
		int ca1 = countCoauthors(author1);
		int ca2 = countCoauthors(author2);
		
		System.out.println(author1+" has "+ca1+" coauthors.\n" +
				author2+" has "+ca2+" coauthors");
		
		if (ca1>ca2){
			String tmp = author1;
			author1=author2;
			author2=tmp;
		}
		
		List<String> expandedAuthors = new ArrayList<String>();
		Fringe fringe = new Fringe();
		
		if (author1.compareTo(author2)==0)
			return 0;
		
		int level=0;
		fringe.add(new TreeNode(author1,null,level));
		
		while(level<=limit){
			System.out.print("Fringe contains "+fringe.size()+" elements. "); 
			if (fringe.isEmpty()){
				System.out.println("Empty fringe");
				return -1;
			}
			TreeNode node = fringe.get();
			level = node.getDepth();
			
			System.out.print("Depth: "+level+"\n");
			
			if (level>limit){
				System.out.println("Current node level is too low");
				return -1;
			}
			if (objectiveTest(node,author2)){
				System.out.println("Found solution");
				System.out.println("\nSolution Path:");
				TreeNode tn = node;
				System.out.println(tn.getAuthorName()+" - distance: "+tn.getDepth());
				while (tn.getFather()!=null){
					tn = tn.getFather();
					System.out.println(tn.getAuthorName()+" - distance: "+tn.getDepth());
				}
				System.out.println("\n");
				return node.getDepth();
			}
			if(!expandedAuthors.contains(node.getAuthorName())){
				List<TreeNode> newNodes = expandNode(node, expandedAuthors, limit);
				if (newNodes!=null)
					fringe.addAll(newNodes);
			}
			
		}
		System.out.println("While loop terminated");
		return -1;
	}
	
	
	private int countCoauthors(String author) throws SodaHibernateException{
		int ca = 0;
		List<String> coAuthors = new ArrayList<String>();
		this.session.open();
		List<DblpPublication> pubs = WorkArounds.getAuthorPublications(author, this.session);
		
		for(DblpPublication p : pubs){
			for (Person a : p.getAuthors()){
				String name = a.getName();
				if (name!=author && !coAuthors.contains(name))
					coAuthors.add(name);
			}
				
		}
		this.session.close();
		ca = coAuthors.size();
		return ca;
	}
	
	
	/**
	 * Expands a node: given a node containing an author it finds all the co-authors and creates a new node for each co-author who is not already in the expanded authors array.
	 * 
	 * @param node the node to be expanded
	 * @param expandedAuthors the list of names of the already "expanded" authors
	 * @param limit the search limit, nodes whose depth is greater or equal to limit will not be expanded.
	 * @return the list of child nodes of the given node
	 * @throws SodaHibernateException
	 */
	private List<TreeNode> expandNode(TreeNode node, List<String> expandedAuthors, int limit ) throws SodaHibernateException{
		
		if (node.getDepth()>=limit)
			return null;
		String author = node.getAuthorName();
		
		List<String> coAuthors = new ArrayList<String>();
		this.session.open();
		List<DblpPublication> pubs = WorkArounds.getAuthorPublications(author, this.session);
		
		for(DblpPublication p : pubs){
			for (Person a : p.getAuthors()){
				String name = a.getName();
				if (name!=author && !coAuthors.contains(name))
					coAuthors.add(name);
			}
				
		}
		this.session.close();
		expandedAuthors.add(author);
		ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
		for (String a: coAuthors)
			nodes.add(new TreeNode(a,node,node.getDepth()+1));
		return nodes;
	}
	
	/**
	 * Test if a node matches the objective, i.e. if a node contains the author to be found
	 * @param node the node to be tested
	 * @param objective the name of the author to be found
	 * @return true if the node matches the objective test, false otherwise
	 */
	private static boolean objectiveTest(TreeNode node, String objective){
		if (node.getAuthorName().compareTo(objective)==0)
			return true;
		else
			return false;
	}
	
	/**
	 * Finds each co-author in the database for the given author name.
	 * @param author the author name
	 * @return the list of co-authors names
	 * @throws SodaHibernateException
	 */
	private List<String> getCoauthors(String author) throws SodaHibernateException{
		List<String> coAuthors = new ArrayList<String>();
		this.session.open();
		List<DblpPublication> pubs = WorkArounds.getAuthorPublications(author, this.session);
		
		for(DblpPublication p : pubs){
			for (Person a : p.getAuthors()){
				String name = a.getName();
				if (name!=author && !coAuthors.contains(name))
					coAuthors.add(name);
			}
				
		}
		this.session.close();
		return coAuthors;
	}
	
}
/**
 * This class defines the structure of a node in the search tree
 * 
 * @author Staffiero
 *
 */
class TreeNode{
	String authorName;
	TreeNode father;
	int depth;
	/**
	 * The constructor to be used
	 * @param authorName the author name
	 * @param father a pointer to the father of this node
	 * @param depth the depth of this node
	 */
	public TreeNode(String authorName, TreeNode father, int depth) {
		super();
		this.authorName = authorName;
		this.father = father;
		this.depth = depth;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public TreeNode getFather() {
		return father;
	}
	public void setFather(TreeNode father) {
		this.father = father;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	
}

/**
 * This class defines the fringe used in the search. 
 * Changing the fringe implementation changes the search algorithm.
 * For example if the fringe is a FIFO queue the search algorithm is a width-first, 
 * if the fringe is a LIFO queue the search algorithm is a depth-first 
 * This fringe is implemented as a FIFO queue.
 * 
 * @author Staffiero
 *
 */
//FIFO queue
class Fringe{
	List<TreeNode> content;
	/**
	 * The empty constructor
	 */
	public Fringe(){
		content = new ArrayList<TreeNode>();
	}
	/**
	 * Adds a node to the fringe.
	 * @param element the node to be added.
	 */
	public void add(TreeNode element){
		this.content.add(element);
	}
	
	/**
	 * Adds a set of nodes to the fringe.
	 * @param nodes the set of nodes to be added.
	 */
	public void addAll(List<TreeNode> nodes){
		this.content.addAll(nodes);
	}
	
	/**
	 * Returns a tree node from the fringe. 
	 * The implementation of this method together with the implementation of the add(TreeNode element) method affects the search algorithm.
	 * @return the first available tree node in the fringe.
	 */
	public TreeNode get(){
		TreeNode element=null;
		if (this.content.size()>0){
			return this.content.remove(0);
		
		}
		return element;
	}
	/**
	 * Tests if the fringe is empty
	 * @return true if the fringe is empty, false otherwise
	 */
	public boolean isEmpty(){
		if (this.content.size()==0)
			return true;
		else
			return false;
	}
	/**
	 * Returns the number of elements in the fringe
	 * @return the number of elements in the fringe
	 */
	public int size(){
		return this.content.size();
	}
	
}