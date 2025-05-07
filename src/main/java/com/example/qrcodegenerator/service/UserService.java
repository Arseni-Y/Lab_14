package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.cache.SimpleCache;
import com.example.qrcodegenerator.exception.ResourceNotFoundException;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final SimpleCache cache;

    @Autowired
    public UserService(UserRepository userRepository, SimpleCache cache) {
        this.userRepository = userRepository;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getById(id);
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findByNameContaining(String namePart) {
        String cacheKey = "usersByName:" + namePart.toLowerCase();
        Optional<Object> cachedResult = cache.get(cacheKey);

        if (cachedResult.isPresent()) {
            return (List<User>) cachedResult.get();
        }

        List<User> users = userRepository.findByNameContainingIgnoreCase(namePart);
        cache.put(cacheKey, users);
        return users;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmailWithCache(String email) {
        String cacheKey = "userByEmail:" + email.toLowerCase();
        Optional<Object> cachedResult = cache.get(cacheKey);

        if (cachedResult.isPresent()) {
            return Optional.of((User) cachedResult.get());
        }

        Optional<User> user = userRepository.findByEmail(email);
        user.ifPresent(u -> cache.put(cacheKey, u));
        return user;
    }
}