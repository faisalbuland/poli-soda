package dblp.social.importer;

import java.util.ArrayList;

import dblp.social.exceptions.ThreadException;

/**
 * This class defines a thread monitor, used to synchronize the various instances of DblpParserThread
 * 
 * @author Staffiero
 *
 */
public class ThreadMonitor {
	private ArrayList<String> threads, lockedBooks, lockedProceedings;
	private boolean lock = false;
	private boolean closeThreads = false;
	private int threadsNumber = 4;
	private int maxThreads = 4;
	private boolean terminateProcess = false;
	
	
	/**
	 * The default constructor
	 */
	public ThreadMonitor(){
		this.threads = new ArrayList<String>();
		this.lockedBooks = new ArrayList<String>();
		this.lockedProceedings = new ArrayList<String>();
		this.closeThreads=false;
		this.threadsNumber = 4;
		this.terminateProcess =false;
		this.lock = false;
	}
	/**
	 * Getter
	 * @return true if all threads have to be closed
	 */
	public boolean closeThreads(){
		return this.closeThreads;
	}
	
	/**
	 * Setter, sets wether or not all threads have to be closed
	 * @param close
	 */
	public void setCloseThreads(boolean close){
		this.closeThreads=close;
	}
	
	/**
	 * Gets the lock on the _partsInfo file. Each thread should call this method BEFORE editing that file.
	 * This method has to be used together with the releaseLockOnDescriptor() method.
	 * The correct invocation chain within a thread is:
	 * 		
	 * 		ThreadMonitor monitor = new ThreadMonitor();
	 * 		//...
	 * 		monitor.getLockOnDescriptor();
	 * 		//edit the descriptor...
	 * 		monitor.releaseLockOnDescriptor();
	 */
	public synchronized void getLockOnDescriptor(){
		while (lock){
			try{
				wait();
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		lock=true;
	}
	
	/**
	 * Releases the lock on the _partsInfo file. Each thread should call this method AFTER editing that file and only AFTER the getLockOnDescriptor() call.
	 * This method has to be used together with the getLockOnDescriptor() method.
	 * The correct invocation chain within a thread is:
	 * 		
	 * 		ThreadMonitor monitor = new ThreadMonitor();
	 * 		//...
	 * 		monitor.getLockOnDescriptor();
	 * 		//edit the descriptor...
	 * 		monitor.releaseLockOnDescriptor();
	 */
	public synchronized void releaseLockOnDescriptor(){
		lock=false;
		notify();
	}
	/**
	 * Gets the lock on a book element. Each thread should call this method BEFORE editing a book element.
	 * This method has to be used together with the releaseLockOnBook() method.
	 * The correct invocation chain within a thread is:
	 * 		
	 * 		ThreadMonitor monitor = new ThreadMonitor();
	 * 		//...
	 * 		monitor.getLockOnBook();
	 * 		//edit the book...
	 * 		monitor.releaseLockOnBook();
	 * @param bookId the id of the book to be locked
	 */
	public  synchronized void getLockOnBook(String bookId){
		while (this.lockedBooks.contains(bookId)){
			try{
				wait();
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		this.lockedBooks.add(bookId);
	}
	/**
	 * Releases the lock on the book object. Each thread should call this method AFTER editing a book element and only AFTER the getLockOnBook() call.
	 * This method has to be used together with the getLockOnBook() method.
	 * The correct invocation chain within a thread is:
	 * 		
	 * 		ThreadMonitor monitor = new ThreadMonitor();
	 * 		//...
	 * 		monitor.getLockOnBook();
	 * 		//edit the book...
	 * 		monitor.releaseLockOnBook();
	 */
	public synchronized void releaseLockOnBook(String bookId){
		if (this.lockedBooks.contains(bookId))
			this.lockedBooks.remove(bookId);
		notify();
	}
	/**
	 * Gets the lock on a proceedings, proceedings element. Each thread should call this method BEFORE editing a proceedings element.
	 * This method has to be used together with the releaseLockOnProceedings() method.
	 * The correct invocation chain within a thread is:
	 * 		
	 * 		ThreadMonitor monitor = new ThreadMonitor();
	 * 		//...
	 * 		monitor.getLockOnProceedings();
	 * 		//edit the proceedings...
	 * 		monitor.releaseLockOnProceedings();
	 * @param proceedingsId the id of the proceedings to be locked
	 */
	public  synchronized void getLockOnProceedings(String proceedingsId){
		while (this.lockedProceedings.contains(proceedingsId)){
			try{
				wait();
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		this.lockedProceedings.add(proceedingsId);
	}
	
	/**
	 * Releases the lock on the proceedings object. Each thread should call this method AFTER editing a proceedings element and only AFTER the getLockOnProceedings() call.
	 * This method has to be used together with the getLockOnProceedings() method.
	 * The correct invocation chain within a thread is:
	 * 		
	 * 		ThreadMonitor monitor = new ThreadMonitor();
	 * 		//...
	 * 		monitor.getLockOnProceedings();
	 * 		//edit the proceedings...
	 * 		monitor.releaseLockOnProceedings();
	 */
	public synchronized void releaseLockOnProceedings(String proceedingsId){
		if (this.lockedProceedings.contains(proceedingsId))
			this.lockedProceedings.remove(proceedingsId);
		notify();
	}
	
	/**
	 * Releases all locks. 
	 */
	public synchronized void releaseAllLocks(){
		this.lockedBooks = new ArrayList<String>();
		this.lockedProceedings = new ArrayList<String>();
		this.lock = false;
		notify();
	}
	
	/**
	 * Adds a thread to the list of current running threads
	 * 
	 * @param threadName
	 */
	public void addThread(String threadName){
		threads.add(threadName);
	}
	/**
	 * Removes a thread form the list of running threads.
	 * @param threadName
	 */
	public synchronized void removeThread(String threadName){
		if (threads.contains(threadName)){
			threads.remove(threadName);
			notify();
		}
	}
	/**
	 * Indicates if the parser has to be stopped
	 * @return true if the parser has to be stopped, false otherwise
	 */
	public boolean terminateProcess() {
		return terminateProcess;
	}
	/**
	 * Sets a boolean value indicating if the parser has to be stopped
	 * @param terminateProcess
	 */
	public void setTerminateProcess(boolean terminateProcess) {
		this.terminateProcess = terminateProcess;
	}
	/**
	 * Getter
	 * @return: the maximum number of concurrent threads
	 */
	public int getMaxThreads() {
		return maxThreads;
	}
	/**
	 * Setter, sets the maximum number of concurrent threads
	 * @param maxThreads
	 */
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}
	/**
	 * Getter
	 * @return: the number of threads to be started
	 */
	public int getThreadsNumber() {
		return threadsNumber;
	}
	/**
	 * Setter, sets the number of threads to be started
	 * @param threadsNumber
	 */
	public void setThreadsNumber(int threadsNumber) {
		this.threadsNumber = threadsNumber;
	}
	/**
	 * Waits for all threads to terminate
	 */
	public synchronized void waitForThreads(){
		
		while (threads.size() != 0){
			try{
				wait();
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * @return the number of running threads
	 */
	public int runningThreads(){
		return threads.size();
	}
	/**
	 * Set the number of threads to be started according to the amount of free memory. 
	 * If this method is called from within a thread it may cause the thread itself to terminate (if the free memory is below a certain threshold).
	 * @param inThreadFlag a boolean variable which indicates if this method is called from within a thread
	 * @throws Exception
	 */
	public void calibrateThreads(boolean inThreadFlag) throws ThreadException{
		Runtime r = Runtime.getRuntime();
		long totalMemory =r.totalMemory();
		long maxMemory = r.maxMemory();
		long freeMemory =r.freeMemory();
		int actualThreads = this.getThreadsNumber();
		long maxFreeMemory = freeMemory+(maxMemory-totalMemory);
		
		switch (this.maxThreads) {
			case 2: {
				//Calibrates the number of threads over a max of 2:
				//if max free mem < 20% max mem: 1 thread
				//else 2 thread		
				if (maxFreeMemory>=totalMemory*0.2){
					//at next restart there will be 2 threads
					if (actualThreads!=2){
						this.setThreadsNumber(2);
					}
				}
				else{ //max free mem <20%
					if (actualThreads!=1){
						this.setThreadsNumber(1);
						if (inThreadFlag)
							//if there are 2 threads running closes one of them
							throw new ThreadException("Forcing thread termination: max free memory below 20% of total memory");
					}
				}
				break;
			}
			case 3: {
				//Calibrates the number of threads over a max of 3:
				//if max free mem 		>= 50% max mem: 	3 thread
				//else if max free mem 	>= 20% max mem: 	2 thread
				//else		 								1 thread
				if (maxFreeMemory>=totalMemory*0.5){
					//at next restart there will be 2 threads
					if (actualThreads!=3){
						this.setThreadsNumber(3);
					}
				}
				else if (maxFreeMemory>=totalMemory*0.2){
					if (actualThreads!=2){
						this.setThreadsNumber(2);
						if (actualThreads == 3 && inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 50% of total memory");
					}
				}
				else {
					if (actualThreads!=1){
						this.setThreadsNumber(1);
						if (inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 20% of total memory");
					}
				}
				break;
			}
			case 4: {
				//Calibrates the number of threads over a max of 4:
				//if max free mem 		>= 50% max mem: 	4 thread
				//else if max free mem 	>= 30% max mem: 	3 thread
				//else if max free mem	>= 10% max mem:		2 thread
				//else 										1 thread
				if (maxFreeMemory>=totalMemory*0.5){
					//at next restart there will be 2 threads
					if (actualThreads!=4){
						this.setThreadsNumber(4);
					}
				}
				else if (maxFreeMemory>=totalMemory*0.3){
					if (actualThreads!=3){
						this.setThreadsNumber(3);
						if (actualThreads == 4 && inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 50% of total memory");
					}
				}
				else if (maxFreeMemory>=totalMemory*0.1){
					if (actualThreads!=2){
						this.setThreadsNumber(2);
						if (actualThreads >= 3 && inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 30% of total memory");
					}
				}
				else {
					if (actualThreads!=1){
						this.setThreadsNumber(1);
						if (inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 10% of total memory");
					}
				}
				break;
			}
			case 5: {
				//Calibrates the number of threads over a max of 5:
				//if max free mem 		>= 50% max mem: 	5 thread
				//else if max free mem 	>= 35% max mem: 	4 thread
				//else if max free mem	>= 15% max mem:		3 thread
				//else if max free mem	>= 5%  max mem:		2 thread
				//else										1 thread
				if (maxFreeMemory>=totalMemory*0.5){
					//at next restart there will be 2 threads
					if (actualThreads!=5){
						this.setThreadsNumber(5);
					}
				}
				else if (maxFreeMemory>=totalMemory*0.35){
					if (actualThreads!=4){
						this.setThreadsNumber(4);
						if (actualThreads == 5 && inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 50% of total memory");
					}
				}
				else if (maxFreeMemory>=totalMemory*0.15){
					if (actualThreads!=3){
						this.setThreadsNumber(3);
						if (actualThreads >= 4 && inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 35% of total memory");
					}
				}
				else if (maxFreeMemory>=totalMemory*0.5){
					if (actualThreads!=2){
						this.setThreadsNumber(2);
						if (actualThreads >= 3 && inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 15% of total memory");
					}
				}
				else {
					if (actualThreads!=1){
						this.setThreadsNumber(1);
						if (inThreadFlag) 
							throw new ThreadException("Forcing thread termination: max free memory below 5% of total memory");
					}
				}
				break;
			}
		}
	}
}
