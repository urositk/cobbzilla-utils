package org.cobbzilla.util.handlebars;

import com.github.jknack.handlebars.io.AbstractTemplateLoader;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class StringTemplateLoader extends AbstractTemplateLoader {

    private String sourceName = "unknown";

    @Override public TemplateSource sourceAt(String source) throws IOException {
        return new StringTemplateSource(sourceName, source);
    }

}
