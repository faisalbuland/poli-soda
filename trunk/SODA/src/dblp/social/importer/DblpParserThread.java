package dblp.social.importer;

import org.apache.log4j.Logger;

import dblp.social.exceptions.PoolException;
import dblp.social.hibernate.ISodaHibernateSession;
import dblp.social.hibernate.SessionsPool;
import dblp.social.loader.PartFile;
import dblp.social.loader.PartFilesPool;

/**
 * This class defines a thread which will parse a single part file produced by the pre-parser.
 * @author Staffiero
 *
 */
public class DblpParserThread extends Thread {
	
	private PartFile partFile;
	private PartFilesPool pool;
	private ThreadMonitor monitor;
	private String name;
	private ISodaHibernateSession session;
	private static Logger logger = Logger.getLogger(DblpParserThread.class);
	
	/**
	 * The constructor to be used
	 * 
	 * @param pf: the PartFile object which represents the part file to be parsed
	 * @param threadName: the name of the thread
	 * @param monitor: the thread monitor
	 * @throws PoolException
	 */
	public DblpParserThread(PartFile pf, String threadName, 
			ThreadMonitor monitor) throws PoolException{
		super(threadName);
		this.name=threadName;
		this.session=SessionsPool.getInstance().getSession();
		this.partFile = pf;
		this.pool = PartFilesPool.getInstance();
		this.monitor = monitor;
	}

	@Override
	public void run() {		
		try{
			if (!this.session.isOpen())
				this.session.open();
			//creates a new parser and starts it
			DblpParser parser = new DblpParser();
			parser.parseDbpl(this.partFile, this.monitor, this.session);
			//if the parser terminates without throwing exceptions sends back the 
			//PartFile to the pool
			pool.parseFinished(this.partFile);
			//removes the thread from the list of running threads
			monitor.removeThread(this.name);
			//tries to close the session
			if (this.session.isOpen())
				this.session.close();
			//sends back the session to the sessions pool
			SessionsPool.getInstance().sessionBackToPool(this.session);
			this.session=null;
		}
		catch (Exception e){
			try{
				//TODO Debug code
				logger.error("Exception in thread parsing "+this.partFile.getFile().getName() + "." +
				"\nException message: "+ e.getMessage()+".\nStopping thread");
//				e.printStackTrace();
				
				//a generic exception may not be an error. 
				logger.debug("Exception occurred in thread parsing "+this.partFile.getFile().getName() + "." +
						"\nException message: "+ e.getMessage()+".\nStopping thread");
				//the parse method stopped before completely parse the file, therfore
				//the part file is sent back to the part files pool
				pool.giveBack(this.partFile);
				//sends back the session to the session pool
				SessionsPool.getInstance().sessionBackToPool(this.session);
				this.session=null;
				//removes the thread from the list of running threads
				monitor.removeThread(this.name);
				this.finalize();
			}
			catch(Throwable ex){
				logger.error("Exception while closing the thread: "+ex.getMessage() );
				ex.printStackTrace();
			}
		}
		catch (OutOfMemoryError oom){
			//Catches out of memory error. If catches an out of memory error 
			// the descriptor hasn't been updated
			try{
				Runtime r = Runtime.getRuntime();
				logger.error("Out of memory Error, handled in DblpParser thread\n" +
						"Free memory: "+r.freeMemory()+"\n");
				
				//gives back the part file to the pool
				pool.giveBack(this.partFile);
				//sends back the session to the session pool
				SessionsPool.getInstance().sessionBackToPool(this.session);
				this.session=null;
				//prevents the main loop from cycling again
				monitor.setTerminateProcess(true);
				//block each other thread
				monitor.setCloseThreads(true);
				//removes the current thread from the active threads list
				monitor.removeThread(this.name);
				//Calls a separate process to update the descriptor
//				Runtime.getRuntime().exec("java -cp ./bin dblp.social.utility.DescriptorUpdater " +
//						this.partFile.getFile() +" "+
//						this.partFile.getPartFileDescriptor()+" "+
//						this.partFile.getLastCheckpoint() +" "+
//						this.partFile.getLastOffset());
			}
			catch(Throwable tr){
				logger.error("exception while handling an OutOfMemoryError: "+tr.getMessage());
				tr.printStackTrace();
			}
		}
	}
}
