package org.cobbzilla.util.collection;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.cobbzilla.util.daemon.ZillaRuntime.notSupported;
import static org.cobbzilla.util.reflect.ReflectionUtil.getTypeParam;

@Accessors(chain=true)
public abstract class Mappy<K, V, C extends Collection<V>> implements Map<K, V> {

    private ConcurrentHashMap<K, C> map = new ConcurrentHashMap<>();

    @Getter(lazy=true) private final Class<C> valueClass = initValueClass();
    private Class<C> initValueClass() { return getTypeParam(getClass(), 2); }

    protected abstract C newCollection();

    @Override public int size() { return map.size(); }

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

    @Override public void clear() { map.clear(); }

    @Override public Set<K> keySet() { return map.keySet(); }

    // todo
    @Override public Collection<V> values() { return notSupported(); }
    @Override public Set<Entry<K, V>> entrySet() { return notSupported(); }

    public Collection<C> allValues() { return map.values(); }
    public Set<Entry<K, C>> allEntrySets() { return map.entrySet(); }

}
