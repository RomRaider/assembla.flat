package enginuity.events;

public interface ProgressListener {

	public void onProgress(String message, float progressQuotient);
	
	public void onFailure(Exception e);
	
	public void onWarning(String message);
}
