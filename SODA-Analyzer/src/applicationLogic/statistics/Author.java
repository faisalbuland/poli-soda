package applicationLogic.statistics;
/**
 * This class is a container of some author's properties extracted from a co-authorship graph.
 * 
 * @author Staffiero
 *
 */
public class Author {
	private String name;
	private double clusteringCoefficient;
	private int degree;
	private double betweennessCentrality;
	private double closenessCentrality;
	private double eigenvectorCentrality;
//	private Collection<Author> coAuthors;
	private int publicationsNumber;
	
	public Author(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	

	public void setName(String name) {
		this.name = name;
	}

	public double getClusteringCoefficient() {
		return clusteringCoefficient;
	}

	public void setClusteringCoefficient(double clusteringCoefficient) {
		this.clusteringCoefficient = clusteringCoefficient;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public double getBetweennessCentrality() {
		return betweennessCentrality;
	}

	public void setBetweennessCentrality(double betweennessCentrality) {
		this.betweennessCentrality = betweennessCentrality;
	}

	public double getClosenessCentrality() {
		return closenessCentrality;
	}

	public void setClosenessCentrality(double closenessCentrality) {
		this.closenessCentrality = closenessCentrality;
	}

	public double getEigenvectorCentrality() {
		return eigenvectorCentrality;
	}

	public void setEigenvectorCentrality(double eigenvectorCentrality) {
		this.eigenvectorCentrality = eigenvectorCentrality;
	}

	public int getPublicationsNumber() {
		return publicationsNumber;
	}

	public void setPublicationsNumber(int publicationsNumber) {
		this.publicationsNumber = publicationsNumber;
	}

	
}
