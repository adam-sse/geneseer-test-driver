package net.ssehub.program_repair.geneseer.evaluation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TestClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }
    
    private static final String[] SYSTEM_CLASS_PREFIXES = {
        "java.", "javax.", "sun.", "com.sun.", // JRE
        "org.junit.", "junit.", "org.hamcrest.", // JUnit
        "org.jacoco.agent.rt.", // JaCoCo
        "org.mockito.", "net.bytebuddy.", "org.objenesis.", // Mockito (though not used by us, does not work when using
                                                            // separate class loaders
        "net.ssehub.program_repair.geneseer.evaluation." // us
    };
    
    private Set<String> loadedByUs;
    private Set<String> delegatedToParent;
    
    public TestClassLoader() throws IOException {
        super(getClassPathUrls(), TestClassLoader.class.getClassLoader());
    }
    
    public void recordClassLoadsForDebug() {
        this.loadedByUs = new LinkedHashSet<>();
        this.delegatedToParent = new LinkedHashSet<>();
    }
    
    @Override
    public void close() throws IOException {
        if (loadedByUs != null && delegatedToParent != null) {
            TestDriver.debugMsg(getClass().getSimpleName() + " closed after test");
            TestDriver.debugMsg("    loadedByUs: " + loadedByUs);
            TestDriver.debugMsg("    delegatedToParent: " + delegatedToParent);
        }
        super.close();
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
                if (!isSystemClass(name)) {
                    try {
                        loaded = findClass(name);
                        if (loadedByUs != null) {
                            loadedByUs.add(name);
                        }
                    } catch (ClassNotFoundException e) {
                        loaded = super.loadClass(name, false);
                        if (delegatedToParent != null) {
                            delegatedToParent.add(name);
                        }
                    }
                } else {
                    loaded = super.loadClass(name, false);
                    if (delegatedToParent != null) {
                        delegatedToParent.add(name);
                    }
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
            if (url != null && delegatedToParent != null) {
                delegatedToParent.add(name);
            }
        } else if (loadedByUs != null) {
            loadedByUs.add(name);
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

    private boolean isSystemClass(String name) {
        boolean systemClass = false;
        for (String prefix : SYSTEM_CLASS_PREFIXES) {
            if (name.startsWith(prefix)) {
                systemClass = true;
                break;
            }
        }
        return systemClass;
    }

}
