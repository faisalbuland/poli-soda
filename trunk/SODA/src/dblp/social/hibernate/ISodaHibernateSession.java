package dblp.social.hibernate;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;

import dblp.social.exceptions.SodaHibernateException;


/**
 * Persistence providers have to provide an implementation
 * of this interface. Like this, we provide a common interface for future persistence
 * solutions such as ontologies or other o/r-mappers.
 * 
 * @author ghezzi
 *
 */
public interface ISodaHibernateSession {

	/**
	 * Opens the session.
	 * 
	 * @throws SodaHibernateException if session is already open.
	 */
	public abstract void open() throws SodaHibernateException;

	/**
	 * Checks whether the session is open or not.
	 *
	 * @return true, if session is open. false, otherwhise.
	 */
	public abstract boolean isOpen();

	/**
	 * Closes the session after making pending changes persistent.
	 * 
	 * @Obligation Has to invoke flush before closing the session to ensure
	 * that now exception/dataloss occurs.
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract void close() throws SodaHibernateException;

	/**
	 * Flushes the session.
	 * 
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract void flush() throws SodaHibernateException;

	/**
	 * Clears the session.
	 * 
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract void clear() throws SodaHibernateException;

	/**
	 * Saves the object.
	 * 
	 * @param saveableObject an instance of a Hibernate/ejb3-annotated class.
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract void saveObject(Object saveableObject) throws SodaHibernateException;
	
	/**
	 * Saves or updates the object.
	 * 
	 * @param saveableObject an instance of a Hibernate/ejb3-annotated class.
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract void saveOrUpdate(Object object) throws SodaHibernateException;
	
	/**
	 * Deletes the object.
	 * 
	 * @param object an instance of a Hibernate/ejb3-annotated class.
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract void delete(Object object) throws SodaHibernateException;

	/**
	 * Executes a hql query and returns the results.
	 * 
	 * @param hqlQuery the query string.
	 * @return a list of objects that match the query
	 * @throws SodaHibernateException if session is not open
	 * @deprecated use {@link #query(String, Class)} instead
	 */
	@SuppressWarnings("unchecked")
	public abstract List query(String hqlQuery) throws SodaHibernateException;
	
	/**
	 * Generic method. Executes a hql query and returns the results.
	 * 
	 * @param hqlQuery the query string.
	 * @param <T> the parameterized type of the returned {@link List}
	 * @return a list of objects of the type <code>T</code> that match the query
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract <T> List<T> query(String hqlQuery, Class<T> type) throws SodaHibernateException;

	/**
	 * Loads an object by its class and id.
	 * 
	 * @param clazz the class
	 * @param id the id
	 * 
	 * @return the object.
	 * @throws SodaHibernateException if session is not open.
	 */
	public <T>T load(Class<T> clazz, Long id) throws SodaHibernateException;
	
	/**
	 * Starts a transaction. Only one transaction per session can be active at any
	 * given time.
	 * 
	 * @throws SodaHibernateException if session is not open or transaction already active.
	 * @Obligation Invokers eventually have to call {@link #endTransaction()}
	 */
	public abstract void startTransaction() throws SodaHibernateException;

	/**
	 * Commits the transaction. 
	 * 
	 * @throws SodaHibernateException if session is not open or no transaction is active
	 */
	public abstract void endTransaction() throws SodaHibernateException;
	
	/**
	 * Rollbacks the transaction. 
	 * 
	 * @throws SodaHibernateException if session is not open or no transaction is active
	 */
	public abstract void rollbackTransaction() throws SodaHibernateException;
	
	/**
	 * Creates the database schema based on the o/r mappings (e.g. the Hibernate/ejb3-annotations).
	 * Can only be executed when session is closed.
	 *
	 * @throws SodaHibernateException if session IS active.
	 */
	public abstract void createSchema() throws SodaHibernateException;

	/**
	 * Updates a database schema based on the o/r mappings (e.g. the Hibernate/ejb3-annotations).
	 * Can only be executed when session is closed.
	 * 
	 * @throws SodaHibernateException if session IS active.
	 */
	public abstract void updateSchema() throws SodaHibernateException;

	/**
	 * Drops the database schema.
	 * Can only be executed when session is closed.
	 *
	 * @throws SodaHibernateException if session IS active.
	 */
	public abstract void dropSchema() throws SodaHibernateException;

	/**
	 * Convenience method that can be used whenever a hql-query is intended to
	 * return only one result.
	 * 
	 * @param hqlQuery the query string
	 * @return the result
	 * @throws SodaHibernateException if session is not open or more than one result was found.
	 * @deprecated Use {@link #uniqueResult(String, Class)} instead.
	 */
	public abstract Object uniqueResult(String hqlQuery) throws SodaHibernateException;
	
	/**
	 * Generic convenience method that can be used whenever a hql-query is intended to
	 * return only one result.
	 * 
	 * @param <T> The type of the result
	 * @param hqlQuery
	 * @throws SodaHibernateException if session is not open or more than one result was found.
	 * @return the result
	 * @throws SodaHibernateException 
	 */
	public abstract <T>T uniqueResult(String hqlQuery, Class<T> type) throws SodaHibernateException;

	/**
	 * Updates the object.
	 * 
	 * @param saveableObject an instance of a Hibernate/ejb3-annotated class.
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract void update(Object object) throws SodaHibernateException;
	
	/**
	 * Merges the object. Useful during batch-processing where regular flushing/clearing is necessary due to memory restrictions.
	 * If e.g. {@link #saveOrUpdate(Object)} is invoked instead (at least while using Hibernate), we often experience exceptions.
	 * 
	 * @param saveableObject an instance of a Hibernate/ejb3-annotated class.
	 * @throws SodaHibernateException if session is not open.
	 */
	public abstract Object merge(Object object) throws SodaHibernateException;
	
	
	public abstract <T>T get(Class<T> clazz, Serializable id) throws SodaHibernateException;
	
	public Query getNamedQuery(String queryName);
	
	public void refresh(Object object);
	
	public void disconnect();
	
	public Query createSQLQuery(String query);
}