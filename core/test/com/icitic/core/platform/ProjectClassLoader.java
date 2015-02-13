package com.icitic.core.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.icitic.core.bundle.BundleClassLoader;
import com.icitic.core.bundle.BundleException;
import com.icitic.core.bundle.Resource;
import com.icitic.core.bundle.ResourceProcessor;

public class ProjectClassLoader extends BundleClassLoader {

    private File root;

    public ProjectClassLoader(File file) throws IOException {
        super(file);
        root = file;
    }

    @Override
    public Resource getLocalResource(String resourceName) {
        File file = new File(root, resourceName);
        if (!file.exists())
            return null;
        return new FileResource(resourceName, file);
    }

    @Override
    public void procResource(ResourceProcessor processor) throws BundleException {
        procDirResource(root, "", processor);
    }

    private void procDirResource(File r, String path, ResourceProcessor processor) throws BundleException {
        File[] files = r.listFiles();
        if (files == null || files.length == 0)
            return;
        for (File file : files)
            if (file.isDirectory())
                procDirResource(file, path + file.getName() + "/", processor);
            else {
                FileResource fr = new FileResource(path + file.getName(), file);
                processor.processResource(this, fr);
            }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        File entry = new File(root, path);
        if (!entry.exists() && !entry.isFile())
            throw new ClassNotFoundException(name);
        return defineClass(name, new FileResource(path, entry));
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            File file = new File(root, name);
            return new FileInputStream(file);
        } catch (IOException e) {
            return null;
        }
    }
}
