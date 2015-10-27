package org.eclipse.californium.actinium.plugnplay;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * This class is used to represent JAR (native java) modules a modules. The module object enables the JavaScript
 * engine to use classes defined in the JAR file. The object provides access to the Java classes mapped in the config
 * file. A mapping is defined by adding a key value entry to the config file. The key represents the property name and
 * the value defines the fully qualified class name. The property name must start with an uppercase letter. When accessing
 * the property the class is returned wrapped in an ExtendableNativeJavaClass object.
 */
public class NativeJavaModuleObject {

    public static Object create(NashornScriptEngine engine, DynamicClassloader classloader, File file, Properties properties) throws IOException, ScriptException {

        File jarFile = new File(file.getParentFile().getAbsolutePath() + File.separator + properties.getProperty("file"));
        URL fileURL = jarFile.toURI().toURL();
        String jarURL = "jar:" + fileURL + "!/";
        URL urls[] = {new URL(jarURL)};
        URLClassLoader ucl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        classloader.add(ucl);

        AppContext context = new AppContext();
        context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        return engine.eval( getJsSource(properties), context);
    }


    private static String getJsSource(Properties properties) {
        String items = "";
        for(String k: properties.stringPropertyNames()){
            items+="get "+k+"(){ return Java.type(\""+properties.getProperty(k)+"\")},";
        }
        return "(function () {\nvar exports = {"+items+"};\nreturn exports;\n}());";
    }
}
