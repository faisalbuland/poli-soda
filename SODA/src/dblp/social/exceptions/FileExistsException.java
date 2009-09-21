package dblp.social.exceptions;

/**
 * Custom exception used by the Pre-parser classes
 * @author Staffiero
 */
public class FileExistsException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4243833541947215801L;

	public FileExistsException(String message){
		super(message);
	}
	
	public FileExistsException(Throwable cause){
		super(cause);
	}

	public FileExistsException(String message, Throwable cause) {
		super(message, cause);
	}	
}
