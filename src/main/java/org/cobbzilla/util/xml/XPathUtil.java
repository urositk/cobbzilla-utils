package org.cobbzilla.util.xml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;

@Slf4j @NoArgsConstructor @AllArgsConstructor
public class XPathUtil {

    @Getter @Setter private Collection<String> pathExpressions;
    @Getter @Setter private boolean useTidy = true;

    public XPathUtil (String expr) { this(new String[] { expr }, true); }
    public XPathUtil (String expr, boolean useTidy) { this(new String[] { expr }, useTidy); }

    public XPathUtil(String[] exprs) { this(Arrays.asList(exprs), true); }
    public XPathUtil(String[] exprs, boolean useTidy) { this(Arrays.asList(exprs), useTidy); }

    public List<Node> getFirstMatchList(InputStream in) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        return applyXPaths(in).values().iterator().next();
    }

    public Node getFirstMatch(InputStream in) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        return getFirstMatchList(in).get(0);
    }

    public String getFirstMatchText (InputStream in) throws ParserConfigurationException, TransformerException, SAXException, IOException {
        return getFirstMatch(in).getTextContent();
    }

    public Map<String, List<Node>> applyXPaths(InputStream in) throws ParserConfigurationException, IOException, SAXException, TransformerException {

        final Map<String, List<Node>> allFound = new HashMap<>();
        InputStream inStream = in;
        if (useTidy) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TidyUtil.parse(in, out, true);
            inStream = new ByteArrayInputStream(out.toByteArray());
        }

        final InputSource inputSource = new InputSource(inStream);
        final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(false);
        dfactory.setValidating(false);
        // dfactory.setExpandEntityReferences(true);
        final DocumentBuilder documentBuilder = dfactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new CommonEntityResolver());
        final Document doc = documentBuilder.parse(inputSource);

        // Use the simple XPath API to select a nodeIterator.
        // System.out.println("Querying DOM using "+pathExpression);
        for (String xpath : this.pathExpressions) {
            final List<Node> found = new ArrayList<>();
            NodeIterator nl = XPathAPI.selectNodeIterator(doc, xpath);

            // Serialize the found nodes to System.out.
            // System.out.println("<output>");
            Node n;
            while ((n = nl.nextNode())!= null) {
                if (isTextNode(n)) {
                    // DOM may have more than one node corresponding to a
                    // single XPath text node.  Coalesce all contiguous text nodes
                    // at this level
                    StringBuilder sb = new StringBuilder(n.getNodeValue());
                    for (
                            Node nn = n.getNextSibling();
                            isTextNode(nn);
                            nn = nn.getNextSibling()
                            ) {
                        sb.append(nn.getNodeValue());
                    }
                    Text textNode = doc.createTextNode(sb.toString());
                    found.add(textNode);

                } else {
                    found.add(n);
                    // serializer.transform(new DOMSource(n), new StreamResult(new OutputStreamWriter(System.out)));
                }
                // System.out.println();
            }
            // System.out.println("</output>");
            allFound.put(xpath, found);
        }
        return allFound;
    }

    /** Decide if the node is text, and so must be handled specially */
    public static boolean isTextNode(Node n) {
        if (n == null) return false;
        short nodeType = n.getNodeType();
        return nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE;
    }
}
