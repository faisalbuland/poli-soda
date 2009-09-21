package applicationLogic.graph.utils;


import java.util.Comparator;

import applicationLogic.statistics.Author;

/**
 * Comparator used to sort an array of Author according to their clustering coefficient score.
 * 
 * @author Staffiero
 *
 */


public class ClusteringComparator implements Comparator<Author>{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Author a1, Author a2) {
		if (a1!=null && a2!=null){
			
			if (a1.getClusteringCoefficient()==a2.getClusteringCoefficient())
				return 0;
			else if (a1.getClusteringCoefficient()< a2.getClusteringCoefficient())
				return-1;
			else
				return 1;
		}
		else if (a2==null)
			return 1;
		return 0;
	}
	
}