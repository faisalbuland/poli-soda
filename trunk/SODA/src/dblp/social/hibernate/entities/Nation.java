package dblp.social.hibernate.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * Affiliation
 * 
 * @author tamburrelli
 * 
 */
@Entity
@NamedQuery(name = "findNationByName", 
		query = "from Nation n where n.name = :nationName")
public class Nation {

	private Long id;
	
	private String name;
	
	public void setAffiliations(List<Affiliation> affiliations) {
		this.affiliations = affiliations;
	}

	public void setPersons(List<Person> persons) {
		this.persons = persons;
	}

	private List<Affiliation> affiliations = new ArrayList<Affiliation>();
	
	private List<Person> persons = new ArrayList<Person>();
	
	
	/**
	* @return Returns the id.
	*/
	@Id
	@Column(name = "nationId")
	@GeneratedValue(strategy = GenerationType.AUTO)
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

	
	
	@OneToMany(mappedBy="nation", 
			cascade=CascadeType.REFRESH,
			targetEntity=Affiliation.class)
	public List<Affiliation> getAffiliations() {
		return affiliations;
	}
	
	@OneToMany(mappedBy="nation", 
			cascade=CascadeType.REFRESH, 
			targetEntity=Person.class)
	public List<Person> getPersons() {
		return persons;
	}

}
