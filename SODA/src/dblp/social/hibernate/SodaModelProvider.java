package dblp.social.hibernate;

import dblp.social.hibernate.entities.Affiliation;
import dblp.social.hibernate.entities.Article;
import dblp.social.hibernate.entities.Book;
import dblp.social.hibernate.entities.DblpPublication;
import dblp.social.hibernate.entities.InCollection;
import dblp.social.hibernate.entities.InProceedings;
import dblp.social.hibernate.entities.Journal;
import dblp.social.hibernate.entities.Nation;
import dblp.social.hibernate.entities.Person;
import dblp.social.hibernate.entities.Proceedings;


public class SodaModelProvider{

	@SuppressWarnings("unchecked")
	public Class[] getAnnotatedClasses() {
		Class[] annotatedClasses = {
				Article.class,
				Book.class,
				DblpPublication.class,
				InCollection.class,
				InProceedings.class,
				Journal.class,
				Person.class,
				Proceedings.class,
				Affiliation.class,
				Nation.class};
		
		return annotatedClasses;
	}

}
