package com.test.task.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Generic, thread-safe, read-optimized table cache backed by an immutable snapshot Map.
 *
 * Reads are lock-free; writers replace the snapshot atomically using CAS.
 * Key is extracted from value via {@code idExtractor}.
 */
public class TableCache<K, V> {

    private final AtomicReference<Map<K, V>> snapshotRef = new AtomicReference<>(Collections.emptyMap());
    private final Function<V, K> idExtractor;

    public TableCache(Function<V, K> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor, "idExtractor must not be null");
    }

    /**
     * Replace entire cache contents atomically with the provided collection of values.
     */
    public void refreshAll(Collection<V> values) {
        if (values == null || values.isEmpty()) {
            snapshotRef.set(Collections.emptyMap());
            return;
        }
        Map<K, V> fresh = new HashMap<>(Math.max(16, values.size() * 2));
        for (V value : values) {
            if (value == null) {
                continue;
            }
            K key = idExtractor.apply(value);
            if (key != null) {
                fresh.put(key, value);
            }
        }
        snapshotRef.set(Collections.unmodifiableMap(fresh));
    }

    /**
     * Insert or replace a single value, atomically updating the snapshot.
     */
    public void upsert(V value) {
        if (value == null) {
            return;
        }
        K key = idExtractor.apply(value);
        if (key == null) {
            return;
        }
        while (true) {
            Map<K, V> current = snapshotRef.get();
            Map<K, V> copy = new HashMap<>(current);
            copy.put(key, value);
            Map<K, V> next = Collections.unmodifiableMap(copy);
            if (snapshotRef.compareAndSet(current, next)) {
                return;
            }
        }
    }

    /**
     * Remove a value by key, if present, atomically updating the snapshot.
     */
    public void removeByKey(K key) {
        if (key == null) {
            return;
        }
        while (true) {
            Map<K, V> current = snapshotRef.get();
            if (!current.containsKey(key)) {
                return;
            }
            Map<K, V> copy = new HashMap<>(current);
            copy.remove(key);
            Map<K, V> next = Collections.unmodifiableMap(copy);
            if (snapshotRef.compareAndSet(current, next)) {
                return;
            }
        }
    }

    public Optional<V> getOptional(K key) {
        return Optional.ofNullable(snapshotRef.get().get(key));
    }

    public V getOrNull(K key) {
        return snapshotRef.get().get(key);
    }

    public boolean containsKey(K key) {
        return snapshotRef.get().containsKey(key);
    }

    public int size() {
        return snapshotRef.get().size();
    }

    /**
     * Returns a defensive copy of values at the moment of calling.
     */
    public List<V> valuesSnapshot() {
        return new ArrayList<>(snapshotRef.get().values());
    }

    /**
     * Replace entire cache with an empty state.
     */
    public void clear() {
        snapshotRef.set(Collections.emptyMap());
    }
}
