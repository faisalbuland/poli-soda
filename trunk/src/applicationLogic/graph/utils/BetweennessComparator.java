package applicationLogic.graph.utils;

import java.util.Comparator;

import applicationLogic.statistics.Author;

/**
 * Comparator used to sort an array of Author according to their betweenness centrality score.
 * 
 * @author Staffiero
 *
 */

public class BetweennessComparator implements Comparator<Author> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Author a1, Author a2) {
		if (a1!=null && a2!=null){
			
			if (a1.getBetweennessCentrality()==a2.getBetweennessCentrality())
				return 0;
			else if (a1.getBetweennessCentrality()< a2.getBetweennessCentrality())
				return-1;
			else
				return 1;
		}
		else if (a2==null)
			return 1;
		return 0;
	}
	
}
