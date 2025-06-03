package com.example.qrcodegenerator.service;

import com.example.qrcodegenerator.dto.QRCodeRequest;
import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.repository.QRCodeRepository;
import com.example.qrcodegenerator.cache.SimpleCache;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @BeforeEach
    void setUp() {
    }

    @Test
    void generateBulkQRCodesReturnsEmptyListForEmptyInput() {
        List<QRCodeResponse> result = qrCodeService.generateBulkQRCodes(List.of(), null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(qrCodeRepository, userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void generateBulkQRCodesThrowsExceptionForNullRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> qrCodeService.generateBulkQRCodes(null, null));
        verifyNoInteractions(qrCodeRepository, userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void generateBulkQRCodesThrowsExceptionWhenGenerationFails() throws WriterException {
        QRCodeRequest qrCodeRequest = mock(QRCodeRequest.class);
        lenient().when(qrCodeRequest.getData()).thenReturn("https://example.com");
        lenient().when(qrCodeRequest.getColor()).thenReturn("#FF0000");
        lenient().when(qrCodeRequest.getBackgroundColor()).thenReturn("#FFFFFF");
        lenient().when(qrCodeRequest.getWidth()).thenReturn(200);
        lenient().when(qrCodeRequest.getHeight()).thenReturn(200);

        when(qrCodeWriter.encode(anyString(), eq(BarcodeFormat.QR_CODE), anyInt(), anyInt()))
                .thenThrow(new WriterException("Generation error"));

        assertThrows(RuntimeException.class,
                () -> qrCodeService.generateBulkQRCodes(List.of(qrCodeRequest), null));

        verify(qrCodeRepository, never()).save(any());
        verifyNoInteractions(userService, contentSearchCache);
    }

    @Test
    void generateBulkQRCodesGeneratesAndSavesQRCodesSuccessfully() throws WriterException {
        QRCodeRequest qrCodeRequest = mock(QRCodeRequest.class);
        lenient().when(qrCodeRequest.getData()).thenReturn("https://example.com");
        lenient().when(qrCodeRequest.getColor()).thenReturn("#FF0000");
        lenient().when(qrCodeRequest.getBackgroundColor()).thenReturn("#FFFFFF");
        lenient().when(qrCodeRequest.getWidth()).thenReturn(200);
        lenient().when(qrCodeRequest.getHeight()).thenReturn(200);

        BitMatrix bitMatrix = mock(BitMatrix.class);
        when(bitMatrix.getWidth()).thenReturn(200);
        when(bitMatrix.getHeight()).thenReturn(200);
        BitArray bitArray = mock(BitArray.class);
        when(bitArray.get(anyInt())).thenReturn(true);
        when(bitMatrix.getRow(anyInt(), any(BitArray.class))).thenReturn(bitArray);
        when(qrCodeWriter.encode(anyString(), eq(BarcodeFormat.QR_CODE), eq(200), eq(200)))
                .thenReturn(bitMatrix);
        QRCode savedQRCode = mock(QRCode.class);
        when(savedQRCode.getId()).thenReturn(1L);
        when(savedQRCode.getData()).thenReturn("https://example.com");
        when(qrCodeRepository.save(any(QRCode.class))).thenReturn(savedQRCode);

        List<QRCodeResponse> result = qrCodeService.generateBulkQRCodes(List.of(qrCodeRequest), null);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("https://example.com", result.get(0).getData());
        verify(qrCodeWriter).encode(anyString(), eq(BarcodeFormat.QR_CODE), eq(200), eq(200));
        verify(qrCodeRepository).save(any(QRCode.class));
        verifyNoInteractions(userService, contentSearchCache);
    }

    @Test
    void generateBulkQRCodesAssociatesUserCorrectly() throws WriterException {
        QRCodeRequest qrCodeRequest = mock(QRCodeRequest.class);
        lenient().when(qrCodeRequest.getData()).thenReturn("data");
        lenient().when(qrCodeRequest.getColor()).thenReturn("#FF0000");
        lenient().when(qrCodeRequest.getBackgroundColor()).thenReturn("#FFFFFF");
        lenient().when(qrCodeRequest.getWidth()).thenReturn(200);
        lenient().when(qrCodeRequest.getHeight()).thenReturn(200);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userService.getById(eq(1L))).thenReturn(user);
        BitMatrix bitMatrix = mock(BitMatrix.class);
        when(bitMatrix.getWidth()).thenReturn(200);
        when(bitMatrix.getHeight()).thenReturn(200);
        BitArray bitArray = mock(BitArray.class);
        when(bitArray.get(anyInt())).thenReturn(true);
        when(bitMatrix.getRow(anyInt(), any(BitArray.class))).thenReturn(bitArray);
        when(qrCodeWriter.encode(anyString(), eq(BarcodeFormat.QR_CODE), eq(200), eq(200)))
                .thenReturn(bitMatrix);
        QRCode savedQRCode = mock(QRCode.class);
        when(savedQRCode.getId()).thenReturn(1L);
        when(savedQRCode.getData()).thenReturn("data");
        when(qrCodeRepository.save(any(QRCode.class))).thenReturn(savedQRCode);

        List<QRCodeResponse> result = qrCodeService.generateBulkQRCodes(List.of(qrCodeRequest), 1L);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        verify(userService).getById(1L);
        verify(userService).save(user);
        verify(qrCodeWriter).encode(anyString(), eq(BarcodeFormat.QR_CODE), eq(200), eq(200));
        verify(qrCodeRepository).save(any(QRCode.class));
        verifyNoInteractions(contentSearchCache);
    }

    @Test
    void generateBulkQRCodesThrowsExceptionForEmptyData() {
        QRCodeRequest qrCodeRequest = mock(QRCodeRequest.class);
        lenient().when(qrCodeRequest.getData()).thenReturn("");
        lenient().when(qrCodeRequest.getColor()).thenReturn("#FF0000");
        lenient().when(qrCodeRequest.getBackgroundColor()).thenReturn("#FFFFFF");
        lenient().when(qrCodeRequest.getWidth()).thenReturn(200);
        lenient().when(qrCodeRequest.getHeight()).thenReturn(200);

        assertThrows(IllegalArgumentException.class,
                () -> qrCodeService.generateBulkQRCodes(List.of(qrCodeRequest), null));
        verifyNoInteractions(qrCodeRepository, userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void generateBulkQRCodesThrowsExceptionForInvalidColor() throws WriterException {
        QRCodeRequest qrCodeRequest = mock(QRCodeRequest.class);
        lenient().when(qrCodeRequest.getData()).thenReturn("https://example.com");
        lenient().when(qrCodeRequest.getColor()).thenReturn("invalid-color");
        lenient().when(qrCodeRequest.getBackgroundColor()).thenReturn("#FFFFFF");
        lenient().when(qrCodeRequest.getWidth()).thenReturn(200);
        lenient().when(qrCodeRequest.getHeight()).thenReturn(200);

        BitMatrix bitMatrix = mock(BitMatrix.class);
        when(bitMatrix.getWidth()).thenReturn(200);
        when(bitMatrix.getHeight()).thenReturn(200);
        when(qrCodeWriter.encode(anyString(), eq(BarcodeFormat.QR_CODE), eq(200), eq(200)))
                .thenReturn(bitMatrix);

        assertThrows(RuntimeException.class,
                () -> qrCodeService.generateBulkQRCodes(List.of(qrCodeRequest), null));
        verify(qrCodeWriter).encode(anyString(), eq(BarcodeFormat.QR_CODE), eq(200), eq(200));
        verifyNoInteractions(userService, qrCodeRepository, contentSearchCache);
    }

    @Test
    void generateQRCodeThrowsExceptionForNullText() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateQRCode(null));
        verifyNoInteractions(qrCodeWriter, qrCodeRepository, userService, contentSearchCache);
    }

    @Test
    void findAllReturnsAllQRCodes() {
        QRCode qrCode = mock(QRCode.class);
        when(qrCodeRepository.findAll()).thenReturn(List.of(qrCode));

        List<QRCode> result = qrCodeService.findAll();
        assertEquals(1, result.size());
        verify(qrCodeRepository).findAll();
        verifyNoInteractions(userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void saveSavesQRCode() {
        QRCode qrCode = mock(QRCode.class);
        when(qrCodeRepository.save(qrCode)).thenReturn(qrCode);

        QRCode result = qrCodeService.save(qrCode);
        assertNotNull(result);
        verify(qrCodeRepository).save(qrCode);
        verifyNoInteractions(userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void getByIdReturnsQRCode() {
        QRCode qrCode = mock(QRCode.class);
        when(qrCodeRepository.findById(1L)).thenReturn(Optional.of(qrCode));

        QRCode result = qrCodeService.getById(1L);
        assertNotNull(result);
        verify(qrCodeRepository).findById(1L);
        verifyNoInteractions(userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void getByIdThrowsExceptionForNonExistentId() {
        when(qrCodeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> qrCodeService.getById(1L));
        verify(qrCodeRepository).findById(1L);
        verifyNoInteractions(userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void deleteByIdDeletesQRCode() {
        qrCodeService.deleteById(1L);
        verify(qrCodeRepository).deleteById(1L);
        verifyNoInteractions(userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void findByUserReturnsQRCodesForValidUser() {
        User user = mock(User.class);
        QRCode qrCode = mock(QRCode.class);
        when(user.getQrCodes()).thenReturn(Set.of(qrCode));

        List<QRCode> result = qrCodeService.findByUser(user);
        assertEquals(1, result.size());
        verifyNoInteractions(qrCodeRepository, userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void findByUserReturnsEmptyListForNullUser() {
        List<QRCode> result = qrCodeService.findByUser(null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(qrCodeRepository, userService, qrCodeWriter, contentSearchCache);
    }

    @Test
    void findByDataContainingReturnsCachedDataWhenAvailable() {
        QRCode cachedQRCode = mock(QRCode.class);
        when(contentSearchCache.get("test")).thenReturn(List.of(cachedQRCode));

        List<QRCode> result = qrCodeService.findByDataContaining("test");
        assertEquals(1, result.size());
        verify(contentSearchCache).get("test");
        verifyNoInteractions(qrCodeRepository, userService, qrCodeWriter);
    }

    @Test
    void findByDataContainingQueriesRepositoryWhenCacheEmpty() {
        QRCode dbQRCode = mock(QRCode.class);
        when(contentSearchCache.get("test")).thenReturn(null);
        when(qrCodeRepository.findByDataContaining("test")).thenReturn(List.of(dbQRCode));

        List<QRCode> result = qrCodeService.findByDataContaining("test");
        assertEquals(1, result.size());
        verify(qrCodeRepository).findByDataContaining("test");
        verify(contentSearchCache).put("test", List.of(dbQRCode));
        verifyNoInteractions(userService, qrCodeWriter);
    }

    @Test
    void clearContentSearchCacheRemovesCacheEntry() {
        qrCodeService.clearContentSearchCache("test");
        verify(contentSearchCache).remove("test");
        verifyNoInteractions(qrCodeRepository, userService, qrCodeWriter);
    }
}