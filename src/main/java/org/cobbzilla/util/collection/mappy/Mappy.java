package org.cobbzilla.util.collection.mappy;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.cobbzilla.util.reflect.ReflectionUtil.getTypeParam;

@Accessors(chain=true)
public abstract class Mappy<K, V, C extends Collection<V>> implements Map<K, V> {

    private final ConcurrentHashMap<K, C> map = new ConcurrentHashMap<>();

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
        final C collection = getAll((K) key);
        return collection.isEmpty() ? null : firstInCollection(collection);
    }

    protected V firstInCollection(C collection) { return collection.iterator().next(); }

    public C getAll (K key) {
        C collection = map.get(key);
        if (collection == null) {
            collection = newCollection();
            map.put(key, collection);
        }
        return collection;
    }

    @Override public V put(K key, V value) {
        synchronized (map) {
            C group = map.get(key);
            if (group == null) {
                group = newCollection();
                map.put(key, group);
            }
            group.add(value);
        }
        return null;
    }

    @Override public V remove(Object key) {
        final C group = map.remove(key);
        if (group == null || group.isEmpty()) return null; // empty case should never happen, but just in case
        return group.iterator().next();
    }

    @Override public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public void putAll(K key, Collection<V> values) {
        synchronized (map) {
            C collection = getAll(key);
            if (collection == null) collection = newCollection();
            collection.addAll(values);
            map.put(key, collection);
        }
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

    public List<V> flattenToValues() {
        final List<V> values = new ArrayList<>();
        for (C collection : allValues()) values.addAll(collection);
        return values;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Mappy other = (Mappy) o;

        if (size() != other.size()) return false;

        for (K key : keySet()) {
            if (!other.containsKey(key)) return false;
            final Collection otherValues = other.getAll(key);
            final Collection thisValues = getAll(key);
            if (otherValues.size() != thisValues.size()) return false;
            for (Object value : thisValues) {
                if (!otherValues.contains(value)) return false;
            }
        }
        return true;
    }

    @Override public int hashCode() {
        int result = new Integer(totalSize()).hashCode();
        result = 31 * result + (valueClass != null ? valueClass.hashCode() : 0);
        for (K key : keySet()) {
            result = 31 * result + (key.hashCode() + 13);
            for (V value : getAll(key)) {
                result = 31 * result + (value == null ? 0 : value.hashCode());
            }
        }
        return result;
    }

    @Override public String toString() {
        final StringBuilder b = new StringBuilder();
        for (K key : keySet()) {
            if (b.length() > 0) b.append(" | ");
            b.append(key).append("->(").append(StringUtil.toString(getAll(key), ", ")).append(")");
        }
        return "{"+b.toString()+"}";
    }
}
