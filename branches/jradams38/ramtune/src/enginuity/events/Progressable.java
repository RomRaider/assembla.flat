package enginuity.events;

public interface Progressable {

	public void addListener(ProgressListener listener);

	public void removeListener(ProgressListener listener);
}
