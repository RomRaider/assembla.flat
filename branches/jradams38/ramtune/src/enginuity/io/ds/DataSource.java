package enginuity.io.ds;

/**
 * Provides a mechanism to the rest of the application to perform low
 * level I/O while abstracting things like underlying protocols.  Synchronized
 * datasource access is always expected.  A <code>DataSource</code> should
 * support sharing over multiple threads.  Use of the <code>open</code> and
 * <code>close</code> methods is to avoid unnecessary resource usage.
 * <p />
 * <b>Remarks:</b>
 * <ul>
 * 		<li>
 * 			Implementations must gracefully handle the concept of invalidation.
 * 			For example, if the user were to "pull the plug" at any time,
 * 			any resulting exceptions should indicate that the current source is
 * 			invalid. 
 * 		<li>
 * 			This interface is meant to reside as a layer on top of the
 * 			<code>enginuity.connection</code> package.
 * 		</li>
 * 		<li>
 * 			It is hoped that the entire application will communicate through
 * 			this interface.
 * 		</li>
 * </ul>
 * 
 * @author jradams38
 */
public interface DataSource {
	
	/**
	 * Readies the <code>DataSource</code> for active use.
	 * 
	 * @throws Exception
	 */
	public void open() throws Exception;
	
	/**
	 * Signals the end of a read / write session.  This will typically
	 * flush any buffers that may be in use by the <code>DataSource</code>.
	 * <p />
	 * Consumers can expect this method will always succeed and never throw
	 * an exception. 
	 */
	public void close();
	
	public boolean isOpen();

	public byte[] readRam(int offset, int length) throws Exception;

	public void writeRam(int offset, byte[] data) throws Exception;

	public byte[] readRom(int offset, int length) throws Exception;
	
	public void writeRom(int offset, byte[] data) throws Exception;
}
