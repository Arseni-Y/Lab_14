package com.example.qrcodegenerator.repository;

import com.example.qrcodegenerator.model.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QRCodeRepository extends JpaRepository<QRCode, Long>
{
    List<QRCode> findByDataContaining(String data);
}