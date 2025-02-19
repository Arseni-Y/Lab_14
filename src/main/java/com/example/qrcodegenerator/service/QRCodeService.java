package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.repository.QRCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class QRCodeService {

    @Autowired
    private QRCodeRepository qrCodeRepository;

    public byte[] generateQRCode(String text) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 350, 350);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR Code", e);
        }
    }

    public List<QRCode> findAll() {
        return qrCodeRepository.findAll();
    }

    public QRCode save(QRCode qrCode) {
        return qrCodeRepository.save(qrCode);
    }

    public QRCode findById(Long id) {
        return qrCodeRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        qrCodeRepository.deleteById(id);
    }
}