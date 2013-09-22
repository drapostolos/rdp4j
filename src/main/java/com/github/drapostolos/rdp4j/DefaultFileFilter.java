package com.github.drapostolos.rdp4j;

import com.github.drapostolos.rdp4j.spi.FileElement;

final class DefaultFileFilter implements FileFilter{

	@Override
	public boolean accept(FileElement file) {
		return true;
	}

}
