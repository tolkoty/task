package com.example.cache.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<ID, E> {
    String getTableName();
    List<E> findAll();
    Optional<E> findById(ID id);
}
