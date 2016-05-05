package org.cobbzilla.util.javascript;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;

import static org.cobbzilla.util.json.JsonUtil.fromJsonOrDie;

public class JsEngine {

    public static <T> T evaluate(String code, String scriptName, Map<String, Object> context, Class<T> returnType) {
        final Context ctx = Context.enter();
        final Scriptable scope = ctx.initStandardObjects();

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            final Object value = entry.getValue();
            final Object wrappedOut;
            if (value instanceof ArrayNode) {
                final Object[] array = fromJsonOrDie((JsonNode) value, Object[].class);
                wrappedOut = Context.javaToJS(array, scope);
            } else if (value instanceof JsonNode) {
                final Object object = fromJsonOrDie((JsonNode) value, Object.class); // todo: insert as a JS object
                wrappedOut = Context.javaToJS(object, scope);
            } else {
                wrappedOut = Context.javaToJS(value, scope);
            }
            ScriptableObject.putProperty(scope, entry.getKey(), wrappedOut);
        }
        final Object result = ctx.evaluateString(scope, code, scriptName, 1, null);
        return result == null ? null : (T) Context.jsToJava(result, returnType);
    }

    public static String replaceDollarSigns(String val) {
        return val.replaceAll("(\\$(\\d+(\\.\\d{2})?))", "($2 * 100)");
    }
}
