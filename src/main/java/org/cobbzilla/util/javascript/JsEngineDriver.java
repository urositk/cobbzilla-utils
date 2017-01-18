package org.cobbzilla.util.javascript;

import java.util.Map;

import static org.cobbzilla.util.security.ShaUtil.sha256_hex;

public abstract class JsEngineDriver {

    public abstract <T> T evaluate(String code, Map<String, Object> context, String scriptName, Class<T> returnType);
    public abstract boolean evaluateBoolean(String code, Map<String, Object> ctx, String scriptName);
    public abstract Integer evaluateInt(String code, Map<String, Object> ctx, String scriptName);
    public abstract Long evaluateLong(String code, Map<String, Object> ctx, String scriptName);

    public <T> T evaluate(String code, Map<String, Object> context, Class<T> returnType) {
        return evaluate(code, context, "evaluate_"+ sha256_hex(code), returnType);
    }
    public boolean evaluateBoolean(String code, Map<String, Object> ctx) {
        return evaluateBoolean(code, ctx, "evaluateBoolean_"+ sha256_hex(code));
    }
    public Integer evaluateInt(String code, Map<String, Object> ctx) {
        return evaluateInt(code, ctx, "evaluateInt_"+ sha256_hex(code));
    }
    public Long evaluateLong(String code, Map<String, Object> ctx) {
        return evaluateLong(code, ctx, "evaluateLong_"+ sha256_hex(code));
    }

}
