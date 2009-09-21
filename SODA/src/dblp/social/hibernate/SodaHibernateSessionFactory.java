package dblp.social.hibernate;

import org.apache.log4j.Category;

import dblp.social.exceptions.SodaHibernateException;


@SuppressWarnings("deprecation")
public class SodaHibernateSessionFactory {

	//For debugging
	static Category logger = Category.getInstance(SodaHibernateSessionFactory.class.getName());
	
	/**
	 * Returns an implementation of {@link ISodaHibernateSession} configured with the passed params.
	 * 
	 * @param dbUrl database host (e.g. mysql://localhost:3306/evolizer_test)
	 * @param dbDialect database dialect (e.g. org.hibernate.dialect.MySQLDialect)
	 * @param dbDriverName jdbc-compliant database driver (e.g. com.mysql.jdbc.Driver)
	 * @param dbUser database username
	 * @param dbPasswd database password for dbUser
	 *
	 * @return an implementation of ICatalogSession
	 * @throws SodaHibernateException 
	 * @author ghezzi
	 */
	public static ISodaHibernateSession getSession(String dbUrl, String dbDialect, String dbDriverName, String dbUser,	String dbPasswd, SodaModelProvider modelProvider) throws SodaHibernateException{
		try{
			return new SodaHibernateSessionImpl(dbUrl,
					dbDialect,
					dbDriverName,
					dbUser,
					dbPasswd,
					modelProvider);
		} catch (Exception e) {
			throw new SodaHibernateException(e);
		}
	}
}
