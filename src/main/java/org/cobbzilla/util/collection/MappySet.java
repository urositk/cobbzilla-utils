package org.cobbzilla.util.collection;

import java.util.HashSet;
import java.util.Set;

public class MappySet<K, V> extends Mappy<K, V, Set<V>> {

    @Override protected Set<V> newCollection() { return new HashSet<>(); }

}

