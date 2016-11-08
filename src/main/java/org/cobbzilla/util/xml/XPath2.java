package org.cobbzilla.util.xml;

import lombok.Getter;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class XPath2 {

    @Getter(lazy=true) private static final XPathFactory xpathFactory = initXpathFactory();
    private static XPathFactory initXpathFactory() {
        try {
            sysinit();
            return XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
        } catch (Exception e) {
            return die("initXpathFactory: "+e, e);
        }
    }

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static void sysinit () {
        synchronized (initialized) {
            if (!initialized.get()) {
                final String name = "javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON;
                System.setProperty(name, "net.sf.saxon.xpath.XPathFactoryImpl");
                initialized.set(true);
            }
        }
    }

    public static class Path {

        @Getter private XPathExpression expr;

        public Path (String xpath) {
            try {
                expr = getXpathFactory().newXPath().compile(xpath);
            } catch (Exception e) {
                die("XPath2.Path: "+e, e);
            }
        }

        public String firstMatch (String xml) {  return firstMatch(new Doc(xml)); }

        public String firstMatch(Doc doc) {
            try {
                final List matches = (List) expr.evaluate(doc.getDoc(), XPathConstants.NODESET);
                if (empty(matches) || matches.get(0) == null) return null;
                final NodeInfo line = (NodeInfo) matches.get(0);
                return line.iterate().next().getStringValue();

            } catch (Exception e) {
                return die("firstMatch: "+e, e);
            }
        }
    }

    public static class Doc {
        @Getter private TreeInfo doc;
        public Doc (String xml) {
            final InputSource is = new InputSource(new ByteArrayInputStream(xml.getBytes()));
            final SAXSource ss = new SAXSource(is);
            final Configuration config = ((XPathFactoryImpl) getXpathFactory()).getConfiguration();
            try {
                doc = config.buildDocumentTree(ss);
            } catch (Exception e) {
                die("XPath2.Doc: "+e, e);
            }
        }
    }

}
