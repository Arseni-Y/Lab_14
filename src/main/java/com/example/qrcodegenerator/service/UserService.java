package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.UserRepository;
import com.example.qrcodegenerator.util.SimpleCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService
{
    private final UserRepository userRepository;
    private final SimpleCache<String, User> emailCache;

    public UserService(UserRepository userRepository, SimpleCache<String, User> emailCache)
    {
        this.userRepository = userRepository;
        this.emailCache = emailCache;
    }

    public List<User> findAll()
    {
        return userRepository.findAll();
    }

    public User save(User user)
    {
        return userRepository.save(user);
    }

    public User getById(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " +
                        id));
    }

    public User updateUser(Long id, User userDetails)
    {
        User user = getById(id);
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        return userRepository.save(user);
    }

    public void deleteById(Long id)
    {
        userRepository.deleteById(id);
    }

    public List<User> findByNameContaining(String namePart)
    {
        return userRepository.findByNameContaining(namePart);
    }

    public Optional<User> findByEmailWithCache(String email)
    {
        User cachedUser = emailCache.get(email);
        if (cachedUser != null)
        {
            log.info("Returning cached user for email: {}", email);
            return Optional.of(cachedUser);
        }

        Optional<User> user = userRepository.findByEmail(email);
        user.ifPresent(u -> emailCache.put(email, u));
        return user;
    }
}