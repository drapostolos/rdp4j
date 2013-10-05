package com.github.drapostolos.rdp4j;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.drapostolos.rdp4j.spi.FileElement;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;

/**
 * An adapter for java's {@link File} class, that enables monitoring a directory 
 * on the local file system. The main purpose of this adapter is to provide
 * an example how to use an Abstract Directory Poller. 
 * <p>
 * Using this to listen to events on your local file system is discouraged, use the Java7 WatchService
 * functionality instead. For Java6 see other options <a href=http://www.rgagnon.com/javadetails/java-0617.html>here</a>.
 *
 * @see <a href="https://github.com/drapostolos/rdp4j/wiki/User-Guide">User Guide</a>.
 */
public final class JavaIoFileAdapter implements FileElement, PolledDirectory{
	private final File file;

	/**
	 * 
	 * @param file the directory to monitor for changes.
	 */
	public JavaIoFileAdapter(File file) {
		if(file == null){
			throw new NullPointerException("null argument not allowed!");
		}
		this.file = file;
	}
	
	/**
	 * 
	 * @return the {@link File} object wrapped by this adapter object.
	 */
	public File getFile(){
		return file;
	}

	/**
	 * @see File#lastModified()
	 */
	@Override
	public long lastModified() throws IOException {
		long lastModified = file.lastModified();
		if(lastModified == 0L){
			String message = 
					"Unknown I/O error occured when retriveing lastModified "	+ 
					"attribute for file '%s'.";
			throw new IOException(String.format(message, file));
		}
		return lastModified;
	}

	/**
	 * @see File#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	/**
	 * @see File#getName()
	 */
	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaIoFileAdapter other = (JavaIoFileAdapter) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}

	@Override
	public Set<FileElement> listFiles() throws IOException {
		Set<FileElement> result = new LinkedHashSet<FileElement>();
		File[] files = file.listFiles();
		if(files == null){
			String message = "Unknown I/O error when listing files in directory '%s'.";
			throw new IOException(String.format(message, file));
		}
		for(File child : file.listFiles()){
			result.add(new JavaIoFileAdapter(child));
		}
		return result;
	}

	/**
	 * @see File#toString()
	 */
	@Override
	public String toString() {
		return file.toString();
	}

}
