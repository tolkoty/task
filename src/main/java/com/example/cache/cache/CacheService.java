package com.example.cache.cache;

import com.example.cache.events.CacheEvent;
import com.example.cache.events.EntityChangedEvent;
import com.example.cache.repository.CrudRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class CacheService {
    private final Map<String, TableCache<Object, Object>> caches = new ConcurrentHashMap<>();
    private final Map<String, RepositoryAdapter<?, ?>> repositories = new HashMap<>();

    public <ID, E> void registerRepository(CrudRepository<ID, E> repository, Function<E, ID> idExtractor) {
        RepositoryAdapter<ID, E> adapter = new RepositoryAdapter<>(repository, idExtractor);
        repositories.put(repository.getTableName(), adapter);
        caches.put(repository.getTableName(), new TableCache<>(repository.getTableName(), adapter::loadAllAsMap));
    }

    @SuppressWarnings("unchecked")
    public <ID, E> Optional<E> get(String tableName, ID id) {
        TableCache<Object, Object> cache = caches.get(tableName);
        if (cache == null) {
            return Optional.empty();
        }
        return (Optional<E>) cache.get(id);
    }

    @SuppressWarnings("unchecked")
    public <ID, E> Map<ID, E> snapshot(String tableName) {
        TableCache<Object, Object> cache = caches.get(tableName);
        if (cache == null) {
            return new HashMap<>();
        }
        return (Map<ID, E>) cache.snapshot();
    }

    @EventListener
    public void onCacheEvent(CacheEvent event) {
        if (event instanceof EntityChangedEvent) {
            handleEntityChanged((EntityChangedEvent) event);
        }
    }

    @SuppressWarnings("unchecked")
    private <ID, E> void handleEntityChanged(EntityChangedEvent event) {
        TableCache<Object, Object> cache = caches.get(event.getTableName());
        RepositoryAdapter<ID, E> adapter = (RepositoryAdapter<ID, E>) repositories.get(event.getTableName());
        if (cache == null || adapter == null) {
            return;
        }
        switch (event.getChangeType()) {
            case CREATED:
            case UPDATED: {
                Optional<E> entity = adapter.repository.findById((ID) event.getEntityId());
                entity.ifPresent(value -> cache.put((ID) event.getEntityId(), value));
                break;
            }
            case DELETED: {
                cache.remove((ID) event.getEntityId());
                break;
            }
        }
    }

    private static class RepositoryAdapter<ID, E> {
        private final CrudRepository<ID, E> repository;
        private final Function<E, ID> idExtractor;

        private RepositoryAdapter(CrudRepository<ID, E> repository, Function<E, ID> idExtractor) {
            this.repository = repository;
            this.idExtractor = idExtractor;
        }

        private Map<ID, E> loadAllAsMap() {
            List<E> all = repository.findAll();
            Map<ID, E> result = new HashMap<>();
            for (E entity : all) {
                result.put(idExtractor.apply(entity), entity);
            }
            return result;
        }
    }
}
