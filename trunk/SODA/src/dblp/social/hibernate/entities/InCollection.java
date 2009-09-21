package dblp.social.hibernate.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;





/**
 * A DBLP Book chapter, which for now we call it with the original name.
 * 
 * @author ghezzi
 * 
 */
@Entity
@NamedQuery(name = "findInCollectionById", 
		query = "from InCollection i where i.id = :inCollectionId")
public class InCollection extends DblpPublication{

	/**
	 * The chapter's book
	 */
	private Book book;
	private String booktitle="";
	public InCollection(){
		super();
	}
	
	public InCollection(String title){
		super(title);
	}
	/**
	 * @return the booktitle
	 */
	public String getBooktitle() {
		return booktitle;
	}

	/**
	 * @param booktitle the booktitle to set
	 */
	public void setBooktitle(String booktitle) {
		this.booktitle = booktitle;
	}
	@ManyToOne
	@JoinTable(name = "Book_Chapters", 
			joinColumns = { @JoinColumn(name = "chapterId") }, 
			inverseJoinColumns = { @JoinColumn(name = "bookId") })
	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}
	
}
