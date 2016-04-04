package org.cobbzilla.util.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.cobbzilla.util.io.FileSuffixFilter;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.io.FilenameSuffixFilter;
import org.cobbzilla.util.io.StreamUtil;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class JsonUtil {

    public static final String EMPTY_JSON = "{}";
    public static final String EMPTY_JSON_ARRAY = "[]";

    public static final JsonNode MISSING = MissingNode.getInstance();

    public static final FileFilter JSON_FILES = new FileSuffixFilter(".json");
    public static final FilenameFilter JSON_FILENAMES = new FilenameSuffixFilter(".json");

    public static final ObjectMapper FULL_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    public static final ObjectWriter FULL_WRITER = FULL_MAPPER.writer();

    public static final ObjectMapper FULL_MAPPER_ALLOW_COMMENTS = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    static {
        FULL_MAPPER_ALLOW_COMMENTS.getFactory().enable(JsonParser.Feature.ALLOW_COMMENTS);
    }

    public static final ObjectMapper FULL_MAPPER_ALLOW_COMMENTS_AND_UNKNOWN_FIELDS = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    static {
        FULL_MAPPER_ALLOW_COMMENTS_AND_UNKNOWN_FIELDS.getFactory().enable(JsonParser.Feature.ALLOW_COMMENTS);
    }

    public static final ObjectMapper NOTNULL_MAPPER = FULL_MAPPER
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public static final ObjectMapper PUBLIC_MAPPER = buildMapper();

    public static final ObjectWriter PUBLIC_WRITER = buildWriter(PUBLIC_MAPPER, PublicView.class);

    public static ObjectMapper buildMapper() {
        return new ObjectMapper()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static ObjectWriter buildWriter(Class<? extends PublicView> view) {
        return buildMapper().writerWithView(view);
    }
    public static ObjectWriter buildWriter(ObjectMapper mapper, Class<? extends PublicView> view) {
        return mapper.writerWithView(view);
    }

    public static class PublicView {}

    public static String toJson (Object o) throws Exception {
        return JsonUtil.NOTNULL_MAPPER.writeValueAsString(o);
    }

    public static String toJsonOrDie (Object o) {
        try {
            return toJson(o);
        } catch (Exception e) {
            return die("toJson: exception writing object ("+o+"): "+e, e);
        }
    }

    public static String toJsonOrErr(Object o) {
        try {
            return toJson(o);
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static <T> T fromJson(InputStream json, Class<T> clazz) throws Exception {
        return fromJson(StreamUtil.toString(json), clazz);
    }

    public static <T> T fromJson(File json, Class<T> clazz) throws Exception {
        return fromJson(FileUtil.toString(json), clazz);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return JsonUtil.FULL_MAPPER.readValue(json, clazz);
    }

    public static <T> T fromJson(String json, JavaType type) throws Exception {
        return JsonUtil.FULL_MAPPER.readValue(json, type);
    }

    public static <T> T fromJsonOrDie(File json, Class<T> clazz) {
        return fromJsonOrDie(FileUtil.toStringOrDie(json), clazz);
    }

    public static <T> T fromJsonOrDie(String json, Class<T> clazz) {
        if (empty(json)) return null;
        try {
            return JsonUtil.FULL_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            return die("fromJsonOrDie: exception while reading: "+json+": "+e, e);
        }
    }

    public static <T> T fromJson(String json, String path, Class<T> clazz) throws Exception {
        return fromJson(FULL_MAPPER.readTree(json), path, clazz);
    }

    public static <T> T fromJson(File json, String path, Class<T> clazz) throws Exception {
        return fromJson(FULL_MAPPER.readTree(json), path, clazz);
    }

    public static <T> T fromJson(JsonNode node, String path, Class<T> clazz) throws Exception {
        node = findNode(node, path);
        return FULL_MAPPER.convertValue(node, clazz);
    }

    public static JsonNode findNode(JsonNode node, String path) throws IOException {
        if (node == null) return null;
        final List<JsonNode> nodePath = findNodePath(node, path);
        if (nodePath == null || nodePath.isEmpty()) return null;
        final JsonNode lastNode = nodePath.get(nodePath.size()-1);
        return lastNode == MISSING ? null : lastNode;
    }

    public static String toString(Object node) throws JsonProcessingException {
        return node == null ? null : FULL_MAPPER.writeValueAsString(node);
    }

    public static String nodeValue (JsonNode node, String path) throws IOException {
        return fromJsonOrDie(toString(findNode(node, path)), String.class);
    }

    public static List<JsonNode> findNodePath(JsonNode node, String path) throws IOException {

        final List<JsonNode> nodePath = new ArrayList<>();
        nodePath.add(node);
        if (empty(path)) return nodePath;
        final List<String> pathParts = tokenize(path);

        for (String pathPart : pathParts) {
            int index = -1;
            int bracketPos = pathPart.indexOf("[");
            int bracketClosePos = pathPart.indexOf("]");
            boolean isEmptyBrackets = false;
            if (bracketPos != -1 && bracketClosePos != -1 && bracketClosePos > bracketPos) {
                if (bracketClosePos == bracketPos+1) {
                    // ends with [], they mean to append
                    isEmptyBrackets = true;
                } else {
                    index = Integer.parseInt(pathPart.substring(bracketPos + 1, bracketClosePos));
                }
                pathPart = pathPart.substring(0, bracketPos);
            }
            node = node.get(pathPart);
            if (node == null) {
                nodePath.add(MISSING);
                return nodePath;
            }
            nodePath.add(node);
            if (index != -1) {
                node = node.get(index);
                nodePath.add(node);

            } else if (isEmptyBrackets) {
                nodePath.add(MISSING);
                return nodePath;
            }
        }
        return nodePath;
    }

    public static List<String> tokenize(String path) {
        final List<String> pathParts = new ArrayList<>();
        final StringTokenizer st = new StringTokenizer(path, ".'", true);
        boolean collectingQuotedToken = false;
        StringBuffer pathToken = new StringBuffer();
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            if (token.equals("'")) {
                collectingQuotedToken = !collectingQuotedToken;

            } else if (collectingQuotedToken) {
                pathToken.append(token);

            } else if (token.equals(".") && pathToken.length() > 0) {
                pathParts.add(pathToken.toString());
                pathToken = new StringBuffer();

            } else {
                pathToken.append(token);
            }
        }
        if (collectingQuotedToken) throw new IllegalArgumentException("Unterminated single quote in: "+path);
        if (pathToken.length() > 0) pathParts.add(pathToken.toString());
        return pathParts;
    }

    public static ObjectNode replaceNode(File file, String path, String replacement) throws Exception {
        return replaceNode((ObjectNode) FULL_MAPPER.readTree(file), path, replacement);
    }

    public static ObjectNode replaceNode(String json, String path, String replacement) throws Exception {
        return replaceNode((ObjectNode) FULL_MAPPER.readTree(json), path, replacement);
    }

    public static ObjectNode replaceNode(ObjectNode document, String path, String replacement) throws Exception {

        final String simplePath = path.contains(".") ? path.substring(path.lastIndexOf(".")+1) : path;
        Integer index = null;
        if (simplePath.contains("[")) {
            index = Integer.parseInt(simplePath.substring(simplePath.indexOf("[")+1, simplePath.indexOf("]")));
        }
        final List<JsonNode> found = findNodePath(document, path);
        if (found == null || found.isEmpty() || found.get(found.size()-1).equals(MISSING)) {
            throw new IllegalArgumentException("path not found: "+path);
        }

        final JsonNode parent = found.size() > 1 ? found.get(found.size()-2) : document;
        if (index != null) {
            final JsonNode origNode = ((ArrayNode) parent).get(index);
            ((ArrayNode) parent).set(index, getValueNode(origNode, path, replacement));
        } else {
            // what is the original node type?
            final JsonNode origNode = parent.get(simplePath);
            ((ObjectNode) parent).put(simplePath, getValueNode(origNode, path, replacement));
        }
        return document;
    }

    public static JsonNode getValueNode(JsonNode node, String path, String replacement) {
        final String nodeClass = node.getClass().getName();
        if ( ! (node instanceof ValueNode) ) die("Path "+path+" does not refer to a value (it is a "+ nodeClass +")");
        if (node instanceof TextNode) return new TextNode(replacement);
        if (node instanceof BooleanNode) return BooleanNode.valueOf(Boolean.parseBoolean(replacement));
        if (node instanceof IntNode) return new IntNode(Integer.parseInt(replacement));
        if (node instanceof LongNode) return new LongNode(Long.parseLong(replacement));
        if (node instanceof DoubleNode) return new DoubleNode(Double.parseDouble(replacement));
        if (node instanceof DecimalNode) return new DecimalNode(new BigDecimal(replacement));
        if (node instanceof BigIntegerNode) return new BigIntegerNode(new BigInteger(replacement));
        throw new IllegalArgumentException("Path "+path+" refers to an unsupported ValueNode: "+ nodeClass);
    }

    public static JsonNode getValueNode(Object data) {
        if (data == null) return NullNode.getInstance();
        if (data instanceof Integer) return new IntNode((Integer) data);
        if (data instanceof Boolean) return BooleanNode.valueOf((Boolean) data);
        if (data instanceof Long) return new LongNode((Long) data);
        if (data instanceof Float) return new DoubleNode((Float) data);
        if (data instanceof Double) return new DoubleNode((Double) data);
        if (data instanceof BigDecimal) return new DecimalNode((BigDecimal) data);
        if (data instanceof BigInteger) return new BigIntegerNode((BigInteger) data);
        throw new IllegalArgumentException("Cannot create value node from: "+data+" (type "+data.getClass().getName()+")");
    }

    public static JsonNode toNode (File f) { return fromJsonOrDie(FileUtil.toStringOrDie(f), JsonNode.class); }

}
