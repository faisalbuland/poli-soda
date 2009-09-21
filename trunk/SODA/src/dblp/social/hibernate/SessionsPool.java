package dblp.social.hibernate;

import java.util.ArrayList;

import dblp.social.exceptions.PoolException;
import dblp.social.exceptions.SodaHibernateException;
/**
 * This class implements a pool of SodaHibernateSessions.
 * If more than one session is needed it is useful to have a pool containing a fixed number of sessions.
 * This because each session stores all the mapping informations, therefore, in order to avoid memory leaks, it is necessary to control the sessions proliferation.
 * 
 * @author Staffiero
 *
 */

public class SessionsPool {
	private static SessionsPool pool=null;
	private ArrayList<ISodaHibernateSession> sessions;
	private int availableSessions, sessionNumber;
	private String dbUrl, dbDialect, dbDriverName, dbUser, dbPasswd;
	private boolean accessAllowed;
	
	/**
	 * A private constructor. 
	 * Other classes should not access this constructor, use the static method initialize instead. 
	 * 
	 * @param dbUrl
	 * @param dbDialect
	 * @param dbDriverName
	 * @param dbUser
	 * @param dbPasswd
	 * @param sessionNumber
	 * @throws SodaHibernateException
	 */
	private SessionsPool(String dbUrl, String dbDialect, String dbDriverName, 
			String dbUser, String dbPasswd, int sessionNumber) 
				throws SodaHibernateException{
		this.dbUrl = dbUrl;
		this.dbDialect = dbDialect;
		this.dbDriverName = dbDriverName;
		this.dbUser = dbUser;
		this.dbPasswd = dbPasswd;
		this.sessionNumber = sessionNumber;
		this.sessions = new ArrayList<ISodaHibernateSession>();
		this.accessAllowed = true;

		for(int i=0; i<this.sessionNumber;i++){
			ISodaHibernateSession session = 
				SodaHibernateSessionFactory.getSession(this.dbUrl, this.dbDialect,
						this.dbDriverName, this.dbUser, this.dbPasswd, 
						new SodaModelProvider());
			sessions.add(session);
		}
		this.availableSessions = this.sessionNumber;
	}
	
	/**
	 * Static method to initialize the sessions pool.
	 * Only one session pool is allowed: if this method has already been called a second call will not affect the sessions pool.
	 * To create a new pool first you have close the current one with the closePool method.
	 * 
	 * @param dbUrl the database url
	 * @param dbDialect the database dialect
	 * @param dbDriverName the driver name
	 * @param dbUser the database username
	 * @param dbPasswd the password for the given username
	 * @param sessionNumber the number of SodaHibernateSessions contained in the pool
	 * @throws SodaHibernateException
	 */
	public static void initialize(String dbUrl, String dbDialect, String dbDriverName, 
			String dbUser, String dbPasswd, int sessionNumber) 
				throws SodaHibernateException{
		if (pool==null)
			pool = new SessionsPool(dbUrl, dbDialect, dbDriverName, dbUser, 
					dbPasswd, sessionNumber); 
	}
	
	/**
	 * Static method to get the existing sessions pool.
	 * 
	 * @return the current session pool.
	 * @throws PoolException if the pool has not been initialized yet.
	 */
	public static SessionsPool getInstance() throws PoolException{
		if (pool!=null)
			return pool;
		else 
			throw new PoolException("Pool not initialized");
	}
	
	/**
	 * Returns a session contained in the pool.
	 * After this call the session is removed from the pool.
	 * The class which uses this session has to give it back to the pool when finished.
	 * 
	 * @return a valid ISodaHibernateSession
	 */
	public synchronized ISodaHibernateSession getSession(){
		while (!this.accessAllowed){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.accessAllowed=false;
		if (availableSessions>0){
			ISodaHibernateSession session = sessions.get(0);
			sessions.remove(session);
			this.availableSessions=this.sessions.size();
			this.accessAllowed=true;
			return session;
		}
		else{
			this.accessAllowed=true;
			return null;
		}
	}
	
	/**
	 * Sends back a session to the pool.
	 * @param session a SodaHibernateSession previously taken from the pool. 
	 */
	public synchronized void sessionBackToPool(ISodaHibernateSession session){
		while (!this.accessAllowed){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.accessAllowed=false;
		this.sessions.add(session);
		this.availableSessions=this.sessions.size();
		this.accessAllowed=true;
	}
	
	/**
	 * Closes each session in the pool and then dereferences each session object.
	 * Note that closing a pool does not guarantees the sessions mapping to be removed.
	 * 
	 * @throws SodaHibernateException
	 */
	public void closePool() throws SodaHibernateException{
		for (ISodaHibernateSession session: this.sessions){
			if (session.isOpen())
				session.close();
			this.sessions.remove(session);
		}
		pool=null;
	}
}
