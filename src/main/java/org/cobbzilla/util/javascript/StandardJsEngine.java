package org.cobbzilla.util.javascript;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.security.ShaUtil.sha256_hex;

@Slf4j
public class StandardJsEngine extends JsEngine {

    private static final String STANDARD_FUNCTIONS

            // function to find element in array
            = "\nfunction found (item, arr) { return arr != null && arr.indexOf(''+item) != -1; }"

            // function to find the first object in array that matches field==value
            // field may contain embedded dots to navigate within each object element of the array
            + "\nfunction find (field, value, arr) {\n"
            + "    return arr == null ? null : arr.find(function (obj) {\n"
            + "      var target = obj;\n"
            + "      var path = field;\n"
            + "      var dotPos = path.indexOf('.');\n"
            + "      while (dotPos != -1) {\n"
            + "        var prop = path.substring(0, dotPos);\n"
            + "        if (!target.hasOwnProperty(prop)) return false;\n"
            + "        target = target[prop];\n"
            + "        path = path.substring(dotPos+1);\n"
            + "        var dotPos = path.indexOf('.');\n"
            + "      }\n"
            + "      return target.hasOwnProperty(path) && target[path] == value;\n"
            + "    });\n"
            + "}\n"

            // function to find the all object in array that match field==value
            // field may contain embedded dots to navigate within each object element of the array
            + "function find_all (field, value, arr) {\n"
            + "    var found = [];\n"
            + "    if (arr == null || arr.length == 0) return found;\n"
            + "    arr.find(function (obj) {\n"
            + "      var target = obj;\n"
            + "      var path = field;\n"
            + "      var dotPos = path.indexOf('.');\n"
            + "      while (dotPos != -1) {\n"
            + "        var prop = path.substring(0, dotPos);\n"
            + "        if (!target.hasOwnProperty(prop)) return false;\n"
            + "        target = target[prop];\n"
            + "        path = path.substring(dotPos+1);\n"
            + "        var dotPos = path.indexOf('.');\n"
            + "      }\n"
            + "      if (target.hasOwnProperty(path) && target[path] == value) {\n"
            + "        found.push(obj);\n"
            + "      }\n"
            + "    }); \n"
            + "    return found;\n"
            + "}\n"

            // functions for rounding up/down to nearest multiple
            + "\nfunction up (x, multiple) { return multiple * parseInt(Math.ceil(parseFloat(x)/parseFloat(multiple))); }"
            + "\nfunction down (x, multiple) { return multiple * parseInt(Math.floor(parseFloat(x)/parseFloat(multiple))); }"
            + "\n";

    public static <T> T evaluate(String code, Map<String, Object> context, Class<T> returnType) {
        return JsEngine.evaluate(STANDARD_FUNCTIONS+"\n"+code, context, "evaluate_"+sha256_hex(code), returnType);
    }

    public static boolean evaluateBoolean(String code, Map<String, Object> ctx) {
        return evaluateBoolean(code, ctx, false);
    }

    public static boolean evaluateBoolean(String code, Map<String, Object> ctx, boolean defaultValue) {
        try {
            return JsEngine.evaluateBoolean(STANDARD_FUNCTIONS + code, ctx, "evaluateBoolean_"+sha256_hex(code));
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
