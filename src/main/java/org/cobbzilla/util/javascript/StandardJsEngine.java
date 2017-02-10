package org.cobbzilla.util.javascript;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.io.StreamUtil.stream2string;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;
import static org.cobbzilla.util.string.StringUtil.getPackagePath;

@Slf4j
public class StandardJsEngine extends JsEngine {

    public static final JsEngineDriver DRIVER = new JsEngineDriver() {
        @Override public <T> T evaluate(String code, Map<String, Object> context, String scriptName, Class<T> returnType) {
            return StandardJsEngine.evaluate(code, context, scriptName, returnType);
        }

        @Override public boolean evaluateBoolean(String code, Map<String, Object> ctx, String scriptName) {
            return StandardJsEngine.evaluateBoolean(code, ctx, scriptName);
        }

        @Override public Integer evaluateInt(String code, Map<String, Object> ctx, String scriptName) {
            return StandardJsEngine.evaluateInt(code, ctx, scriptName);
        }

        @Override public Long evaluateLong(String code, Map<String, Object> ctx, String scriptName) {
            return StandardJsEngine.evaluateLong(code, ctx, scriptName);
        }
    };

    private static final String STANDARD_FUNCTIONS = stream2string(getPackagePath(StandardJsEngine.class)+"/standard_js_lib.js");

    public static <T> T evaluate(String code, Map<String, Object> context, Class<T> returnType) {
        return evaluate(code, context, "evaluate_"+sha256_hex(code), returnType);
    }

    public static <T> T evaluate(String code, Map<String, Object> context, String scriptName, Class<T> returnType) {
        return JsEngine.evaluate(STANDARD_FUNCTIONS+"\n"+code, context, scriptName, returnType);
    }

    public static boolean evaluateBooleanWithIncludes(String code, Map<String, Object> context) {
        return evaluateBooleanWithIncludes(code, context, "evaluateBooleanWithIncludes_"+sha256_hex(code));
    }

    public static boolean evaluateBooleanWithIncludes(String code, Map<String, Object> context, boolean defaultValue) {
        try {
            return evaluateBooleanWithIncludes(code, context, "evaluateBooleanWithIncludes_"+sha256_hex(code));
        } catch (Exception e) {
            log.debug("evaluateBooleanWithIncludes: returning "+defaultValue+" due to exception:"+e);
            return defaultValue;
        }
    }

    public static boolean evaluateBooleanWithIncludes(String code, Map<String, Object> context, String scriptName) {
        if (code.startsWith("!")) code = stream2string(code.substring(1));
        return evaluateBoolean(code, context, scriptName);
    }

    public static <T> T evaluateWithIncludes(String code, Map<String, Object> context, Class<T> returnType) {
        return evaluateWithIncludes(code, context, "evaluate_"+sha256_hex(code), returnType);
    }

    public static <T> T evaluateWithIncludes(String code, Map<String, Object> context, String scriptName, Class<T> returnType) {
        if (code.startsWith("!")) code = stream2string(code.substring(1));
        return evaluate(STANDARD_FUNCTIONS+"\n"+code, context, scriptName, returnType);
    }

    public static boolean evaluateBoolean(String code, Map<String, Object> ctx) {
        return evaluateBoolean(code, ctx, false);
    }

    public static boolean evaluateBoolean(String code, Map<String, Object> ctx, boolean defaultValue) {
        try {
            return JsEngine.evaluateBoolean(STANDARD_FUNCTIONS + code, ctx, "evaluateBoolean_"+sha256_hex(code));
        } catch (Exception e) {
            log.debug("evaluateBoolean: returning "+defaultValue+" due to exception:"+e);
            return defaultValue;
        }
    }

    public static Integer evaluateInt(String code, Map<String, Object> ctx) {
        return JsEngine.evaluateInt(STANDARD_FUNCTIONS+code, ctx, "evaluateInt_"+sha256_hex(code));
    }

    public static Long evaluateLong(String code, Map<String, Object> ctx) {
        return JsEngine.evaluateLong(STANDARD_FUNCTIONS+code, ctx, "evaluateLong_"+sha256_hex(code));
    }

    public static String evaluateString(String condition, Map<String, Object> ctx) {
        final Object rval = evaluate(condition, ctx, Object.class);
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
            return String.valueOf(evaluateInt(script, ctx));
        } catch (Exception e) {
            log.warn("round('"+value+"', '"+script+"', NOT rounding due to exception: "+e);
            return value;
        }
    }
}
