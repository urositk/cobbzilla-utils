package org.cobbzilla.util.javascript;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StandardJsEngine extends JsEngine {

    private static final String STANDARD_FUNCTIONS

            // function to find element in array
            = "\nfunction found (item, arr) { return arr != null && arr.indexOf(''+item) != -1; }"

            // functions for rounding up/down to nearest multiple
            + "\nfunction up (x, multiple) { return multiple * parseInt(Math.ceil(parseFloat(x)/parseFloat(multiple))); }"
            + "\nfunction down (x, multiple) { return multiple * parseInt(Math.floor(parseFloat(x)/parseFloat(multiple))); }"
            + "\n";

    public static <T> T evaluate(String code, String scriptName, Map<String, Object> context, Class<T> returnType) {
        return JsEngine.evaluate(STANDARD_FUNCTIONS+"\n"+code, context, scriptName, returnType);
    }

    public static boolean evaluateBoolean(String code, Map<String, Object> ctx, String scriptName) {
        return evaluateBoolean(code, ctx, scriptName, false);
    }

    public static boolean evaluateBoolean(String code, Map<String, Object> ctx, String scriptName, boolean defaultValue) {
        try {
            return JsEngine.evaluateBoolean(STANDARD_FUNCTIONS + code, ctx, scriptName);
        } catch (Exception e) {
            log.warn("evaluateBoolean: returning "+defaultValue+" due to exception:"+e);
            return defaultValue;
        }
    }

    public static Integer evaluateInt(String code, Map<String, Object> ctx, String scriptName) {
        return JsEngine.evaluateInt(STANDARD_FUNCTIONS+code, ctx, scriptName);
    }

    public static Long evaluateLong(String code, Map<String, Object> ctx, String scriptName) {
        return JsEngine.evaluateLong(STANDARD_FUNCTIONS+code, ctx, scriptName);
    }

    public static String evaluateString(String condition, String scriptName, Map<String, Object> ctx) {
        final Object rval = JsEngine.evaluate(condition, ctx, scriptName, Object.class);
        if (rval == null) return null;

        if (rval instanceof String) return rval.toString();
        if (rval instanceof Number) {
            if (rval.toString().endsWith(".0")) return ""+((Number) rval).longValue();
            return rval.toString();
        }
        return rval.toString();
    }

    private static final String ESC_DOLLAR = "__ESCAPED_DOLLAR_SIGN__";
    public static String replaceDollarSigns(String val) {
        return val.replace("'$", ESC_DOLLAR)
                .replaceAll("(\\$(\\d+(\\.\\d{2})?))", "($2 * 100)")
                .replace(ESC_DOLLAR, "'$");
    }

    public static String round(String value, String script) {
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("x", value);
        try {
            return String.valueOf(evaluateInt(script, ctx, "round_" + script));
        } catch (Exception e) {
            log.warn("round('"+value+"', '"+script+"', NOT rounding due to exception: "+e);
            return value;
        }
    }
}
