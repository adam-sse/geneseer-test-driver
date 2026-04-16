package net.ssehub.program_repair.geneseer.evaluation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TestClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }
    
    private static final String[] SYSTEM_CLASS_PREFIXES = {
        "java.", "javax.", "sun.", "com.sun.", // JRE
        "org.junit.", "junit.", "org.hamcrest.", // JUnit
        "org.jacoco.agent.rt.", // JaCoCo
        "net.ssehub.program_repair.geneseer.evaluation." // us
    };
    
    public TestClassLoader() throws IOException {
        super(getClassPathUrls(), TestClassLoader.class.getClassLoader());
    }
    
    private static URL[] getClassPathUrls() throws IOException {
        String classPath = System.getProperty("java.class.path");
        String[] entries = classPath.split(File.pathSeparator);

        URL[] urls = new URL[entries.length];
        for (int i = 0; i < entries.length; i++) {
            urls[i] = new File(entries[i]).toURI().toURL();
        }
        return urls;
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                if (shouldLoadChildFirst(name)) {
                    try {
                        loaded = findClass(name);
                    } catch (ClassNotFoundException e) {
                        loaded = super.loadClass(name, false);
                    }
                } else {
                    loaded = super.loadClass(name, false);
                }
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            url = super.getResource(name);
        }
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> result = new ArrayList<>();

        Enumeration<URL> own = findResources(name);
        while (own.hasMoreElements()) {
            result.add(own.nextElement());
        }

        Enumeration<URL> parent = getParent().getResources(name);
        while (parent.hasMoreElements()) {
            result.add(parent.nextElement());
        }

        return java.util.Collections.enumeration(result);
    }

    private boolean shouldLoadChildFirst(String name) {
        boolean systemClass = false;
        for (String prefix : SYSTEM_CLASS_PREFIXES) {
            if (name.startsWith(prefix)) {
                systemClass = true;
                break;
            }
        }
        return !systemClass;
    }

}
