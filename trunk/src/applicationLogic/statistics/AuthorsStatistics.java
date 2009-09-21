package applicationLogic.statistics;
/**
 * This class is a container of some conference or journal statistics.
 * The year field is used to store the year for a conference or the volume number for a transaction.
 * For each year / volume this structure stores some statistics on the authors numbers
 * 
 * @author Staffiero
 *
 */

public class AuthorsStatistics {
	private int year;
	private int authorsNumber;
	private int newAuthorsNumber;
	private int totalAuthors; //until the current year
	
	
	public AuthorsStatistics(int year, int authorsNumber, int newAuthorsNumber,
			int totalAuthors) {
		super();
		this.year = year;
		this.authorsNumber = authorsNumber;
		this.newAuthorsNumber = newAuthorsNumber;
		this.totalAuthors = totalAuthors;
	}


	public int getYear() {
		return year;
	}


	public void setYear(int year) {
		this.year = year;
	}


	public int getAuthorsNumber() {
		return authorsNumber;
	}


	public void setAuthorsNumber(int authorsNumber) {
		this.authorsNumber = authorsNumber;
	}


	public int getNewAuthorsNumber() {
		return newAuthorsNumber;
	}


	public void setNewAuthorsNumber(int newAuthorsNumber) {
		this.newAuthorsNumber = newAuthorsNumber;
	}


	public int getTotalAuthors() {
		return totalAuthors;
	}


	public void setTotalAuthors(int totalAuthors) {
		this.totalAuthors = totalAuthors;
	}
	
	
	
	
}
