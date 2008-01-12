package enginuity.events;

import java.util.HashSet;
import java.util.ArrayList;

/**
 * @note The whole class could probably be generalized quite elegantly using reflection.
 * @author john
 *
 */
public class ProgressListenerCollection {

	private volatile HashSet<ProgressListener> listeners;
	
	
	public ProgressListenerCollection() {
	}
	
	public void add(ProgressListener listener) {
		listeners.add(listener);
	}
	
	public void remove(ProgressListener listener) {
		listeners.remove(listener);
	}
	
	public void notifyProgress(String message, float progressQuotient)
		throws Exception {
		ArrayList<Exception> exceptions = new ArrayList<Exception>();

		for (ProgressListener listener : listeners)
			try {
				listener.onProgress(message, progressQuotient);
			} catch (Exception e) {
				exceptions.add(e);
			}
		
		if (!exceptions.isEmpty())
			throw new EventException("onProgress", exceptions);
	}
	
	public void notifyWarning(String message)
		throws Exception {
		ArrayList<Exception> exceptions = new ArrayList<Exception>();

		for (ProgressListener listener : listeners)
			try {
				listener.onWarning(message);
			} catch (Exception e) {
				exceptions.add(e);
			}
		
		if (!exceptions.isEmpty())
			throw new EventException("onWarning", exceptions);
	}
	
	public void notifyFailure(Exception e)
		throws Exception {
		ArrayList<Exception> exceptions = new ArrayList<Exception>();

		for (ProgressListener listener : listeners)
			try {
				listener.onFailure(e);
			} catch (Exception ex) {
				exceptions.add(ex);
			}
		
		if (!exceptions.isEmpty())
			throw new EventException("onFailure", exceptions);
	}
}
