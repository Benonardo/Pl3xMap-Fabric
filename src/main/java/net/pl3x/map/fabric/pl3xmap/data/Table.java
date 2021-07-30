package net.pl3x.map.fabric.pl3xmap.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public class Table<T> {
    private final Map<Integer, Map<Integer, T>> map = new HashMap<>();

    public T get(int x, int z) {
        Map<Integer, T> map = this.map.get(x);
        if (map == null) {
            return null;
        }
        return map.get(z);
    }

    public T put(int x, int z, T value) {
        Map<Integer, T> map = this.map.get(x);
        if (map == null) {
            map = new HashMap<>();
        }
        T old = map.put(z, value);
        this.map.put(x, map);
        return old;
    }

    public T remove(int x, int z) {
        Map<Integer, T> map = this.map.get(x);
        if (map != null) {
            T old = map.remove(z);
            this.map.put(x, map);
            return old;
        }
        return null;
    }

    public Collection<T> values() {
        Collection<T> values = new HashSet<>();
        this.map.values().forEach(map -> values.addAll(map.values()));
        return values;
    }

    public boolean contains(int x, int z) {
        return get(x, z) != null;
    }

    public void clear() {
        this.map.clear();
    }
}
