package dblp.social.hibernate.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;



/**
 * A DBLP Article
 * 
 * @author ghezzi
 * 
 */
@Entity
@NamedQuery(name = "findArticleById", 
		query = "from Article a where a.id = :articleId")
public class Article extends DblpPublication{

	private Journal journal;
	
	public Article(){
		super();
	}
	
	public Article(String title){
		super(title);
	}
	
	public void setJournal(Journal journal) {
		this.journal = journal;
	}

	@ManyToOne
	@JoinTable(name = "Journal_Articles",
			joinColumns = { @JoinColumn(name = "articleId")},
			inverseJoinColumns = { @JoinColumn(name = "journalId") })
	public Journal getJournal() {
		return journal;
	}
}
