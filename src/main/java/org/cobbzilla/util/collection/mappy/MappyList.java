package org.cobbzilla.util.collection.mappy;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class MappyList<K, V> extends Mappy<K, V, List<V>> {

    public MappyList (int size) { super(size); }

    @Override protected List<V> newCollection() { return new ArrayList<>(); }

    @Override protected V firstInCollection(List<V> collection) { return collection.get(0); }

}
