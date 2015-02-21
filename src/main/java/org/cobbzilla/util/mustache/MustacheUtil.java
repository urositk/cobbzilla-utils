package org.cobbzilla.util.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MustacheUtil {

    public static final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public static Map<String, Mustache> mustacheCache = new ConcurrentHashMap<>();

    public static Mustache getMustache(String value) {
        Mustache m = mustacheCache.get(value);
        if (m == null) {
            m = mustacheFactory.compile(new StringReader(value), value);
            mustacheCache.put(value, m);
        }
        return m;
    }

    public static String render(String value, Map<String, Object> scope) {
        final StringWriter w = new StringWriter();
        final Mustache mustache = MustacheUtil.getMustache(value);
        mustache.execute(w, scope);
        return w.toString();
    }

}
