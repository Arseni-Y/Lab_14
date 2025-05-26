package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.cache.SimpleCache;
import com.example.qrcodegenerator.dto.QRCodeRequest;
import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.QRCodeRepository;
import com.example.qrcodegenerator.cache.SimpleCache;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    @Mock
    private QRCodeRepository qrCodeRepository;

    @Mock
    private SimpleCache <String, List<QRCode>> contentSearchCache;

    @Mock
    private UserService userService;

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
    void testGenerateBulkQRCodes_Success_WithUser() {
        QRCode qrCode1 = new QRCode();
        qrCode1.setId(1L);
        qrCode1.setData(request1.getData());

        QRCode qrCode2 = new QRCode();
        qrCode2.setId(2L);
        qrCode2.setData(request2.getData());

        when(userService.getById(1L)).thenReturn(user);
        when(qrCodeRepository.save(any(QRCode.class)))
                .thenReturn(qrCode1)
                .thenReturn(qrCode2);
        when(userService.save(any(User.class))).thenReturn(user);

        List<QRCodeRequest> requests = Arrays.asList(request1, request2);
        List<QRCodeResponse> responses = qrCodeService.generateBulkQRCodes(requests, 1L);

        assertEquals(2, responses.size());

        QRCodeResponse response1 = responses.get(0);
        assertEquals(1L, response1.getId());
        assertEquals(request1.getData(), response1.getData());
        assertEquals("200x200", response1.getSize());
        assertEquals("#FF0000/#FFFFFF", response1.getColors());
        assertNotNull(response1.getImageUrl());
        assertEquals(1L, response1.getUserId());
        assertNotNull(response1.getCreatedAt());

        QRCodeResponse response2 = responses.get(1);
        assertEquals(2L, response2.getId());
        assertEquals(request2.getData(), response2.getData());
        assertEquals("300x300", response2.getSize());
        assertEquals("#00FF00/#000000", response2.getColors());
        assertNotNull(response2.getImageUrl());
        assertEquals(1L, response2.getUserId());
        assertNotNull(response2.getCreatedAt());

        verify(qrCodeRepository, times(2)).save(any(QRCode.class));
        verify(userService, times(1)).save(user);
    }

    @Test
    void testGenerateBulkQRCodes_Success_WithoutUser() {
        QRCode qrCode1 = new QRCode();
        qrCode1.setId(1L);
        qrCode1.setData(request1.getData());

        when(qrCodeRepository.save(any(QRCode.class))).thenReturn(qrCode1);

        List<QRCodeRequest> requests = Arrays.asList(request1);
        List<QRCodeResponse> responses = qrCodeService.generateBulkQRCodes(requests, null);

        assertEquals(1, responses.size());
        QRCodeResponse response = responses.get(0);
        assertEquals(1L, response.getId());
        assertEquals(request1.getData(), response.getData());
        assertNull(response.getUserId());

        verify(qrCodeRepository, times(1)).save(any(QRCode.class));
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testGenerateBulkQRCodes_EmptyList() {
        List<QRCodeResponse> responses = qrCodeService.generateBulkQRCodes(Collections.emptyList(), null);

        assertTrue(responses.isEmpty());
        verify(qrCodeRepository, never()).save(any(QRCode.class));
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void testGenerateBulkQRCodes_FailsOnWriterException() throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = mock(QRCodeWriter.class);
        when(qrCodeWriter.encode(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new WriterException("Test WriterException"));

        List<QRCodeRequest> requests = Arrays.asList(request1);
        assertThrows(RuntimeException.class, () -> qrCodeService.generateBulkQRCodes(requests, null));
        verify(qrCodeRepository, never()).save(any(QRCode.class));
    }
}