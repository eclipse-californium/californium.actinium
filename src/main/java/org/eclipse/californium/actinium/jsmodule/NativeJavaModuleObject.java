package org.eclipse.californium.actinium.jsmodule;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.eclipse.californium.actinium.plugnplay.AppContext;
import org.eclipse.californium.actinium.plugnplay.DynamicClassloader;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;


public class NativeJavaModuleObject {

    /**
     * Returns an object that represents a JAR (native java) module. The module object enables the JavaScript
     * engine to use classes defined in the JAR file. The object provides access to the Java classes mapped in the config
     * file. A mapping is defined by adding a key value entry to the config file. The key represents the property name and
     * the value defines the fully qualified class name. The property name must start with an uppercase letter.
     */
    public static Object create(NashornScriptEngine engine, AppContext ctx, DynamicClassloader classloader, File file, Properties properties) throws IOException, ScriptException {
        File jarFile = new File(file.getParentFile().getAbsolutePath() + File.separator + properties.getProperty("file"));
        URL fileURL = jarFile.toURI().toURL();
        classloader.addJARFile(fileURL);
        return engine.eval(getJsSource(properties), ctx);
    }


    private static String getJsSource(Properties properties) {
        String items = "";
        for (String k : properties.stringPropertyNames()) {
            if(Character.isUpperCase(k.codePointAt(0))) {
                items += "get " + k + "(){ return Java.type(\"" + properties.getProperty(k) + "\")},";
            }
        }
        return "(function () {\nvar exports = {" + items + "};\nreturn exports;\n}());";
    }
}
