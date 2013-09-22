package com.github.drapostolos.rdp4j;

import java.util.regex.Pattern;

import com.github.drapostolos.adp4j.spi.FileElement;

/**
 * A {@link FileFilter} accepting only {@link FileElement}s, who's name matches 
 * a regular expression.
 * 
 */
public final class RegexFileFilter implements FileFilter{
	private final Pattern pattern;

	/**
	 * Creates a new {@link FileFilter} that accepts only {@link FileElement}s, 
	 * who's name matches the given <code>regex</code>. 
	 * 
	 * @param regex a regular expression as defined in {@link Pattern}.
	 */
	public RegexFileFilter(String regex){
		this.pattern = Pattern.compile(regex);
	}

	/** {@inheritDoc} */
	public boolean accept(FileElement file){
		return pattern.matcher(file.getName()).matches();
	}
}
