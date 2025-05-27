package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.dto.QRCodeRequest;
import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.QRCodeRepository;
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
    private UserService userService;

    @Mock
    private QRCodeWriter qrCodeWriter;

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
    void generateBulkQRCodes_ValidInputWithUser_ReturnsQRCodeResponses() {
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

        assertEquals(2, responses.size(), "Должно вернуться два ответа QR-кода");

        QRCodeResponse response1 = responses.get(0);
        assertAll("Первый ответ QR-кода",
                () -> assertEquals(1L, response1.getId(), "ID должен совпадать"),
                () -> assertEquals(request1.getData(), response1.getData(), "Данные должны совпадать"),
                () -> assertEquals("200x200", response1.getSize(), "Размер должен совпадать"),
                () -> assertEquals("#FF0000/#FFFFFF", response1.getColors(), "Цвета должны совпадать"),
                () -> assertNotNull(response1.getImageUrl(), "URL изображения не должен быть null"),
                () -> assertEquals(1L, response1.getUserId(), "ID пользователя должен совпадать"),
                () -> assertNotNull(response1.getCreatedAt(), "Время создания не должно быть null")
        );

        QRCodeResponse response2 = responses.get(1);
        assertAll("Второй ответ QR-кода",
                () -> assertEquals(2L, response2.getId(), "ID должен совпадать"),
                () -> assertEquals(request2.getData(), response2.getData(), "Данные должны совпадать"),
                () -> assertEquals("300x300", response2.getSize(), "Размер должен совпадать"),
                () -> assertEquals("#00FF00/#000000", response2.getColors(), "Цвета должны совпадать"),
                () -> assertNotNull(response2.getImageUrl(), "URL изображения не должен быть null"),
                () -> assertEquals(1L, response2.getUserId(), "ID пользователя должен совпадать"),
                () -> assertNotNull(response2.getCreatedAt(), "Время создания не должно быть null")
        );

        verify(qrCodeRepository, times(2)).save(any(QRCode.class));
        verify(userService, times(1)).save(user);
    }

    @Test
    void generateBulkQRCodes_ValidInputWithoutUser_ReturnsQRCodeResponses() {
        QRCode qrCode1 = new QRCode();
        qrCode1.setId(1L);
        qrCode1.setData(request1.getData());

        when(qrCodeRepository.save(any(QRCode.class))).thenReturn(qrCode1);

        List<QRCodeRequest> requests = Arrays.asList(request1);
        List<QRCodeResponse> responses = qrCodeService.generateBulkQRCodes(requests, null);

        assertEquals(1, responses.size(), "Должен вернуться один ответ QR-кода");
        QRCodeResponse response = responses.get(0);
        assertAll("Ответ QR-кода",
                () -> assertEquals(1L, response.getId(), "ID должен совпадать"),
                () -> assertEquals(request1.getData(), response.getData(), "Данные должны совпадать"),
                () -> assertNull(response.getUserId(), "ID пользователя должен быть null")
        );

        verify(qrCodeRepository, times(1)).save(any(QRCode.class));
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void generateBulkQRCodes_EmptyInputList_ReturnsEmptyList() {
        List<QRCodeResponse> responses = qrCodeService.generateBulkQRCodes(Collections.emptyList(), null);

        assertTrue(responses.isEmpty(), "Должен вернуться пустой список для пустого входного списка");
        verify(qrCodeRepository, never()).save(any(QRCode.class));
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void generateBulkQRCodes_QRCodeWriterThrowsWriterException_ThrowsRuntimeException() throws WriterException, IOException {
        when(qrCodeWriter.encode(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new WriterException("Тестовое исключение WriterException"));

        List<QRCodeRequest> requests = Arrays.asList(request1);
        assertThrows(RuntimeException.class, () -> qrCodeService.generateBulkQRCodes(requests, null),
                "Должно выбросить RuntimeException при WriterException");
        verify(qrCodeRepository, never()).save(any(QRCode.class));
    }

    @Test
    void generateBulkQRCodes_QRCodeWriterThrowsIOException_ThrowsRuntimeException() throws WriterException, IOException {
        when(qrCodeWriter.encode(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new IOException("Тестовое исключение IOException"));

        List<QRCodeRequest> requests = Arrays.asList(request1);
        assertThrows(RuntimeException.class, () -> qrCodeService.generateBulkQRCodes(requests, null),
                "Должно выбросить RuntimeException при IOException");
        verify(qrCodeRepository, never()).save(any(QRCode.class));
    }
}