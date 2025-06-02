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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QRCodeService {
    private static final int DEFAULT_DIMENSION = 350;
    private static final String DEFAULT_COLOR = "#000000";
    private static final String DEFAULT_BACKGROUND_COLOR = "#FFFFFF";

    private final QRCodeRepository qrCodeRepository;
    private final SimpleCache<String, List<QRCode>> contentSearchCache;
    private final UserService userService;
    private final QRCodeWriter qrCodeWriter;

    public QRCodeService(QRCodeRepository qrCodeRepository,
                         SimpleCache<String, List<QRCode>> contentSearchCache,
                         UserService userService,
                         QRCodeWriter qrCodeWriter) {
        this.qrCodeRepository = qrCodeRepository;
        this.contentSearchCache = contentSearchCache;
        this.userService = userService;
        this.qrCodeWriter = qrCodeWriter;
    }

    public List<QRCodeResponse> generateBulkQRCodes(List<QRCodeRequest> requests, Long userId) {
        if (requests == null) {
            throw new IllegalArgumentException("Requests list cannot be null");
        }
        User user = userId != null ? userService.getById(userId) : null;
        return requests.stream()
                .map(request -> generateSingleQRCode(request, user))
                .collect(Collectors.toList());
    }

    private QRCodeResponse generateSingleQRCode(QRCodeRequest request, User user) {
        try {
            if (request == null || request.getData() == null || request.getData().trim().isEmpty()) {
                throw new IllegalArgumentException("QR code request or data cannot be null or empty");
            }

            String imageBase64 = generateQRCodeImage(request);

            QRCode qrCode = new QRCode();
            qrCode.setData(request.getData());
            if (user != null) {
                qrCode.addUser(user);
            }

            QRCode savedQRCode = qrCodeRepository.save(qrCode);

            if (user != null) {
                userService.save(user);
            }

            Integer width = request.getWidth();
            Integer height = request.getHeight();
            if (width == null) width = 0;
            if (height == null) height = 0;
            String size = width.toString() + "x" + height.toString();
            String colors = (request.getColor() != null ? request.getColor() : DEFAULT_COLOR) + "/" + (request.getBackgroundColor() != null ? request.getBackgroundColor() : DEFAULT_BACKGROUND_COLOR);

            QRCodeResponse response = QRCodeResponse.builder()
                    .id(savedQRCode.getId())
                    .data(savedQRCode.getData())
                    .imageUrl("data:image/png;base64," + imageBase64)
                    .size(size)
                    .colors(colors)
                    .createdAt(LocalDateTime.now())
                    .userId(user != null ? user.getId() : null)
                    .build();

            log.info("Successfully generated QR code with ID: {}", savedQRCode.getId());
            return response;
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }
    }

    private String generateQRCodeImage(QRCodeRequest request) throws WriterException, IOException {
        int width = getValidDimension(request.getWidth());
        int height = getValidDimension(request.getHeight());
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive values");
        }
        String color = request.getColor() != null ? request.getColor() : DEFAULT_COLOR;
        String backgroundColor = request.getBackgroundColor() != null ? request.getBackgroundColor() : DEFAULT_BACKGROUND_COLOR;

        BitMatrix bitMatrix = qrCodeWriter.encode(request.getData(), BarcodeFormat.QR_CODE, width, height);
        if (bitMatrix == null) {
            throw new IllegalStateException("Failed to generate BitMatrix for QR code data: " + request.getData());
        }
        if (bitMatrix.getWidth() <= 0 || bitMatrix.getHeight() <= 0) {
            throw new IllegalStateException("Generated BitMatrix has invalid dimensions: " + bitMatrix.getWidth() + "x" + bitMatrix.getHeight());
        }

        int onColor = parseHexColor(color);
        int offColor = parseHexColor(backgroundColor);
        MatrixToImageConfig config = new MatrixToImageConfig(onColor, offColor);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream, config);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private int getValidDimension(Integer dimension) {
        return dimension != null && dimension > 0 ? dimension : DEFAULT_DIMENSION;
    }

    private int parseHexColor(String hexColor) {
        String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        return (int) Long.parseLong(hex, 16) | 0xFF000000;
    }

    public byte[] generateQRCode(String text) throws WriterException, IOException {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text for QR code cannot be null or empty");
        }

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, DEFAULT_DIMENSION, DEFAULT_DIMENSION);
        if (bitMatrix == null) {
            throw new IllegalStateException("Failed to generate BitMatrix for QR code text: " + text);
        }
        if (bitMatrix.getWidth() <= 0 || bitMatrix.getHeight() <= 0) {
            throw new IllegalStateException("Generated BitMatrix has invalid dimensions: " + bitMatrix.getWidth() + "x" + bitMatrix.getHeight());
        }

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public List<QRCode> findAll() {
        return qrCodeRepository.findAll();
    }

    public QRCode save(QRCode qrCode) {
        return qrCodeRepository.save(qrCode);
    }

    public QRCode getById(Long id) {
        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QRCode not found with id: " + id));
    }

    public void deleteById(Long id) {
        qrCodeRepository.deleteById(id);
    }

    public List<QRCode> findByUser(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        return user.getQrCodes().stream().collect(Collectors.toList());
    }

    public List<QRCode> findByDataContaining(String data) {
        List<QRCode> cachedResult = contentSearchCache.get(data);
        if (cachedResult != null) {
            log.info("Returning cached result for content search: {}", data);
            return cachedResult;
        }

        List<QRCode> result = qrCodeRepository.findByDataContaining(data);
        contentSearchCache.put(data, result);
        return result;
    }

    public void clearContentSearchCache(String content) {
        contentSearchCache.remove(content);
        log.info("Cleared cache for content search: {}", content);
    }
}