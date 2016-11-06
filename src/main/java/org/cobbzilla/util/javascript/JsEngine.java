package org.cobbzilla.util.javascript;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;

import static org.cobbzilla.util.json.JsonUtil.fromJsonOrDie;

public class JsEngine {

    public static String scriptName (Object caller, String name) {
        return caller.getClass().getSimpleName() + ":" + name;
    }

    public static <T> T evaluate(String code, Map<String, Object> context, String scriptName, Class<T> returnType) {
        final Context ctx = Context.enter();
        final Scriptable scope = ctx.initStandardObjects();

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            final Object value = entry.getValue();
            final Object wrappedOut;
            if (value instanceof JsWrappable) {
                wrappedOut = Context.javaToJS(((JsWrappable) value).jsObject(), scope);
            } else if (value instanceof ArrayNode) {
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

    public static boolean evaluateBoolean(String code, Map<String, Object> ctx, String scriptName) {
        final Object result = evaluate(code, ctx, scriptName, Object.class);
        return result == null ? false : Boolean.valueOf(result.toString().toLowerCase());
    }

    public static Integer evaluateInt(String code, Map<String, Object> ctx, String scriptName) {
        final Object result = evaluate(code, ctx, scriptName, Object.class);
        if (result == null) return null;
        if (result instanceof Number) return ((Number) result).intValue();
        return Integer.parseInt(result.toString().trim());
    }

    public static Long evaluateLong(String code, Map<String, Object> ctx, String scriptName) {
        final Object result = evaluate(code, ctx, scriptName, Object.class);
        if (result == null) return null;
        if (result instanceof Number) return ((Number) result).longValue();
        return Long.parseLong(result.toString().trim());
    }

}
