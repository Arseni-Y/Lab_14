package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.dto.QRCodeRequest;
import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.QRCodeRepository;
import com.example.qrcodegenerator.cache.SimpleCache;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    @Mock
    private QRCodeRepository qrCodeRepository;

    @Mock
    private UserService userService;

    @Mock
    private QRCodeWriter qrCodeWriter;

    @Mock
    private SimpleCache<String, List<QRCode>> contentSearchCache;

    @InjectMocks
    private QRCodeService qrCodeService;

    private QRCodeRequest request1;
    private QRCodeRequest request2;
    private User user;

    @BeforeEach
    void setUp() {
        request1 = new QRCodeRequest();
        request1.setData("https://example1.com");
        request1.setColor("#FF0000");
        request1.setBackgroundColor("#FFFFFF");
        request1.setWidth(200);
        request1.setHeight(200);

        request2 = new QRCodeRequest();
        request2.setData("https://example2.com");
        request2.setColor("#00FF00");
        request2.setBackgroundColor("#000000");
        request2.setWidth(300);
        request2.setHeight(300);

        user = new User();
        user.setId(1L);
    }

    @Test
    void generateBulkQRCodes_WithEmptyInputList_ReturnsEmptyList() throws WriterException {
        List<QRCodeResponse> responses = qrCodeService.generateBulkQRCodes(List.of(), null);

        assertTrue(responses.isEmpty());
        verify(qrCodeRepository, never()).save(any(QRCode.class));
        verify(userService, never()).save(any(User.class));
        verify(qrCodeWriter, never()).encode(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    void generateBulkQRCodes_WhenQRCodeWriterThrowsWriterException_ThrowsRuntimeException() throws WriterException {
        when(qrCodeWriter.encode(anyString(), eq(BarcodeFormat.QR_CODE), anyInt(), anyInt()))
                .thenThrow(new WriterException("Test WriterException"));

        assertThrows(RuntimeException.class, () -> qrCodeService.generateBulkQRCodes(List.of(request1), null));
        verify(qrCodeRepository, never()).save(any(QRCode.class));
    }

    @Test
    void generateBulkQRCodes_WhenQRCodeWriterThrowsIOException_ThrowsRuntimeException() throws WriterException {
        when(qrCodeWriter.encode(anyString(), eq(BarcodeFormat.QR_CODE), anyInt(), anyInt()))
                .thenThrow(new WriterException("Test WriterException"));

        assertThrows(RuntimeException.class, () -> qrCodeService.generateBulkQRCodes(List.of(request1), null));
        verify(qrCodeRepository, never()).save(any(QRCode.class));
    }

    @Test
    void generateBulkQRCodes_WithNullRequestList_ThrowsIllegalArgumentException() throws WriterException {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateBulkQRCodes(null, null));
        verify(qrCodeRepository, never()).save(any(QRCode.class));
        verify(userService, never()).save(any(User.class));
        verify(qrCodeWriter, never()).encode(anyString(), any(), anyInt(), anyInt());
    }

    @Test
    void findByDataContaining_WhenCacheHit_ReturnsCachedResult() {
        List<QRCode> cachedQRCodes = List.of(new QRCode());
        when(contentSearchCache.get("test")).thenReturn(cachedQRCodes);

        List<QRCode> result = qrCodeService.findByDataContaining("test");

        assertEquals(cachedQRCodes, result);
        verify(contentSearchCache).get("test");
        verify(qrCodeRepository, never()).findByDataContaining(anyString());
    }

    @Test
    void findByDataContaining_WhenCacheMiss_ReturnsRepositoryResult() {
        List<QRCode> qrCodes = List.of(new QRCode());
        when(contentSearchCache.get("test")).thenReturn(null);
        when(qrCodeRepository.findByDataContaining("test")).thenReturn(qrCodes);

        List<QRCode> result = qrCodeService.findByDataContaining("test");

        assertEquals(qrCodes, result);
        verify(contentSearchCache).get("test");
        verify(qrCodeRepository).findByDataContaining("test");
        verify(contentSearchCache).put("test", qrCodes);
    }

    @Test
    void clearContentSearchCache_RemovesCacheEntry() {
        qrCodeService.clearContentSearchCache("test");

        verify(contentSearchCache).remove("test");
    }

    @Test
    void findByUser_WithNullUser_ReturnsEmptyList() {
        List<QRCode> result = qrCodeService.findByUser(null);
        assertTrue(result.isEmpty());
    }
}