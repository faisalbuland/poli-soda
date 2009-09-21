package dblp.social.exceptions;

@SuppressWarnings("serial")
public class SodaHibernateException extends Exception {

	public SodaHibernateException(String message){
		super(message);
	}
	
	public SodaHibernateException(Throwable cause){
		super(cause);
	}

	public SodaHibernateException(String message, Throwable cause) {
		super(message, cause);
	}
}
