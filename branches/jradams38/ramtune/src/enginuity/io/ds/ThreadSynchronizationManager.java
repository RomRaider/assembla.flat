package enginuity.io.ds;

public class ThreadSynchronizationManager {
	
	private Thread owningThread;

	private Object syncObject;

	public ThreadSynchronizationManager(Object syncObject) {
		this.syncObject = syncObject;
		owningThread = null;
	}

	public void open() throws Exception {
		synchronized (syncObject) {
			if (!hasOwnership()) {
				while (owningThread != null)
					wait();
	
				owningThread = Thread.currentThread();
			}
		}
	}

	public void close() {
		synchronized (syncObject) {
			if (hasOwnership()) {
				owningThread = null;
				notify();
			} else
				throw new IllegalStateException(
					"This resource is not owned by this thread.");
		}
	}
	
	private boolean hasOwnership() {
		return Thread.currentThread().equals(owningThread);
	}
}
