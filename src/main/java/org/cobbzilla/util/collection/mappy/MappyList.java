package org.cobbzilla.util.collection.mappy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NoArgsConstructor;
import org.cobbzilla.util.json.JsonUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.cobbzilla.util.json.JsonUtil.json;

@NoArgsConstructor
public class MappyList<K, V> extends Mappy<K, V, List<V>> {

    protected Integer subSize;

    public MappyList (int size) { super(size); }

    public MappyList (int size, int subSize) { super(size); this.subSize = subSize; }

    @Override protected List<V> newCollection() { return subSize != null ? new ArrayList<V>(subSize) : new ArrayList<V>(); }

    @Override protected V firstInCollection(List<V> collection) { return collection.get(0); }

}
