package enginuity.io.ds;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class FileSystemDataSource implements DataSource {
	
	private static HashMap<String, FileSystemDataSource> instances;

	private String baseFilePath;

	private boolean isOpen;


	public synchronized static FileSystemDataSource getInstance(
		String baseFilePath) {
		
		FileSystemDataSource instance = null;

		if (instances == null)
			instances = new HashMap<String, FileSystemDataSource>();
		
		if (instances.containsKey(baseFilePath))
			instance = instances.get(baseFilePath);
		else {
			instance = new FileSystemDataSource(baseFilePath);
			instances.put(baseFilePath, instance);
		}
		
		return instance;
	}
	
	private FileSystemDataSource(String baseFilePath) {
		super();
		this.baseFilePath = baseFilePath;
	}

	public synchronized void close() {
		isOpen = false;
	}

	public synchronized void open() throws Exception {
		isOpen = true;
	}
	
	public synchronized boolean isOpen() {
		return isOpen;
	}

	public byte[] readRam(int offset, int length) throws Exception {
		return read(".ram", offset, length);
	}

	public byte[] readRom(int offset, int length) throws Exception {
		return read(".rom", offset, length);
	}

	public void writeRam(int offset, byte[] data) throws Exception {
		write(".ram", offset, data);
	}

	public void writeRom(int offset, byte[] data) throws Exception {
		write(".rom", offset, data);
	}

	private synchronized byte[] read(
		String fileExtension, int offset, int length) throws Exception {
		
		boolean wasOpen = isOpen();

		byte[] data = new byte[length];
		FileInputStream stream = null;
		try {
			open();

			stream = new FileInputStream(baseFilePath + fileExtension);
			int bytesRead = stream.read(data, offset, length);
			if (bytesRead == -1)
				throw new IllegalArgumentException("Reached the EOF.");
			else if (bytesRead != length) {
				throw new Exception("Failed to read the total request.");
			}
		} catch (Exception e) {
			throw new Exception("Failure to read to the '" + baseFilePath +
				"' datasource.", e);
		} finally {
			if (stream != null)
				try { stream.close(); } catch (IOException e) {}
			if (!wasOpen) close();
		}

		return data;
	}
	
	private synchronized void write(
		String fileExtension, int offset, byte[] data) throws Exception {
		
		boolean wasOpen = isOpen();

		FileOutputStream stream = null;
		try {
			open();

			stream = new FileOutputStream(baseFilePath + fileExtension);
			stream.write(data, offset, data.length);
		} catch (Exception e) {
			throw new Exception("Failure to write to the '" + baseFilePath +
				"' datasource.", e);
		} finally {
			if (stream != null)
				try { stream.close(); } catch (IOException e) {}
			if (!wasOpen) close();
		}
	}
}
