package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.dto.QRCodeRequest;
import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.exception.ResourceNotFoundException;
import com.example.qrcodegenerator.mapper.QRCodeMapper;
import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.service.QRCodeService;
import com.example.qrcodegenerator.service.UserService;
import jakarta.validation.Valid;
import com.google.zxing.WriterException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/qrcodes")
public class QRCodeController {
    private final QRCodeService qrCodeService;
    private final UserService userService;

    public QRCodeController(QRCodeService qrCodeService, UserService userService) {
        this.qrCodeService = qrCodeService;
        this.userService = userService;
    }

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateQRCode(@RequestParam String text) {
        try {
            byte[] qrCodeImage = qrCodeService.generateQRCode(text);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);
            return ResponseEntity.ok().headers(headers).body(qrCodeImage);
        } catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<QRCodeResponse>> getAllQRCodes() {
        List<QRCodeResponse> response = qrCodeService.findAll().stream()
                .map(QRCodeMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<QRCodeResponse> createQRCode(
            @Valid @RequestBody QRCodeRequest qrCodeRequest,
            @RequestParam(required = false) Long userId) {

        QRCode qrCode = new QRCode();
        qrCode.setContent(qrCodeRequest.getData());

        if (userId != null) {
            User user = userService.getById(userId);
            user.addQRCode(qrCode);
        }

        QRCode createdQRCode = qrCodeService.save(qrCode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(QRCodeMapper.toDTO(createdQRCode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QRCodeResponse> getQRCodeById(@PathVariable Long id) {
        QRCode qrCode = qrCodeService.getById(id);
        return ResponseEntity.ok(QRCodeMapper.toDTO(qrCode));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QRCodeResponse> updateQRCode(
            @PathVariable Long id,
            @Valid @RequestBody QRCodeRequest qrCodeRequest) {

        QRCode existingQRCode = qrCodeService.getById(id);
        existingQRCode.setContent(qrCodeRequest.getData());
        QRCode updatedQRCode = qrCodeService.save(existingQRCode);
        return ResponseEntity.ok(QRCodeMapper.toDTO(updatedQRCode));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQRCode(@PathVariable Long id) {
        qrCodeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesByUser(@PathVariable Long userId) {
        User user = userService.getById(userId);
        List<QRCodeResponse> response = qrCodeService.findByUser(user).stream()
                .map(QRCodeMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}/qrcodes")
    public ResponseEntity<QRCodeResponse> addQRCodeToUser(
            @PathVariable Long userId,
            @Valid @RequestBody QRCodeRequest qrCodeRequest) {

        User user = userService.getById(userId);
        QRCode qrCode = new QRCode();
        qrCode.setContent(qrCodeRequest.getData());
        user.addQRCode(qrCode);

        QRCode savedQRCode = qrCodeService.save(qrCode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(QRCodeMapper.toDTO(savedQRCode));
    }
}