package org.cobbzilla.util.xml;

import org.atteo.xmlcombiner.XmlCombiner;
import org.cobbzilla.util.string.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.system.Bytes.KB;

public class XmlMerger {

    public static OutputStream merge (OutputStream out, String... documents) {
        try {
            final XmlCombiner combiner = new XmlCombiner();
            for (String document : documents) {
                combiner.combine(StringUtil.stream(document));
            }
            combiner.buildDocument(out);
            return out;

        } catch (Exception e) {
            return die("merge: "+e, e);
        }
    }

    public static String merge (String... documents) { return merge((int) (32*KB), documents); }

    public static String merge (int bufsiz, String... documents) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufsiz);
        return merge(buffer, documents).toString();
    }

    public static String replaceElement (String document, String fromElement, String toElement) {
        return document
                .replace("<"+fromElement+">", "<"+toElement+">")
                .replace("</"+fromElement+">", "</"+toElement+">");
    }

}
