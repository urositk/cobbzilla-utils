package org.cobbzilla.util.xml;

import org.cobbzilla.util.collection.mappy.MappyList;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;

public class TidyHandlebarsSpanMerger implements TidyHelper {

    public static final TidyHandlebarsSpanMerger instance = new TidyHandlebarsSpanMerger();

    @Override public void process(Document doc) {
        final MappyList<Node, Node> toRemove = new MappyList<>();
        mergeSpans(doc, toRemove);
        for (Map.Entry<Node, Node> n : toRemove.entrySet()) {
            n.getKey().removeChild(n.getValue());
        }
    }

    protected void mergeSpans(Node parent, MappyList<Node, Node> toRemove) {

        Node spanStart = null;
        StringBuilder spanTemp = null;
        NodeList childNodes = parent.getChildNodes();
        for (int i=0; i <childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equalsIgnoreCase("span")) {
                    if (spanStart == null) {
                        spanStart = child;
                        spanTemp = new StringBuilder(collectText(child));

                    } else if (sameAttrs(spanStart.getAttributes(), child.getAttributes())) {
                        //noinspection ConstantConditions
                        append(spanTemp, collectText(child));
                        spanStart.getFirstChild().setNodeValue(spanTemp.toString());
                        toRemove.put(parent, child);
                    } else {
                        spanStart = child;
                        spanTemp = new StringBuilder(collectText(child));
                    }
                } else if (child.hasChildNodes()) {
                    mergeSpans(child, toRemove);
                }
                continue;

            } else if (child.getNodeType() == Node.TEXT_NODE) {
                if (spanTemp != null) {
                    append(spanTemp, child.getNodeValue());
                    spanStart.getFirstChild().setNodeValue(spanTemp.toString());
                    toRemove.put(parent, child);
                    continue;
                }
            }
            if (child.hasChildNodes()) {
                mergeSpans(child, toRemove);
            }
        }
        if (spanStart != null && spanTemp != null && spanTemp.length() > 0) {
            spanStart.getFirstChild().setNodeValue(spanTemp.toString());
        }
    }

    private StringBuilder append(StringBuilder b, String s) {
        if (s == null || s.length() == 0) return b;
        return b.append(s);
    }

    private String collectText(Node node) {
        final StringBuilder b = new StringBuilder();
        if (node.hasChildNodes()) {
            final NodeList childNodes = node.getChildNodes();
            for (int i=0; i<childNodes.getLength(); i++) {
                final Node child = childNodes.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    b.append(child.getNodeValue());
                }
            }
        }
        return b.toString();
    }


    private boolean sameAttrs(NamedNodeMap a1, NamedNodeMap a2) {
        if (a1.getLength() != a2.getLength()) return false;
        for (int i=0; i<a1.getLength(); i++) {
            boolean found = false;
            final Node a1item = a1.item(i);
            for (int j=0; j<a2.getLength(); j++) {
                if (a1item.getNodeName().equalsIgnoreCase(a2.item(j).getNodeName())
                        && a1item.getNodeValue().equalsIgnoreCase(a2.item(j).getNodeValue())) {
                    found = true; break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
}
