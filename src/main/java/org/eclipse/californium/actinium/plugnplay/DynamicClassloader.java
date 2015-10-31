package org.eclipse.californium.actinium.plugnplay;

import javassist.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by ynh on 26/10/15.
 */
public class DynamicClassloader extends ClassLoader {

    private Map<String, Class<?>> classesMap = new HashMap<String, Class<?>>();
    private Set<String> addedJarFiles = new HashSet<>();
    private ArrayList<ClassLoader> classLoaders;

    public DynamicClassloader(ClassLoader parent) {
        super(parent);
        classLoaders = new ArrayList<>();
    }

    // Adding dynamically created classes
    public void defineClass(String name, Class<?> clazz) {
        classesMap.put(name, clazz);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader cl : classLoaders) {
            try {
                return cl.loadClass(name);
            } catch (Exception e) {

            }
        }
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            if (name.startsWith("gen.")) {
                try {
                    return generateInterface(name);
                } catch (Exception e1) {
                }
            }
            throw e;
        }
    }

    private Class<?> generateInterface(String name) throws Exception {
        int i = name.lastIndexOf('_');
        if (i > 0) {
            String[] segments = {name.substring(0, i), name.substring(i + 1)};
            String methodname = segments[0].substring(4);
            int arguments = Integer.parseInt(segments[1]);
            return generateInterface(methodname, arguments);
        }
        throw new Exception("Invalid gen class name");
    }

    private Class<?> generateInterface(String methodname, int arguments) throws NotFoundException, CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctObject = pool.getCtClass("java.lang.Object");
        CtClass cc = pool.makeInterface("gen." + methodname + "_" + arguments);
        CtClass[] args = new CtClass[arguments];
        for (int i = 0; i < arguments; i++) {
            args[i] = ctObject;
        }
        CtMethod method = new CtMethod(ctObject, methodname, args, cc);
        cc.addMethod(method);
        return cc.toClass();
    }


    public void add(URLClassLoader ucl) {
        classLoaders.add(ucl);
    }

    public void addJARFile(URL fileURL) throws MalformedURLException {
        String jarURL = "jar:" + fileURL + "!/";
        if (addedJarFiles.add(jarURL)) {
            URL urls[] = {new URL(jarURL)};
            URLClassLoader ucl = new URLClassLoader(urls);
            add(ucl);
        }
    }
}