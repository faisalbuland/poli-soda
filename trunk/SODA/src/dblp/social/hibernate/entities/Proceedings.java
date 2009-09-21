package dblp.social.hibernate.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

/**
 * A DBLP Proceedings.
 * 
 * @author ghezzi
 * 
 */
@Entity
@NamedQuery(name = "findProceedingsById", 
		query = "from Proceedings p where p.id = :proceedingsId")
public class Proceedings {

	/**
	* Unique ID, used by Hibernate.
	*/
	private String id;

	/**
	 * Name of the proceedings.
	 */
	private String title = "";
	/**
	 * Year of the proceedings
	 */
	private String year = "";
	/**
	 * ISBN of the proceedings
	 */
	private String isbn = "";
	/**
	 * Editors of the proceedings
	 */
	private List<Person> editors = new ArrayList<Person>();
	/**
	 * Proceedings articles
	 */
	private List<InProceedings> inProceedings = new ArrayList<InProceedings>();
	/**
	 * Publisher of the proceedings
	 */
	private String publisher = "";
	private String dblpKey = "";
	
	public Proceedings() { super(); }

	public Proceedings(String name) {
		this();
		this.title = name;
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
	@JoinTable(name = "Proceedings_Editors", 
			joinColumns = { @JoinColumn(name = "proceedingsId") }, 
			inverseJoinColumns = { @JoinColumn(name = "personId") })
	public List<Person> getEditors() {
		return editors;
	}

	public void setEditors(List<Person> editors) {
		this.editors = editors;
	}

	public void addEditor(Person editor){
		this.editors.add(editor);
	}
	
	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	
	public void addInProceedings(InProceedings inProceeding){
		this.inProceedings.add(inProceeding);
	}
	
	@OneToMany(cascade=CascadeType.REFRESH)
	@JoinTable(name = "Proceedings_InProceedings", 
			joinColumns = { @JoinColumn(name = "proceedingsId") }, 
			inverseJoinColumns = { @JoinColumn(name = "inproceedingsId" , unique = true) })
	public List<InProceedings> getInProceedings() {
		return inProceedings;
	}

	public void setInProceedings(List<InProceedings> inProceedings) {
		this.inProceedings = inProceedings;
	}
	
	/**
	* @return Returns the id.
	*/
	@Id
	@Column(name = "proceedingsId")
	public String getId() {
		return this.id;
	}

	/**
	* @param id The id to set.
	*/
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the proceedings title.
	 */
	@Column(length = 500)
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title the title to set.
	 */
	public void setTitle(String name) {
		this.title = name;
	}
}
