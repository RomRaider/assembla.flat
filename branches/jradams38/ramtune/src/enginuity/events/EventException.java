package enginuity.events;

import java.util.Collection;

public class EventException extends Exception {

	private static final long serialVersionUID = 2996350081103206327L;

	private Collection<Exception> exceptions;

	private String eventTitle;

	public EventException(String eventTitle, Collection<Exception> exceptions) {
		this.eventTitle = eventTitle;
		this.exceptions = exceptions;
	}
	
	public String toString() {
		return "The event '" + eventTitle + "' caused " + exceptions.size() + " exceptions";
	}
}
