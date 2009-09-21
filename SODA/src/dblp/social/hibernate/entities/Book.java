package dblp.social.hibernate.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;

import org.hibernate.validator.Length;


/**
 * A DBLP Book.
 * 
 * @author ghezzi
 * 
 */

@Entity
@NamedQuery(name = "findBookById", 
		query = "from Book b where b.id = :bookId")
public class Book {

	/**
	* Unique ID, used by Hibernate.
	*/
	private String id;
	/**
	 * Book title.
	 */
	@Length(max=600)
	private String title = "";
	@Length(max=600)
	private String booktitle ="";
	/**
	 * Book ISBN.
	 */
	private String isbn = "";
	/**
	 * Book publisher
	 */
	private String publisher ="";
	/**
	 * Book year.
	 */
	private String year = "";
	/**
	 * Book editors
	 */
	private List<Person> editors = new ArrayList<Person>();
	/**
	 * Book authors, as it can have them instead of of editors
	 */
	private List<Person> authors = new ArrayList<Person>();
	/**
	 * Book chapter, but for now we call the InCollection keeping the dblp terms (TODO change it).
	 */
	private List<InCollection> chapters = new ArrayList<InCollection>();
	
	private String dblpKey = "";
	
	public Book() {
		super();
		this.editors = new ArrayList<Person>();
	}

	public Book(String title) {
		this();
		this.title = title;
		this.editors = new ArrayList<Person>();
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

	/**
	 * @return the authors
	 */
	@ManyToMany
	@JoinTable(name = "Book_Authors", 
			joinColumns = { @JoinColumn(name = "bookId") }, 
			inverseJoinColumns = { @JoinColumn(name = "personId") })
	public List<Person> getAuthors() {
		return authors;
	}

	/**
	 * @param authors the authors to set
	 */
	public void setAuthors(List<Person> authors) {
		this.authors = authors;
	}

	public void addAuthor(Person author){
		this.authors.add(author);
	}
	public void addEditor(Person editor){
		this.editors.add(editor);
	}
	
	public String getDblpKey() {
		return dblpKey;
	}

	public void setDblpKey(String dblpKey) {
		this.dblpKey = dblpKey;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	@ManyToMany
	@JoinTable(name = "Book_Editors", 
			joinColumns = { @JoinColumn(name = "bookId") }, 
			inverseJoinColumns = { @JoinColumn(name = "personId") })
	public List<Person> getEditors() {
		return editors;
	}

	public void setEditors(List<Person> editors) {
		this.editors = editors;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
	
	@Column(length = 500)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public void addChapter(InCollection chapter){
		this.chapters.add(chapter);
	}
	
	/**
	* @return Returns the id.
	*/
	@Id
	@Column(name = "bookId")
	public String getId() {
		return this.id;
	}

	/**
	* @param id The id to set.
	*/
	public void setId(String id) {
	this.id = id;
	}

	@OneToMany(cascade=CascadeType.ALL)
	@JoinTable(name = "Book_Chapters", 
			joinColumns = { @JoinColumn(name = "bookId") }, 
			inverseJoinColumns = { @JoinColumn(name = "chapterId" , unique = true) })
	public List<InCollection> getChapters() {
		return chapters;
	}

	public void setChapters(List<InCollection> chapters) {
		this.chapters = chapters;
	}
	
}
