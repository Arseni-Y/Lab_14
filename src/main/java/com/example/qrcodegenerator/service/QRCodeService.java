package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.QRCodeRepository;
import com.example.qrcodegenerator.util.SimpleCache;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QRCodeService
{
    private final QRCodeRepository qrCodeRepository;
    private final SimpleCache<String, List<QRCode>> contentSearchCache;

    public QRCodeService(QRCodeRepository qrCodeRepository,
                         SimpleCache<String, List<QRCode>> contentSearchCache)
    {
        this.qrCodeRepository = qrCodeRepository;
        this.contentSearchCache = contentSearchCache;
    }

    public byte[] generateQRCode(String text) throws WriterException, IOException
    {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE,
                350, 350);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public List<QRCode> findAll()
    {
        return qrCodeRepository.findAll();
    }

    public QRCode save(QRCode qrCode)
    {
        return qrCodeRepository.save(qrCode);
    }

    public QRCode getById(Long id)
    {
        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QRCode not found with id: " +
                        id));
    }

    public void deleteById(Long id)
    {
        qrCodeRepository.deleteById(id);
    }

    public List<QRCode> findByUser(User user)
    {
        return user.getQrCodes().stream().collect(Collectors.toList());
    }

    public List<QRCode> findByDataContaining(String data)
    {
        List<QRCode> cachedResult = contentSearchCache.get(data);
        if (cachedResult != null)
        {
            log.info("Returning cached result for content search: {}", data);
            return cachedResult;
        }

        List<QRCode> result = qrCodeRepository.findByDataContaining(data);
        contentSearchCache.put(data, result);
        return result;
    }

    public void clearContentSearchCache(String content)
    {
        contentSearchCache.remove(content);
        log.info("Cleared cache for content search: {}", content);
    }
}