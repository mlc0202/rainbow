package com.icitic.core.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.icitic.core.bundle.BundleClassLoader;
import com.icitic.core.bundle.BundleException;
import com.icitic.core.bundle.Resource;
import com.icitic.core.bundle.ResourceProcessor;

public class JarClassLoader extends BundleClassLoader {

    private JarFile jarFile;

    public JarClassLoader(File file) throws IOException {
        super(file);
        jarFile = new JarFile(file);
    }

    public void destroy() {
        if (jarFile != null)
            try {
                jarFile.close();
            } catch (IOException e) {
            }
    }

    @Override
    public Resource getLocalResource(String resourceName) {
        ZipEntry entry = jarFile.getJarEntry(resourceName);
        if (entry == null)
            return null;
        return new JarEntryResource(jarFile, entry);
    }

    @Override
    public void procResource(ResourceProcessor processor) throws BundleException {
        Enumeration<JarEntry> entrys = jarFile.entries();
        while (entrys.hasMoreElements()) {
            JarEntry entry = entrys.nextElement();
            if (!entry.isDirectory()) {
                JarEntryResource jr = new JarEntryResource(jarFile, entry);
                processor.processResource(this, jr);
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        JarEntry entry = jarFile.getJarEntry(path);
        if (entry == null)
            throw new ClassNotFoundException(name);
        return defineClass(name, new JarEntryResource(jarFile, entry));
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        ZipEntry entry = jarFile.getJarEntry(name);
        if (entry==null)
            return null;
        try {
            return jarFile.getInputStream(entry);
        } catch (IOException e) {
            return null;
        }
    }

}
