package com.example.qrcodegenerator.repository;

import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    @Query("SELECT q FROM QRCode q JOIN q.users u WHERE u = :user")
    List<QRCode> findByUser(@Param("user") User user);
}