package dblp.social.hibernate;

import java.io.Serializable;
import java.util.List;

import javax.persistence.NonUniqueResultException;

import org.apache.log4j.Category;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;


import dblp.social.exceptions.SodaHibernateException;

/**
 * This class wraps a Hibernate session and provides behaviour for making objects
 * of Hibernate/ejb3-annotated classes persistent.
 * 
 * @author ghezzi
 *
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class SodaHibernateSessionImpl implements ISodaHibernateSession {
	static Category logger = Category.getInstance(SodaHibernateSessionImpl.class.getName());

	private Class[] annotatedClasses;
	/**
	 * The Hibernate session.
	 */
	private Session fHibernateSession = null;
	/**
	 * The configuration of the Hibernate session.
	 */
	private AnnotationConfiguration fHibernateAnnotationConfig = null;
	/**
	 * Whenever a transaction is open, its reference is stored here.
	 */
	private Transaction fTransaction = null;
	
	/**
	 * true, if the session is open
	 * false, otherwhise
	 */
	private boolean fIsOpen = false;

	/**
	 * Constructor. Not intended to be called by clients directly. Use {@link EvolizerSessionFactory#getCatalogSession(String, String, String, String, String)} instead.
	 * 
	 * @param dbUrl database host (e.g. mysql://localhost:3306/evolizer_test)
	 * @param dbDialect database dialect (e.g. org.hibernate.dialect.MySQLDialect)
	 * @param dbDriverName jdbc-compliant database driver (e.g. com.mysql.jdbc.Driver)
	 * @param dbUser database username
	 * @param dbPasswd database password for dbUser
	 */
	public SodaHibernateSessionImpl(String dbUrl,
						   String dbDialect,
						   String dbDriverName,
						   String dbUser,
						   String dbPasswd,
						   SodaModelProvider modelProvider) {
		this.annotatedClasses = modelProvider.getAnnotatedClasses();
		configureDataBaseConnection(dbUrl,
									dbDialect,
									dbDriverName,
									dbUser,
									dbPasswd);
	}

	private void assertSessionIsClosed() throws SodaHibernateException {
		if(fIsOpen){
			SodaHibernateException ex =  new SodaHibernateException("Session is already active.");
			logger.error("Session is already active.", ex);
			throw ex;
		}
	}

	private void assertSessionIsClosed(String message) throws SodaHibernateException {
		if(fIsOpen){
			SodaHibernateException ex =  new SodaHibernateException(message);
			logger.error(message, ex);
			throw ex;
		}
	}
	
	private void assertSessionIsOpen() throws SodaHibernateException {
		if(!fIsOpen){ 
			SodaHibernateException ex =  new SodaHibernateException("Session is not open.");
			logger.error("Session is not open.", ex);
			throw ex;
		}
	}

	private void assertTransactionIsNotActive() throws SodaHibernateException{
		if(fTransaction != null){
			SodaHibernateException ex =  new SodaHibernateException("A Transaction is already active.");
			logger.error("A Transaction is already active.", ex);
			throw ex;
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#clear()
	 */
	public void clear() throws SodaHibernateException {
		assertSessionIsOpen();
		
		fHibernateSession.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#close()
	 */
	public void close() throws SodaHibernateException{
		logger.debug("Closing session.");
		assertSessionIsOpen();
		
		flush();
		fHibernateSession.clear();
		fHibernateSession.close();
		fHibernateSession = null;
		fIsOpen = false;
		
		logger.debug("Session is now close.");
	}

	/**
		 * Creates and stores the configuration for the Hibernate-session based
		 * on the passed parameters. Furthermore, it queries all <code>org.evolizer.hibernate.modelProvider</code>
		 * extensions for annotated classes.
		 * 
		 * @param dbUrl database host (e.g. mysql://localhost:3306/evolizer_test)
		 * @param dbDialect database dialect (e.g. org.hibernate.dialect.MySQLDialect)
		 * @param dbDriverName jdbc-compliant database driver (e.g. com.mysql.jdbc.Driver)
		 * @param dbUser database username
		 * @param dbPasswd database password for dbUser
		 */
		private void configureDataBaseConnection(String dbUrl,
												 String dbDialect,
												 String dbDriverName,
												 String dbUser,
												 String dbPasswd) {

			
			fHibernateAnnotationConfig = new AnnotationConfiguration();
	
			fHibernateAnnotationConfig.setProperty("hibernate.connection.url", "jdbc:" + dbUrl);
			fHibernateAnnotationConfig.setProperty("hibernate.connection.username", dbUser);
			fHibernateAnnotationConfig.setProperty("hibernate.connection.password", dbPasswd);
			fHibernateAnnotationConfig.setProperty("hibernate.dialect", dbDialect);
			fHibernateAnnotationConfig.setProperty("hibernate.connection.driver_class", dbDriverName);
	
			fHibernateAnnotationConfig.setProperty("hibernate.jdbc.batch_size", "25");
			fHibernateAnnotationConfig.setProperty("hibernate.cache.use_second_level_cache", "false");
			
	//		fHibernateAnnotationConfig.setProperty("hibernate.show_sql", "true");
			
			//Here I add the specific annotated classes
			for (Class annotatedClass : annotatedClasses) {
				fHibernateAnnotationConfig.addAnnotatedClass(annotatedClass);
			}

		}
	
	
		
	/*
	 * @see org.evolizer.hibernate.session.IEvolizerSession#createSchema()
	 */
	public void createSchema() throws SodaHibernateException{
		assertSessionIsClosed("Could not create schema because session is open.");

		SchemaExport exporter = new SchemaExport(fHibernateAnnotationConfig);
		exporter.create(false, true);
	}
	
	public Query createSQLQuery(String query){
		return fHibernateSession.createSQLQuery(query);
	}
	
	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.api.IEvolizerSession#delete(java.lang.Object)
	 */
	public void delete(Object object) throws SodaHibernateException{
		assertSessionIsOpen();
		
		fHibernateSession.delete(object);
	}
	
	public void disconnect(){
		fHibernateSession.disconnect();
	}
	
	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#dropSchema()
	 */
	public void dropSchema() throws SodaHibernateException{
		assertSessionIsClosed("Could not drop schema because session is open.");
		
		SchemaExport exporter = new SchemaExport(fHibernateAnnotationConfig);
		exporter.drop(false, true);
	}

	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#endTransaction()
	 */
	public void endTransaction() throws SodaHibernateException {
		assertSessionIsOpen();
		ensureTransactionIsActive();
		
		try{
			this.fTransaction.commit();
		}
		catch (Exception e){
			this.fTransaction.rollback();
			throw new SodaHibernateException(e);
		}
		finally{
			this.fTransaction = null;
		}
	}
	
	
	private void ensureTransactionIsActive() throws SodaHibernateException{
		if(fTransaction == null){
			SodaHibernateException ex =  new SodaHibernateException("No Transaction is active.");
//			logger.error("No Transaction is active.", ex);
			
			throw ex;
		}	
	}

	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#flush()
	 */
	public void flush() throws SodaHibernateException {
		logger.debug("Flushing the session");
		fHibernateSession.flush();
		logger.debug("Session flushed");
	}

	public <T>T get(Class<T> clazz, Serializable id) throws SodaHibernateException {
		assertSessionIsOpen();
		
		return (T)fHibernateSession.get(clazz, id);
	}

	/**
	 * Not recommended. Use this only when dealing with special issues like e.g. criterias is absolutely necessary..
	 * 
	 * @return the wrapped Hibernate-Session
	 */
	protected Session getHibernateSession(){
		return fHibernateSession;
	}
	
	
	public Query getNamedQuery(String queryName){
		return fHibernateSession.getNamedQuery(queryName);
	}
	
	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#isOpen()
	 */
	public boolean isOpen(){
		return fIsOpen;
	}
	
	public <T>T load(Class<T> clazz, Long id) throws SodaHibernateException{
		assertSessionIsOpen();
		
		return (T)fHibernateSession.load(clazz, id);
	}
	
	
	public Object merge(Object object) throws SodaHibernateException{
		assertSessionIsOpen();
		
		return fHibernateSession.merge(object);
	}

	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#open()
	 */
	public void open() throws SodaHibernateException {
		logger.debug("Opening session.");
		
		assertSessionIsClosed();
		
		SessionFactory sessionFactory = fHibernateAnnotationConfig.buildSessionFactory();
		fHibernateSession = sessionFactory.openSession();
		fIsOpen = true;
		sessionFactory.close();
		sessionFactory=null;
//		this.fHibernateSession.setFlushMode(FlushMode.ALWAYS);
		logger.debug("Session is now open.");
	}
	
	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#query(java.lang.String)
	 */
	public List query(String hqlQuery) throws SodaHibernateException{
		assertSessionIsOpen();
		
		Query query = fHibernateSession.createQuery(hqlQuery);
		return query.list();
	}

	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#query(java.lang.String, Class<T)
	 */
	public <T>List<T> query(String hqlQuery, Class<T> type) throws SodaHibernateException{
		assertSessionIsOpen();
		
		Query query = fHibernateSession.createQuery(hqlQuery);
		return query.list();
	}
	
	public void rollbackTransaction() throws SodaHibernateException {
		assertSessionIsOpen();
		//ensureTransactionIsActive();
		if (fTransaction!= null){
			fTransaction.rollback();
			fTransaction = null;
		}
		fHibernateSession.clear();
	}

	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#saveObject(java.lang.Object)
	 */
	public void saveObject(Object saveableObject) throws SodaHibernateException {
		assertSessionIsOpen();
		
		fHibernateSession.save(saveableObject);
	}

	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.api.IEvolizerSession#saveOrUpdate(java.lang.Object)
	 */
	public void saveOrUpdate(Object object) throws SodaHibernateException{
		assertSessionIsOpen();
		
		fHibernateSession.saveOrUpdate(object);
	}

	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#startTransaction()
	 */
	public void startTransaction() throws SodaHibernateException{
		assertSessionIsOpen();
		assertTransactionIsNotActive();
		
		fTransaction = fHibernateSession.beginTransaction();
	}

	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.IEvolizerSession#uniqueResult(java.lang.String)
	 */
	public Object uniqueResult(String hqlQuery) throws SodaHibernateException{
		assertSessionIsOpen();
		
		Query query = fHibernateSession.createQuery(hqlQuery);
		
		try {
			return query.uniqueResult();
		} catch (NonUniqueResultException e) {
			SodaHibernateException ex =  new SodaHibernateException("Non unique result for uniqueResult query");
//			logger.error("Non unique result for uniqueResult query", ex);
			
			throw ex;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.api.IEvolizerSession#load(java.lang.Class, java.lang.Long)
	 */
	public <T>T uniqueResult(String hqlQuery, Class<T> type) throws SodaHibernateException{
		assertSessionIsOpen();
		
		Query query = fHibernateSession.createQuery(hqlQuery);
		
		try {
			return (T) query.uniqueResult();
		} catch (NonUniqueResultException e) {
			SodaHibernateException ex =  new SodaHibernateException("Non unique result for uniqueResult query");
				logger.error("Non unique result for uniqueResult query", ex);
			
			throw ex;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.evolizer.hibernate.session.api.IEvolizerSession#update(java.lang.Object)
	 */
	public void update(Object updateableObject) throws SodaHibernateException{
		assertSessionIsOpen();
		
		fHibernateSession.update(updateableObject);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.evolizer.hibernate.session.api.IEvolizerSession#updateSchema()
	 */
	public void updateSchema() throws SodaHibernateException {
		assertSessionIsClosed("Could not update schema because session is open");
		
		SchemaUpdate updater = new SchemaUpdate(fHibernateAnnotationConfig);
		updater.execute(false, true);
	}
	
	public void refresh(Object object){
		fHibernateSession.refresh(object);
	}
}
