package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateQRCode(@RequestParam String text) {
        byte[] qrCodeImage = qrCodeService.generateQRCode(text);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeImage.length);
        return ResponseEntity.ok().headers(headers).body(qrCodeImage);
    }

    @GetMapping
    public List<QRCode> getAllQRCodes() {
        return qrCodeService.findAll();
    }

    @PostMapping
    public QRCode createQRCode(@RequestBody QRCode qrCode) {
        return qrCodeService.save(qrCode);
    }

    @GetMapping("/{id}")
    public QRCode getQRCode(@PathVariable Long id) {
        return qrCodeService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteQRCode(@PathVariable Long id) {
        qrCodeService.delete(id);
    }
}