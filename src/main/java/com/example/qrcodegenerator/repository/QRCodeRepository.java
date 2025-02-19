package com.example.qrcodegenerator.repository;

import com.example.qrcodegenerator.model.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
}