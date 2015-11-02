package org.eclipse.californium.actinium.jsmodule;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.eclipse.californium.actinium.Utils;
import org.eclipse.californium.actinium.plugnplay.AppContext;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;


public class JavaScriptModuleObject  {

    /**
     * Returns an object containing the exported variables. The variables are exported using the exports
     * variable as defined in the CommonJS module format.
     */
    public static Object create(String name, NashornScriptEngine engine, AppContext ctx, File jsFile) throws FileNotFoundException, ScriptException {
        String content = Utils.readFile(jsFile);
        return engine.eval(transformSource(name, content), ctx);
    }

    /**
     * Inject source code in to module context defined by the CommonJS module format.
     * http://wiki.commonjs.org/wiki/Modules/1.1.1
     * @param name
     * @param content
     * @return
     */
    private static String transformSource(String name, String content) {
        return "(function () {\n" +
                "var module = {get id(){ return \""+name.replace("\"","\\\"")+("\";}};\n" +
                "var exports = {};\nvar app=null;").replace("\n", " ")+content+"\n" +
                "return exports;\n" +
                "}).apply({});";
    }
}
