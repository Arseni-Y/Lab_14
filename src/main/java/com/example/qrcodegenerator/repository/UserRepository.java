package com.example.qrcodegenerator.repository;

import com.example.qrcodegenerator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    List<User> findByNameContaining(String namePart);

    Optional<User> findByEmail(String email);
}