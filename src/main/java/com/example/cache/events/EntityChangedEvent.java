package com.example.cache.events;

public class EntityChangedEvent implements CacheEvent {
    public enum ChangeType { CREATED, UPDATED, DELETED }

    private final String tableName;
    private final ChangeType changeType;
    private final Object entityId;

    public EntityChangedEvent(String tableName, ChangeType changeType, Object entityId) {
        this.tableName = tableName;
        this.changeType = changeType;
        this.entityId = entityId;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Object getEntityId() {
        return entityId;
    }
}
