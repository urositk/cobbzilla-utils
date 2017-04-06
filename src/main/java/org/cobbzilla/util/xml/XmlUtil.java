package org.cobbzilla.util.xml;

import lombok.AllArgsConstructor;
import org.atteo.xmlcombiner.XmlCombiner;
import org.cobbzilla.util.string.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.system.Bytes.KB;

public class XmlUtil {

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
                .replaceAll("<\\s*"+fromElement+"([^>]*)>", "<"+toElement+"$1>")
                .replaceAll("</\\s*"+fromElement+"\\s*>", "</"+toElement+">")
                .replaceAll("<\\s*"+fromElement+"\\s*/>", "<"+toElement+"/>");
    }

    public static Element textElement(Document doc, String element, String text) {
        final Element node = doc.createElement(element);
        node.appendChild(doc.createTextNode(text));
        return node;
    }

    public static Document readDocument (String xml) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);
        } catch (Exception e) {
            return die("readDocument: "+e, e);
        }
    }

    public static void writeDocument (Document doc, Writer writer) {
        try {
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (Exception e) {
            die("writeDocument: " + e, e);
        }
    }

    public static String writeDocument(Document doc) {
        final StringWriter writer = new StringWriter();
        writeDocument(doc, writer);
        return writer.getBuffer().toString();
    }

    public static Node applyRecursively (Element element, XmlElementFunction func) {
        func.apply(element);
        final NodeList childNodes = element.getChildNodes();
        if (childNodes != null && childNodes.getLength() > 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node item = childNodes.item(i);
                if (item instanceof Element) applyRecursively((Element) item, func);
            }
        }
        return element;
    }

    public static List<Element> findElements(Document doc, final String name) {
        final List<Element> found = new ArrayList<>();
        applyRecursively(doc.getDocumentElement(), new MatchNodeName(name, found));
        return found;
    }

    public static List<Element> findElements(Element element, final String name) {
        final List<Element> found = new ArrayList<>();
        applyRecursively(element, new MatchNodeName(name, found));
        return found;
    }

    public static Element findUniqueElement(Document doc, String name) {
        final List<Element> elements = findElements(doc, name);
        if (empty(elements)) return null;
        if (elements.size() > 1) return die("add: multiple "+name+" elements found");
        return elements.get(0);
    }

    public static Element findFirstElement(Document doc, String name) {
        final List<Element> elements = findElements(doc, name);
        return empty(elements) ? null : elements.get(0);
    }

    @AllArgsConstructor
    public static class MatchNodeName implements XmlElementFunction {
        private final String name;
        private final List<Element> found;
        @Override public void apply(Element element) {
            if (element != null && !empty(element.getNodeName()) && element.getNodeName().equalsIgnoreCase(name)) {
                found.add(element);
            }
        }
    }
}
