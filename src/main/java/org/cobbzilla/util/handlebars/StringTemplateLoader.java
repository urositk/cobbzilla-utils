package org.cobbzilla.util.handlebars;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.AbstractTemplateLoader;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import lombok.AllArgsConstructor;
import lombok.Cleanup;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@AllArgsConstructor
public class StringTemplateLoader extends AbstractTemplateLoader {

    private String sourceName = "unknown";

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
