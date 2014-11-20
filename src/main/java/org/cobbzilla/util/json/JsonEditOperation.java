package org.cobbzilla.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;

import java.io.IOException;

import static org.cobbzilla.util.json.JsonUtil.FULL_MAPPER;

@Accessors(chain=true)
public class JsonEditOperation {

    @Getter @Setter private JsonEditOperationType type;
    @Getter @Setter private String path;
    @Getter @Setter private String json;

    public boolean isRead() { return type == JsonEditOperationType.read; }

    public JsonNode getNode () throws IOException { return FULL_MAPPER.readTree(json); }

    public boolean hasIndex () { return getIndex() != null; }

    public boolean isEmptyBrackets () {
        int bracketPos = path.indexOf("[");
        int bracketClosePos = path.indexOf("]");
        return bracketPos != -1 && bracketClosePos != -1 && bracketClosePos == bracketPos+1;
    }

    public Integer getIndex() {
        int dotPos = path.lastIndexOf(".");
        if (dotPos == -1) return index(path);
        return index(path.substring(dotPos + 1));
    }

    private Integer index(String path) {
        try {
            int bracketPos = path.indexOf("[");
            int bracketClosePos = path.indexOf("]");
            if (bracketPos != -1 && bracketClosePos != -1 && bracketClosePos > bracketPos) {
                return new Integer(path.substring(bracketPos + 1, bracketClosePos));
            }
        } catch (Exception ignored) {}
        return null;
    }

    public String getName() {
        int dotPos = path.lastIndexOf(".");
        if (dotPos == -1) return path;
        return path.substring(dotPos+1);
    }

    public String getParentPath() {
        if (StringUtil.empty(path)) return null;
        final int dotPos = path.lastIndexOf(".");
        return dotPos == -1 ? null : path.substring(0, dotPos);
    }

}
