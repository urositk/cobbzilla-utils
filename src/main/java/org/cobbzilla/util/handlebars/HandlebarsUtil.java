package org.cobbzilla.util.handlebars;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.AbstractTemplateLoader;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import lombok.AllArgsConstructor;
import lombok.Cleanup;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@AllArgsConstructor
public class HandlebarsUtil extends AbstractTemplateLoader {

    private String sourceName = "unknown";

    public static Map<String, Object> apply(Handlebars handlebars, Map<String, Object> map, Map<String, Object> ctx) {
        final Map<String, Object> merged = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof String) {
                final String val = (String) value;
                if (val.contains("{{") && val.contains("}}")) {
                    merged.put(entry.getKey(), apply(handlebars, value.toString(), ctx));
                } else {
                    merged.put(entry.getKey(), entry.getValue());
                }

            } else if (value instanceof Map) {
                merged.put(entry.getKey(), apply(handlebars, (Map<String, Object>) value, ctx));

            } else {
                merged.put(entry.getKey(), entry.getValue());
            }
        }
        return merged;
    }

    public static String apply(Handlebars handlebars, String value, Map<String, Object> ctx) {
        try {
            @Cleanup final StringWriter writer = new StringWriter(value.length());
            handlebars.compile(value).apply(ctx, writer);
            return writer.toString();

        } catch (Exception e) {
            return die("apply: "+e, e);
        }
    }

    @Override public TemplateSource sourceAt(String source) throws IOException {
        return new StringTemplateSource(sourceName, source);
    }

}
