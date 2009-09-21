package dblp.social.hibernate.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;



/**
 * A DBLP InProceedings (conference paper)
 * 
 * @author ghezzi
 * 
 */
@Entity
@NamedQuery(name = "findInProceedingsById", 
		query = "from InProceedings i where i.id = :inProceedingsId")
public class InProceedings extends DblpPublication{

	/**
	 * The article's proceedings.
	 */
	private Proceedings proceedings;
	private String booktitle = "";
	
	public InProceedings(){
		super();
	}
	
	public InProceedings(String title){
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
	@JoinTable(name = "Proceedings_InProceedings", 
			joinColumns = { @JoinColumn(name = "inproceedingsId") }, 
			inverseJoinColumns = { @JoinColumn(name = "proceedingsId") })
	public Proceedings getProceedings() {
		return proceedings;
	}

	public void setProceedings(Proceedings proceedings) {
		this.proceedings = proceedings;
	}
}
