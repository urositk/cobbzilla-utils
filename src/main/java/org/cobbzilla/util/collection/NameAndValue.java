package org.cobbzilla.util.collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.cobbzilla.util.javascript.JsEngineDriver;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true) @ToString
public class NameAndValue {

    @Getter @Setter private String name;
    public boolean hasName () { return !empty(name); }
    @JsonIgnore public boolean getHasName () { return !empty(name); }

    @Getter @Setter private String value;
    public boolean hasValue () { return !empty(value); }
    @JsonIgnore public boolean getHasValue () { return !empty(value); }

    public static NameAndValue[] evaluate (NameAndValue[] pairs, Map<String, Object> context) {
        return evaluate(pairs, context, StandardJsEngine.DRIVER);
    }

    public static NameAndValue[] evaluate (NameAndValue[] pairs, Map<String, Object> context, JsEngineDriver engineDriver) {

        if (empty(context) || empty(pairs)) return pairs;

        final NameAndValue[] results = new NameAndValue[pairs.length];
        for (int i=0; i<pairs.length; i++) {
            final boolean isCode = pairs[i].getHasValue() && pairs[i].getValue().trim().startsWith("@");
            if (isCode) {
                results[i] = new NameAndValue(pairs[i].getName(), engineDriver.evaluate(pairs[i].getValue().trim().substring(1), context, String.class));
            } else {
                results[i] = pairs[i];
            }
        }

        return results;
    }

}
