package org.cobbzilla.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.json.JsonUtil.*;

/**
 * Facilitates editing JSON files.
 *
 * Notes:
 *  - only one read operation can be specified. the 'edit' method will return the value of the first read operation processed.
 *  - if you 'replace' a node that does not exist, it will be created
 */
@Accessors(chain=true)
public class JsonEdit {

    @Getter @Setter private InputStream jsonStream;
    @Getter @Setter private List<JsonEditOperation> operations = new ArrayList<>();

    public JsonEdit addOperation (JsonEditOperation operation) { operations.add(operation); return this; }

    public String edit () throws Exception {

        FULL_MAPPER.getFactory().enable(JsonParser.Feature.ALLOW_COMMENTS);

        JsonNode root = FULL_MAPPER.readTree(jsonStream);
        for (JsonEditOperation operation : operations) {
            if (operation.isRead()) return toString(JsonUtil.findNode(root, operation.getPath()));
            root = apply(root, operation);
        }
        return toString(FULL_MAPPER.treeToValue(root, Object.class));
    }

    private String toString(Object node) throws JsonProcessingException {
        return node == null ? null : FULL_MAPPER.writeValueAsString(node);
    }

    private JsonNode apply(JsonNode root, JsonEditOperation operation) throws IOException {
        final List<JsonNode> path = findNodePath(root, operation.getPath());

        switch (operation.getType()) {
            case write:
                root = write(root, path, operation);
                break;

            case delete:
                delete(path, operation);
                break;

            default: throw new IllegalArgumentException("unsupported operation: "+operation.getType());
        }
        return root;
    }

    private JsonNode write(JsonNode root, List<JsonNode> path, JsonEditOperation operation) throws IOException {

        JsonNode current = path.get(path.size()-1);
        final JsonNode parent = path.size() > 1 ? path.get(path.size()-2) : null;
        final JsonNode data = operation.getNode();

        if (current == MISSING) {
            // replacing root with single object node
            return addToParent(root, operation, parent, data);
        }

        if (current instanceof ObjectNode) {
            final ObjectNode node = (ObjectNode) current;
            if (data instanceof ObjectNode) {
                final Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
                while (fields.hasNext()) {
                    final Map.Entry<String, JsonNode> entry = fields.next();
                    node.set(entry.getKey(), entry.getValue());
                }
            } else {
                return addToParent(root, operation, parent, data);
            }

        } else if (current instanceof ArrayNode) {
            final ArrayNode node = (ArrayNode) current;
            if (operation.hasIndex()) {
                node.set(operation.getIndex(), operation.getNode());
            } else {
                node.add(operation.getNode());
            }

        } else if (current instanceof ValueNode) {
            if (parent == null) return new ObjectNode(FULL_MAPPER.getNodeFactory()).set(operation.getName(), data);

            // overwrite value node at location
            addToParent(root, operation, parent, data);

        } else {
            throw new IllegalArgumentException("Cannot append to node (is a "+current.getClass().getName()+"): "+current);
        }

        return root;
    }

    private JsonNode addToParent(JsonNode root, JsonEditOperation operation, JsonNode parent, JsonNode data) {

        if (parent == null) return new ObjectNode(FULL_MAPPER.getNodeFactory()).set(operation.getName(), data);

        if (parent instanceof ObjectNode) {
            ((ObjectNode) parent).set(operation.getName(), data);
        } else if (parent instanceof ArrayNode) {
            ((ArrayNode) parent).set(operation.getIndex(), data);
        } else {
            throw new IllegalArgumentException("Cannot append to node (is a "+parent.getClass().getName()+"): "+parent);
        }

        return root;
    }

    private void delete(List<JsonNode> path, JsonEditOperation operation) throws IOException {
        if (path.size() < 2) throw new IllegalArgumentException("Cannot delete root");
        final JsonNode parent = path.get(path.size()-2);

        if (parent instanceof ArrayNode) {
            ((ArrayNode) parent).remove(operation.getIndex());

        } else if (parent instanceof ObjectNode) {
            ((ObjectNode) parent).remove(operation.getName());

        } else {
            throw new IllegalArgumentException("Cannot remove node (parent is a "+parent.getClass().getName()+")");
        }

    }

}
