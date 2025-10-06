package com.test.task.persistence;

import com.test.task.cache.ResponseLogCache;
import com.test.task.model.ResponseLog;
import com.test.task.support.ApplicationContextHolder;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

/**
 * JPA entity listener that updates the ResponseLog cache on entity lifecycle events.
 */
public class ResponseLogEntityListener {

    @PostPersist
    public void afterCreate(ResponseLog entity) {
        cache().upsert(entity);
    }

    @PostUpdate
    public void afterUpdate(ResponseLog entity) {
        cache().upsert(entity);
    }

    @PostRemove
    public void afterRemove(ResponseLog entity) {
        if (entity.getId() != null) {
            cache().remove(entity.getId());
        }
    }

    private ResponseLogCache cache() {
        return ApplicationContextHolder.getBean(ResponseLogCache.class);
    }
}
