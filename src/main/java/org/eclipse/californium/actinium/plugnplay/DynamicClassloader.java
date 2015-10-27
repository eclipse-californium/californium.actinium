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
        try {
            return super.loadClass(name);
        }catch (ClassNotFoundException e){
            if(name.startsWith("gen.")) {
                String[] segments = name.split("_");
                if (segments.length == 2) {
                    try {
                        ClassPool pool = ClassPool.getDefault();
                        CtClass ctObject = pool.getCtClass("java.lang.Object");
                        String methodname = segments[0];
                        int arguments = Integer.parseInt(segments[1]);
                        CtClass cc = pool.makeInterface(name);
                        CtClass[] args = new CtClass[arguments];
                        for (int i = 0; i < arguments; i++) {
                            args[i] = ctObject;
                        }
                        CtMethod method = new CtMethod(ctObject, methodname.substring(4), args, cc);
                        cc.addMethod(method);
                        return cc.toClass();
                    } catch (NotFoundException e1) {
                        e1.printStackTrace();
                    } catch (CannotCompileException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            throw e;
        }
    }


    public void add(URLClassLoader ucl) {
        classLoaders.add(ucl);
    }
}