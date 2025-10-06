package com.test.task.cache;

import com.test.task.model.ResponseLog;
import com.test.task.repository.ResponseLogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResponseLogCache {

    private final TableCache<Long, ResponseLog> cache = new TableCache<>(ResponseLog::getId);
    private final ResponseLogRepository repository;

    public ResponseLogCache(ResponseLogRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void load() {
        List<ResponseLog> all = repository.findAll();
        cache.refreshAll(all);
    }

    public ResponseLog getOrNull(Long id) {
        return cache.getOrNull(id);
    }

    public void upsert(ResponseLog entity) {
        cache.upsert(entity);
    }

    public void remove(Long id) {
        cache.removeByKey(id);
    }

    public void refreshFromDb() {
        List<ResponseLog> all = repository.findAll();
        cache.refreshAll(all);
    }

    public int size() {
        return cache.size();
    }
}
