package com.example.cache.example;

import com.example.cache.cache.CacheService;
import com.example.cache.events.CacheEventBus;
import com.example.cache.events.EntityChangedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final CacheEventBus eventBus;
    @PersistenceContext
    private EntityManager entityManager;

    public UserService(UserRepository userRepository, CacheService cacheService, CacheEventBus eventBus) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
        this.eventBus = eventBus;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        cacheService.registerRepository(userRepository, User::getId);
    }

    @Transactional
    public User create(String name) {
        User u = new User();
        u.setName(name);
        entityManager.persist(u);
        entityManager.flush();
        eventBus.publish(new EntityChangedEvent("users", EntityChangedEvent.ChangeType.CREATED, u.getId()));
        return u;
    }

    @Transactional
    public Optional<User> updateName(Long id, String newName) {
        User u = entityManager.find(User.class, id);
        if (u == null) return Optional.empty();
        u.setName(newName);
        entityManager.merge(u);
        eventBus.publish(new EntityChangedEvent("users", EntityChangedEvent.ChangeType.UPDATED, id));
        return Optional.of(u);
    }

    @Transactional
    public boolean delete(Long id) {
        User u = entityManager.find(User.class, id);
        if (u == null) return false;
        entityManager.remove(u);
        eventBus.publish(new EntityChangedEvent("users", EntityChangedEvent.ChangeType.DELETED, id));
        return true;
    }

    public Optional<User> getFromCache(Long id) {
        return cacheService.get("users", id);
    }

    
}
