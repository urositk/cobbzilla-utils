package org.cobbzilla.util.collection;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.cobbzilla.util.reflect.ReflectionUtil.getTypeParam;

@Accessors(chain=true)
public abstract class Mappy<K, V, C extends Collection<V>> implements Map<K, V> {

    private ConcurrentHashMap<K, C> map = new ConcurrentHashMap<>();

    @Getter(lazy=true) private final Class<C> valueClass = initValueClass();
    private Class<C> initValueClass() { return getTypeParam(getClass(), 2); }

    protected abstract C newCollection();

    @Override public int size() { return map.size(); }

    public int totalSize () {
        int count = 0;
        for (Collection<V> c : allValues()) count += c.size();
        return count;
    }

    @Override public boolean isEmpty() { return map.isEmpty(); }

    @Override public boolean containsKey(Object key) { return map.containsKey(key); }

    @Override public boolean containsValue(Object value) { return map.containsValue(value); }

    @Override public V get(Object key) {
        C group = map.get(key);
        if (group == null) {
            group = newCollection();
            map.put((K) key, group);
            return null;
        }
        return group.iterator().next();
    }

    @Override public V put(K key, V value) {
        C group = map.get(key);
        if (group == null) {
            group = newCollection();
            map.put(key, group);
        }
        group.add(value);
        return null;
    }

    @Override public V remove(Object key) {
        C group = map.remove(key);
        return group == null ? null : group.iterator().next();
    }

    @Override public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public void putAll(K key, Collection<V> values) {
        for (V value : values) put(key, value);
    }

    @Override public void clear() { map.clear(); }

    @Override public Set<K> keySet() { return map.keySet(); }

    @Override public Collection<V> values() {
        final List<V> vals = new ArrayList<>();
        for (C collection : map.values()) vals.addAll(collection);
        return vals;
    }
    @Override public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> entries = new HashSet<>();
        for (Entry<K, C> entry : map.entrySet()) {
            for (V item : entry.getValue()) {
                entries.add(new AbstractMap.SimpleEntry<K, V>(entry.getKey(), item));
            }
        }
        return entries;
    }

    public Collection<C> allValues() { return map.values(); }
    public Set<Entry<K, C>> allEntrySets() { return map.entrySet(); }

}
