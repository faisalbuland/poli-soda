package dblp.social.hibernate.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.hibernate.validator.Length;

/**
 * A Journal. It does not exist expilcitly in the original dblp model.
 * But it much easier and useful for us to make it explicit.
 * 
 * @author ghezzi
 * 
 */
@Entity
@NamedQuery(name = "findJournalById", 
		query = "from Journal j where j.id = :journalId")
public class Journal {

	/**
	* Unique ID, used by Hibernate.
	*/
	private String id;

	/**
	 * Title of the journal.
	 */
	@Length(max=600)
	private String title = "";
	private String year = "";
	private String volume = "";
	private String number = "";
	private String isbn = "";
	private List<DblpPublication> articles = new ArrayList<DblpPublication>();
	private String publisher = "";
	private String dblpKey = "";

	public Journal() { super(); }

	public Journal(String title) {
		this();
		this.title = title;
	}

	@OneToMany(cascade=CascadeType.REFRESH)
	@JoinTable(name = "Journal_Articles",
			joinColumns = { @JoinColumn(name = "journalId")},
			inverseJoinColumns = { @JoinColumn(name = "articleId", unique = true)})
	public List<DblpPublication> getArticles() {
		return articles;
	}

	public void setArticles(List<DblpPublication> articles) {
		this.articles = articles;
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

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void addArticle(DblpPublication article){
		this.articles.add(article);
	}
	
	/**
	* @return Returns the id.
	*/
	@Id
	@Column(name = "journalId")
	public String getId() {
	return this.id;
	}

	/**
	* @param id The id to set.
	*/
	public void setId(String id) {
	this.id = id;
	}

}
