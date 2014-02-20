package org.cobbzilla.util.collection;

import java.util.*;

public class MapUtil {

    public static Map<String, String> toMap (Properties props) {
        if (props == null || props.isEmpty()) return Collections.emptyMap();
        final Map<String, String> map = new LinkedHashMap<>(props.size());
        for (String name : props.stringPropertyNames()) map.put(name, props.getProperty(name));
        return map;
    }

}
