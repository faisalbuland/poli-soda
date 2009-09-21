package dblp.social.hibernate;

import java.util.ArrayList;
import java.util.List;

import dblp.social.exceptions.SodaHibernateException;
import dblp.social.hibernate.entities.DblpPublication;
import dblp.social.hibernate.entities.Person;
/**
 * This class provides 3 workarounds to overcome some Hibernate mapping problems.
 * In particular the problem is bounded to the many to many relation between DblpPublication (abstract class) and Person:
 * it works in one way (concrete publication -> person) but not in the other.
 * 
 * @author staffiero
 *
 */
public class WorkArounds {
	/**
	 * Returns a list of DblpPublication for a given author.
	 *  
	 * @param author the author whose publications we want to get.
	 * @param session an ISodaHibernateSession, which can be both open or close, anyway this method will leave it opened.
	 * @return the list of author's publications
	 * @throws SodaHibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<DblpPublication> getAuthorPublications(Person author, ISodaHibernateSession session) throws SodaHibernateException{
		if (!session.isOpen())
			throw new SodaHibernateException("The Hibernate session is closed");
		List<DblpPublication> pubs = new ArrayList<DblpPublication>();
		String query1 = 
			"SELECT publicationId FROM DblpPublication_Authors where personId="+author.getId();
		for (Object o : session.createSQLQuery(query1).list()){
			String pubId = o.toString();
			
			List<DblpPublication> pub = session.getNamedQuery("findPublicationById").setString("publicationId", pubId).list();
			for (DblpPublication p : pub){
				pubs.add(p);
			}
		}
		
		return pubs;
	}
	
	/**
	 * Returns a list of DblpPublication for a given author.
	 * 
	 * @param author the author whose publications are needed.
	 * @param session an ISodaHibernateSession, which can be both open or close, anyway this method will leave it opened.
	 * @return the list of author's publications
	 * @throws SodaHibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<DblpPublication> getAuthorPublications(String author, ISodaHibernateSession session) throws SodaHibernateException{
		if (!session.isOpen())
			throw new SodaHibernateException("The Hibernate session is closed");
		List<Person> authors = session.getNamedQuery("findPersonByName").setString("personName", author).list();
		
		if (authors.size()==0)
			throw new SodaHibernateException("Author not found");
		
		Person a = authors.get(0);
		List<DblpPublication> pubs = new ArrayList<DblpPublication>();
		String query1 = 
			"SELECT publicationId FROM DblpPublication_Authors where personId="+a.getId();
		for (Object o : session.createSQLQuery(query1).list()){
			String pubId = o.toString();
			
			List<DblpPublication> pub = session.getNamedQuery("findPublicationById").setString("publicationId", pubId).list();
			for (DblpPublication p : pub){
				pubs.add(p);
			}
		}
		
		return pubs;
	}
	/**
	 * Returns a list of Articles for a given Journal.
	 * 
	 * @param journalId the journal whose articles are needed.
	 * @param session n ISodaHibernateSession, which can be both open or close, anyway this method will leave it opened.
	 * @return
	 * @throws SodaHibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<DblpPublication> getJournalArticles(String journalId, ISodaHibernateSession session) throws SodaHibernateException{
		if (!session.isOpen())
			throw new SodaHibernateException("The Hibernate session is closed");
		
		List<DblpPublication> pubs = new ArrayList<DblpPublication>();
		String query = 
			"SELECT articleId FROM Journal_Articles where journalId='"+journalId+"'";
		

		
		for (Object o : session.createSQLQuery(query).list()){
			String pubId = o.toString();
			
			List<DblpPublication> pub = session.getNamedQuery("findPublicationById").setString("publicationId", pubId).list();
			for (DblpPublication p : pub){
				pubs.add(p);
			}
		}
		
		return pubs;
	}
}
