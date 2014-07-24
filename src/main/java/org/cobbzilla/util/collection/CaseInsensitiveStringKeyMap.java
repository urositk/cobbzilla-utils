package org.cobbzilla.util.collection;

import java.util.concurrent.ConcurrentHashMap;

public class CaseInsensitiveStringKeyMap<V> extends ConcurrentHashMap<String, V> {

    public String key(Object key) { return key == null ? null : key.toString().toLowerCase(); }

    @Override public V get(Object key) { return super.get(key(key)); }

    @Override public boolean containsKey(Object key) { return super.containsKey(key(key)); }

    @Override public V put(String key, V value) { return super.put(key(key), value); }

    @Override public V putIfAbsent(String key, V value) { return super.putIfAbsent(key(key), value); }

    @Override public V remove(Object key) { return super.remove(key(key)); }

    @Override public boolean remove(Object key, Object value) { return super.remove(key(key), value); }

    @Override public boolean replace(String key, V oldValue, V newValue) { return super.replace(key(key), oldValue, newValue); }

    @Override public V replace(String key, V value) { return super.replace(key(key), value); }

}
