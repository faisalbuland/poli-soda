package dblp.social.hibernate.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * Affiliation
 * 
 * @author tamburrelli
 * 
 */
@Entity
@NamedQuery(name = "findAffiliationByName", 
		query = "from Affiliation a where a.name = :affiliationName")
public class Affiliation {

	private String name;
	
	private Long id;
	
	private String website;
	
	private String address;
	
	private Nation nation;
	
	private List<Person> persons = new ArrayList<Person>();
	
	/**
	* @return Returns the id.
	*/
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "affiliationId")
	public Long getId() {
		return id;
	}
	
	/**
	* @param id The id to set.
	*/
	public void setId(Long id) {
		this.id = id;
	}

			
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Affiliation(){
		super();
	}
	
	public Affiliation(String name){
		this.name = name;
	}
	
	@ManyToOne
	@JoinTable(name = "Nation_Affiliations",
			joinColumns = { @JoinColumn(name = "affiliationId")},
			inverseJoinColumns = { @JoinColumn(name = "nationId") })
	public Nation getNation() {
		return nation;
	}
	
	@OneToMany(mappedBy="affiliation",
			cascade=CascadeType.REFRESH,
			targetEntity = Person.class)
	public List<Person> getPersons() {
		return persons;
	}

	public void setNation(Nation nation) {
		this.nation = nation;
	}

	public void setPersons(List<Person> persons) {
		this.persons = persons;
	}
	

}
