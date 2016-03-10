package org.cobbzilla.util.collection;

import java.util.ArrayList;
import java.util.List;

public class MappyList<K, V> extends Mappy<K, V, List<V>> {

    @Override protected List<V> newCollection() { return new ArrayList<>(); }

}
