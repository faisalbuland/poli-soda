package dblp.social.hibernate.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NamedQuery;

/**
 * A DBLP Author.
 * 
 * @author tamburrelli
 * 
 */

@Entity
@Table(name="Person")
@NamedQuery(name = "findPersonByName", 
		query = "from Person p where p.name= :personName")
public class Person {

	/**
	* Unique ID, used by Hibernate.
	*/
	private Long id;
	/**
	 * The author name.
	 */
	private String name="";
	
	private Nation nation;
	
	private Affiliation affiliation;
	
	private List<Person> advisors;
	
	private List<Person> advised;
	
	private List<DblpPublication> publications;

	@ManyToOne
	@JoinTable(name = "Nation_Persons",
			joinColumns = { @JoinColumn(name = "personId", unique = true)},
			inverseJoinColumns = { @JoinColumn(name = "nationId") })
	public Nation getNation() {
		return nation;
	}

	@ManyToMany(cascade=CascadeType.REFRESH,
			targetEntity=Person.class,
			mappedBy="advisors")
	public List<Person> getAdvised() {
		return advised;
	}
	
	

	@ManyToMany(cascade=CascadeType.REFRESH)
	@JoinTable(name = "Advisors_Advised",
			joinColumns = { @JoinColumn(name = "advised")},
			inverseJoinColumns = { @JoinColumn(name = "advisor")})
	public List<Person> getAdvisors() {
		return advisors;
	}
	
	public void setNation(Nation nation) {
		this.nation = nation;
	}

	public void setAffiliation(Affiliation affiliation) {
		this.affiliation = affiliation;
	}

	public void setAdvisors(List<Person> advisors) {
		this.advisors = advisors;
	}

	public void setAdvised(List<Person> advised) {
		this.advised = advised;
	}
	
	
	
	@ManyToOne
	@JoinTable(name = "Affiliation_Persons",
			joinColumns = { @JoinColumn(name = "personId")},
			inverseJoinColumns = { @JoinColumn(name = "affiliationId") })
	public Affiliation getAffiliation() {
		return affiliation;
	}
	
	public Person(){}
	
	public Person(String name){
		this.name = name;
	}
	@Column(unique=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	* @return Returns the id.
	*/
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "personId")
	public Long getId() {
		return this.id;
	}

	/**
	* @param id The id to set.
	*/
	@SuppressWarnings(value = { "unused" })
	private void setId(Long id) {
		this.id = id;
	}
	
	

	/**
	 * @return the InCollections
	 */
	@ManyToMany(mappedBy = "authors",  
			targetEntity = DblpPublication.class)
//	@org.hibernate.annotations.LazyCollection(org.hibernate.annotations.LazyCollectionOption.FALSE)
	public List<DblpPublication> getPublications() {
		return publications;
	}

	/**
	 * @param publications the publications to set
	 */
	public void setPublications(List<DblpPublication> publications) {
		this.publications = publications;
	}
	

//	public void addPublication(DblpPublication publication){
//		this.publications.add(publication);
//	}
}
