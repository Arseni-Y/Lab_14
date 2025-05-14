package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.cache.SimpleCache;
import com.example.qrcodegenerator.exception.ResourceNotFoundException;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final SimpleCache cache;

    public UserService(UserRepository userRepository, SimpleCache cache) {
        this.userRepository = userRepository;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        String cacheKey = "allUsers";
        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            return (List<User>) cached.get();
        }

        List<User> result = userRepository.findAll();
        cache.put(cacheKey, result);
        return result;
    }

    @Transactional
    public User save(User user) {
        User saved = userRepository.save(user);
        cache.clear();
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        String cacheKey = "userOptional:" + id;
        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            return Optional.of((User) cached.get());
        }

        Optional<User> result = userRepository.findById(id);
        result.ifPresent(u -> cache.put(cacheKey, u));
        return result;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        String cacheKey = "user:" + id;
        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            return (User) cached.get();
        }

        User result = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        cache.put(cacheKey, result);
        return result;
    }

    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        cache.remove("user:" + id);
        cache.remove("userOptional:" + id);
        cache.clear();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        String cacheKey = "userExists:" + id;
        Optional<Object> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            return (boolean) cached.get();
        }

        boolean result = userRepository.existsById(id);
        cache.put(cacheKey, result);
        return result;
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getById(id);
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        User updated = userRepository.save(user);
        cache.clear();
        return updated;
    }

    @Transactional(readOnly = true)
    public List<User> findByNameContaining(String namePart) {
        String cacheKey = "usersByName:" + namePart.toLowerCase();
        Optional<Object> cached = cache.get(cacheKey);

        if (cached.isPresent()) {
            return (List<User>) cached.get();
        }

        List<User> users = userRepository.findByNameContainingIgnoreCase(namePart);
        cache.put(cacheKey, users);
        return users;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmailWithCache(String email) {
        String cacheKey = "userByEmail:" + email.toLowerCase();
        Optional<Object> cached = cache.get(cacheKey);

        if (cached.isPresent()) {
            return Optional.of((User) cached.get());
        }

        Optional<User> user = userRepository.findByEmail(email);
        user.ifPresent(u -> cache.put(cacheKey, u));
        return user;
    }
}