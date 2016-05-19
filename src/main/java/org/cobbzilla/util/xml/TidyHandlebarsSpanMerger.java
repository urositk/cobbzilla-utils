package org.cobbzilla.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class TidyHandlebarsSpanMerger implements TidyHelper {

    public static final TidyHandlebarsSpanMerger instance = new TidyHandlebarsSpanMerger();

    @Override public void process(Document doc) { mergeSpans(doc); }

    protected void mergeSpans(Node parent) {
        Node firstSpan = null;
        NodeList childNodes = parent.getChildNodes();
        List<Node> toRemove = new ArrayList<Node>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equalsIgnoreCase("span") && child.hasChildNodes()) {
                for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                    final Node subChild = child.getChildNodes().item(j);
                    if (subChild.getNodeType() == Node.TEXT_NODE) {
                        final String textContent = subChild.getNodeValue();
                        if (firstSpan == null && textContent.startsWith("{{")) {
                            firstSpan = subChild;
                        } else if (firstSpan != null) {
                            firstSpan.setNodeValue(firstSpan.getNodeValue() + textContent);
                            toRemove.add(child);
                            if (textContent.endsWith("}}")) {
                                firstSpan = null;
                            }
                        }
                    }
                }
            }
        }
        for (Node n : toRemove) parent.removeChild(n);
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && !child.getNodeName().equalsIgnoreCase("span") && child.hasChildNodes()) {
                mergeSpans(child);
            }
        }
    }
}
