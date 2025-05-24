package com.example.qrcodegenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object with QR code information")
public class QRCodeResponse
{
    @Schema(description = "Unique identifier of QR code", example = "1")
    private Long id;

    @Schema(description = "Encoded data in QR code", example = "https://example.com")
    private String data;

    @Schema(description = "URL or base64 encoded image of QR code",
            example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String imageUrl;

    @Schema(description = "Size of QR code in pixels (width x height)", example = "200x200")
    private String size;

    @Schema(description = "Color scheme used (foreground/background)", example = "#000000/#FFFFFF")
    private String colors;

    @Schema(description = "Date and time when QR code was created",
            example = "2023-05-15T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "User ID who created this QR code (if applicable)",
            example = "123", nullable = true)
    private Long userId;
}