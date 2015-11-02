package org.eclipse.californium.actinium.jsmodule;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.eclipse.californium.actinium.Utils;
import org.eclipse.californium.actinium.plugnplay.AppContext;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class JavaScriptModuleObject  {

    /**
     * Returns an object containing the exported variables. The variables are exported using the exports
     * variable as defined in the CommonJS module format.
     */
    public static Object create(String name, NashornScriptEngine engine, AppContext ctx, File jsFile) throws FileNotFoundException, ScriptException {
        String content = Utils.readFile(jsFile);
        AppContext context = new AppContext();
        context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        context.setAttribute(ScriptEngine.FILENAME, name, ScriptContext.ENGINE_SCOPE);
        context.setAttribute(ScriptEngine.NAME, name, ScriptContext.ENGINE_SCOPE);
        Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);
        engineScope.put("require", ctx.getAttribute("require"));
        engineScope.put("dump", ctx.getAttribute("dump"));
        engineScope.put("extend", ctx.getAttribute("extend"));
        return engine.eval(transformSource(name, content), context);
    }

    /**
     * Inject source code in to module context defined by the CommonJS module format.
     * http://wiki.commonjs.org/wiki/Modules/1.1.1
     * @param name
     * @param content
     * @return
     */
    private static String transformSource(String name, String content) {
        return "(function () {\nvar module = {get id(){ return \""+name.replace("\"","\\\"")+"\";}};\nvar exports = {}\n"+content+"\nreturn exports;\n}());";
    }
}
