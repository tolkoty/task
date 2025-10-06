package com.example.cache.example;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestParam String name) {
        return userService.create(name);
    }

    @PutMapping("/{id}")
    public Optional<User> update(@PathVariable Long id, @RequestParam String name) {
        return userService.updateName(id, name);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return userService.delete(id);
    }

    @GetMapping("/{id}")
    public Optional<User> get(@PathVariable Long id) {
        return userService.getFromCache(id);
    }
}
