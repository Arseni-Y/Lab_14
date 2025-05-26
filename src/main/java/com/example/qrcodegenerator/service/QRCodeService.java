package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.dto.QRCodeRequest;
import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.QRCodeRepository;
import com.example.qrcodegenerator.cache.SimpleCache;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QRCodeService
{
    private final QRCodeRepository qrCodeRepository;
    private final SimpleCache<String, List<QRCode>> contentSearchCache;
    private final UserService userService;

    public QRCodeService(QRCodeRepository qrCodeRepository,
                         SimpleCache<String, List<QRCode>> contentSearchCache,
                         UserService userService)
    {
        this.qrCodeRepository = qrCodeRepository;
        this.contentSearchCache = contentSearchCache;
        this.userService = userService;
    }

    public List<QRCodeResponse> generateBulkQRCodes(List<QRCodeRequest> requests, Long userId) {
        User user = userId != null ? userService.getById(userId) : null;

        return requests.stream()
                .map(request -> generateSingleQRCode(request, user))
                .collect(Collectors.toList());
    }

    private QRCodeResponse generateSingleQRCode(QRCodeRequest request, User user) {
        try {
            QRCode qrCode = new QRCode();
            qrCode.setData(request.getData());

            if (user != null) {
                user.addQRCode(qrCode);
                userService.save(user);
            }

            QRCode savedQRCode = qrCodeRepository.save(qrCode);

            String imageBase64 = generateQRCodeImage(request);

            return QRCodeResponse.builder()
                    .id(savedQRCode.getId())
                    .data(savedQRCode.getData())
                    .imageUrl("data:image/png;base64," + imageBase64)
                    .size(request.getWidth() + "x" + request.getHeight())
                    .colors(request.getColor() + "/" + request.getBackgroundColor())
                    .createdAt(LocalDateTime.now())
                    .userId(user != null ? user.getId() : null)
                    .build();
        } catch (WriterException | IOException e) {
            log.error("Ошибка при генерации QR-кода: {}", e.getMessage());
            throw new RuntimeException("Не удалось сгенерировать QR-код: " + e.getMessage());
        }
    }

    private String generateQRCodeImage(QRCodeRequest request) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                request.getData(),
                BarcodeFormat.QR_CODE,
                request.getWidth(),
                request.getHeight()
        );

        int onColor = parseHexColor(request.getColor());
        int offColor = parseHexColor(request.getBackgroundColor());
        MatrixToImageConfig config = new MatrixToImageConfig(onColor, offColor);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream, config);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private int parseHexColor(String hexColor) {
        String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        return (int) Long.parseLong(hex, 16) | 0xFF000000;
    }

    public byte[] generateQRCode(String text) throws WriterException, IOException
    {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 350, 350);

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
                .orElseThrow(() -> new RuntimeException("QRCode not found with id: " + id));
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