package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.cache.SimpleCache;
import com.example.qrcodegenerator.exception.ResourceNotFoundException;
import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.QRCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QRCodeService {
    private final QRCodeRepository qrCodeRepository;
    private final SimpleCache cache;

    public QRCodeService(QRCodeRepository qrCodeRepository, SimpleCache cache) {
        this.qrCodeRepository = qrCodeRepository;
        this.cache = cache;
    }

    public byte[] generateQRCode(String text) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 350, 350);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    @Transactional(readOnly = true)
    public List<QRCode> findAll() {
        return qrCodeRepository.findAll();
    }

    public QRCode save(QRCode qrCode) {
        return qrCodeRepository.save(qrCode);
    }

    @Transactional(readOnly = true)
    public QRCode getById(Long id) {
        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QRCode not found with id: " + id));
    }

    public void deleteById(Long id) {
        if (!qrCodeRepository.existsById(id)) {
            throw new ResourceNotFoundException("QRCode not found with id: " + id);
        }
        qrCodeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<QRCode> findByUser(User user) {
        return qrCodeRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<QRCode> findByContentContaining(String content) {
        String cacheKey = "qrcodes-content-" + content;

        Optional<Object> cachedResult = cache.get(cacheKey);
        if (cachedResult.isPresent()) {
            return (List<QRCode>) cachedResult.get();
        }

        List<QRCode> result = qrCodeRepository.findByContentContaining(content);
        cache.put(cacheKey, result);
        return result;
    }

    public void clearContentSearchCache(String content) {
        String cacheKey = "qrcodes-content-" + content;
        cache.remove(cacheKey);
    }
}