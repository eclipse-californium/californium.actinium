package org.eclipse.californium.actinium.plugnplay;

import javassist.*;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ynh on 26/10/15.
 */
public class DynamicClassloader extends ClassLoader {

    private Map<String, Class<?>> classesMap = new HashMap<String, Class<?>>();
    private ArrayList<ClassLoader> classLoaders;

    public DynamicClassloader(ClassLoader parent) {
        // Also tried super(parent);
        super(sun.misc.Launcher.getLauncher().getClassLoader());
        classLoaders=new ArrayList<>();
    }

    // Adding dynamically created classes
    public void defineClass(String name, Class<?> clazz) {
        classesMap.put(name, clazz);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for(ClassLoader cl: classLoaders){
            try {
                return cl.loadClass(name);
            }catch (Exception e){

            }
        }
        return super.loadClass(name);
    }


    public void add(URLClassLoader ucl) {
        classLoaders.add(ucl);
    }
}