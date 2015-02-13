package com.icitic.core.platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.icitic.core.bundle.Resource;

public class JarEntryResource implements Resource {

	private JarFile jarFile;
	private ZipEntry entry;

	public JarEntryResource(JarFile jarFile, ZipEntry entry) {
		this.jarFile = jarFile;
		this.entry = entry;
	}

	public InputStream getInputStream() throws IOException {
		return jarFile.getInputStream(entry);
	}

	public boolean isDirectory() {
		return entry.isDirectory();
	}

	public String getName() {
		return entry.getName();
	}

    @Override
    public long getSize() {
        return entry.getSize();
    }

}
