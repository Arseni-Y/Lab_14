package com.example.qrcodegenerator.controller;

import com.example.qrcodegenerator.dto.QRCodeRequest;
import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.exception.ResourceNotFoundException;
import com.example.qrcodegenerator.mapper.QRCodeMapper;
import com.example.qrcodegenerator.model.QRCode;
import com.example.qrcodegenerator.model.User;
import com.example.qrcodegenerator.service.QRCodeService;
import com.example.qrcodegenerator.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "QR Code", description = "QR Code generation and management APIs")
@RestController
@RequestMapping("/api/qrcodes")
public class QRCodeController
{
    private final QRCodeService qrCodeService;
    private final UserService userService;

    public QRCodeController(QRCodeService qrCodeService, UserService userService)
    {
        this.qrCodeService = qrCodeService;
        this.userService = userService;
    }

    @Operation(summary = "Generate QR code image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "QR code image generated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input parameters"),
            @ApiResponse(responseCode = "500",
                    description = "Error generating QR code image")
    })
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateQRCode(
            @Parameter(description = "Text to encode in QR code")
            @RequestParam String text)
    {
        try
        {
            log.info("Generating QR code for text: {}", text);
            byte[] qrCodeImage = qrCodeService.generateQRCode(text);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);
            return ResponseEntity.ok().headers(headers).body(qrCodeImage);
        }
        catch (WriterException | IOException e)
        {
            log.error("Error generating QR code: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all QR codes")
    @ApiResponse(responseCode = "200",
            description = "List of all QR codes retrieved successfully")
    @GetMapping
    public ResponseEntity<List<QRCodeResponse>> getAllQRCodes()
    {
        log.info("Fetching all QR codes");
        List<QRCodeResponse> response = qrCodeService.findAll().stream()
                .map(QRCodeMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new QR code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "QR code created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input data"),
            @ApiResponse(responseCode = "404",
                    description = "User not found")
    })
    @PostMapping
    public ResponseEntity<QRCodeResponse> createQRCode(
            @Parameter(description = "QR code creation request")
            @Valid @RequestBody QRCodeRequest qrCodeRequest,
            @Parameter(description = "Optional user ID to associate with QR code")
            @RequestParam(required = false) Long userId)
    {
        log.info("Creating new QR code with data: {}", qrCodeRequest.getData());
        QRCode qrCode = new QRCode();
        qrCode.setData(qrCodeRequest.getData());

        if (userId != null)
        {
            User user = userService.getById(userId);
            user.addQRCode(qrCode);
            userService.save(user);
        }

        QRCode createdQRCode = qrCodeService.save(qrCode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(QRCodeMapper.toDTO(createdQRCode));
    }

    @Operation(summary = "Get QR code by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR code found"),
            @ApiResponse(responseCode = "404", description = "QR code not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<QRCodeResponse> getQRCodeById(
            @Parameter(description = "ID of QR code to retrieve")
            @PathVariable Long id)
    {
        log.info("Fetching QR code with ID: {}", id);
        QRCode qrCode = qrCodeService.getById(id);
        return ResponseEntity.ok(QRCodeMapper.toDTO(qrCode));
    }

    @Operation(summary = "Update QR code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "QR code updated successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input data"),
            @ApiResponse(responseCode = "404",
                    description = "QR code not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<QRCodeResponse> updateQRCode(
            @Parameter(description = "ID of QR code to update")
            @PathVariable Long id,
            @Parameter(description = "Updated QR code data")
            @Valid @RequestBody QRCodeRequest qrCodeRequest)
    {
        log.info("Updating QR code with ID: {}", id);
        QRCode existingQRCode = qrCodeService.getById(id);
        existingQRCode.setData(qrCodeRequest.getData());
        QRCode updatedQRCode = qrCodeService.save(existingQRCode);
        return ResponseEntity.ok(QRCodeMapper.toDTO(updatedQRCode));
    }

    @Operation(summary = "Delete QR code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "QR code deleted successfully"),
            @ApiResponse(responseCode = "404",
                    description = "QR code not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQRCode(
            @Parameter(description = "ID of QR code to delete")
            @PathVariable Long id)
    {
        log.info("Deleting QR code with ID: {}", id);
        qrCodeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get QR codes by user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "QR codes retrieved successfully"),
            @ApiResponse(responseCode = "404",
                    description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QRCodeResponse>> getQRCodesByUser(
            @Parameter(description = "ID of user to retrieve QR codes for")
            @PathVariable Long userId)
    {
        log.info("Fetching QR codes for user ID: {}", userId);
        User user = userService.getById(userId);
        List<QRCodeResponse> response = qrCodeService.findByUser(user).stream()
                .map(QRCodeMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add QR code to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "QR code added to user successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input data"),
            @ApiResponse(responseCode = "404",
                    description = "User not found")
    })
    @PostMapping("/user/{userId}/qrcodes")
    public ResponseEntity<QRCodeResponse> addQRCodeToUser(
            @Parameter(description = "ID of user to add QR code to")
            @PathVariable Long userId,
            @Parameter(description = "QR code creation request")
            @Valid @RequestBody QRCodeRequest qrCodeRequest)
    {
        log.info("Adding QR code to user ID: {}", userId);
        User user = userService.getById(userId);
        QRCode qrCode = new QRCode();
        qrCode.setData(qrCodeRequest.getData());
        user.addQRCode(qrCode);
        userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(QRCodeMapper.toDTO(qrCode));
    }

    @Operation(summary = "Search QR codes by content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "QR codes retrieved successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid search parameters")
    })
    @GetMapping("/search")
    public ResponseEntity<List<QRCodeResponse>> searchQRCodesByContent(
            @Parameter(description = "Content to search for")
            @RequestParam String content,
            @Parameter(description = "Whether to clear cache before searching")
            @RequestParam(required = false, defaultValue = "false") boolean clearCache)
    {
        log.info("Searching QR codes with content: {}, clearCache: {}",
                content, clearCache);
        if (clearCache)
        {
            qrCodeService.clearContentSearchCache(content);
        }

        List<QRCodeResponse> response = qrCodeService.findByDataContaining(content)
                .stream()
                .map(QRCodeMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}