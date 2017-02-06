package org.cobbzilla.util.xml;

import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicInteger;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class ElementIdGenerator {

    private String prefix = "";
    private AtomicInteger counter;

    public ElementIdGenerator () { this(1); }
    public ElementIdGenerator (int start) { counter = new AtomicInteger(start); }
    public ElementIdGenerator (String prefix) { this(prefix, 1); }
    public ElementIdGenerator (String prefix, int start) { this.prefix = prefix; counter = new AtomicInteger(start); }

    public Element id(Element e) {
        if (empty(e.getAttribute("id"))) e.setAttribute("id", prefix+counter.getAndIncrement());
        return e;
    }

    public Element create(Document doc, String elementName) { return id(doc.createElement(elementName)); }

    public Element text(Document doc, String elementName, String text) { return text(doc, elementName, text, null); }

    public Element text(Document doc, String elementName, String text, Integer truncate) {
        if (truncate != null && text.trim().length() > truncate) text = text.trim().substring(0, truncate);
        final Element element = create(doc, elementName);
        element.appendChild(doc.createTextNode(text));
        return element;
    }

    @Getter(lazy=true) private final XmlElementFunction idFunction = initIdFunction();
    private XmlElementFunction initIdFunction() {
        return new XmlElementFunction() { @Override public void apply(Element element) { id(element); } };
    }

}
