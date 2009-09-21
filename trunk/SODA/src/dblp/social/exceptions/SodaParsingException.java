package dblp.social.exceptions;

@SuppressWarnings("serial")
public class SodaParsingException extends Exception {

	public SodaParsingException(String message){
		super(message);
	}
	
	public SodaParsingException(Throwable cause){
		super(cause);
	}

	public SodaParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}
