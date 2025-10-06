package com.example.cache.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TableCache<ID, E> {
    private final String tableName;
    private final Map<ID, E> dataById = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    private final Supplier<Map<ID, E>> initializer;

    public TableCache(String tableName, Supplier<Map<ID, E>> initializer) {
        this.tableName = tableName;
        this.initializer = initializer;
    }

    public String getTableName() {
        return tableName;
    }

    public synchronized void ensureInitialized() {
        if (!initialized) {
            Map<ID, E> loaded = initializer.get();
            dataById.clear();
            dataById.putAll(loaded);
            initialized = true;
        }
    }

    public Optional<E> get(ID id) {
        ensureInitialized();
        return Optional.ofNullable(dataById.get(id));
    }

    public Map<ID, E> snapshot() {
        ensureInitialized();
        return new ConcurrentHashMap<>(dataById);
    }

    public void put(ID id, E entity) {
        ensureInitialized();
        dataById.put(id, entity);
    }

    public void remove(ID id) {
        ensureInitialized();
        dataById.remove(id);
    }

    public void invalidate() {
        initialized = false;
    }
}
