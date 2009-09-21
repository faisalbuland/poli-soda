package applicationLogic.statistics;
/**
 * This class is a container of some conference or journal general statistics.
 * The year field is used to store the year for a conference or the volume number for a transaction.
 * 
 * @author Staffiero
 *
 */
import java.util.List;

public class PublicationsStatistics {
	private int year;
	private int publicationsNumber;
	/**
	 * The bar char Authors-Publications. Provides information on the number of publications (value) authored by a given authors number (index) 
	 */
	private List<Integer> authorsPublications;
	/**
	 * The bar char Publications-Authors. Provides information on the number of authors (value) who authored a given number of publications(index) 
	 */
	private List<Integer> publicationsAuthors;
	/**
	 * The productivity rate for the given year, i.e. the average paper per author, considering all previous authors.
	 */
	private double productivity;
	
	public PublicationsStatistics(int year){
		super();
		this.year = year;
	}
	
	public PublicationsStatistics(int year, int publicationsNumber) {
		super();
		this.year = year;
		this.publicationsNumber = publicationsNumber;
	}
	
	
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getPublicationsNumber() {
		return publicationsNumber;
	}
	public void setPublicationsNumber(int publicationsNumber) {
		this.publicationsNumber = publicationsNumber;
	}

	public List<Integer> getAuthorsPublications() {
		return authorsPublications;
	}

	public void setAuthorsPublications(List<Integer> authorsPublications) {
		this.authorsPublications = authorsPublications;
	}

	public List<Integer> getPublicationsAuthors() {
		return publicationsAuthors;
	}

	public void setPublicationsAuthors(List<Integer> publicationsAuthors) {
		this.publicationsAuthors = publicationsAuthors;
	}

	public double getProductivity() {
		return productivity;
	}

	public void setProductivity(double productivity) {
		this.productivity = productivity;
	}
	
	
	
}
