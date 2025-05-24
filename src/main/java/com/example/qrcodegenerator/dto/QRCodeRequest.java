package com.example.qrcodegenerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for QR code creation")
public class QRCodeRequest
{
    @NotBlank(message = "Data cannot be blank")
    @Size(max = 1000, message = "Data must be less than 1000 characters")
    @Schema(
            description = "Content to be encoded in QR code",
            example = "https://example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String data;

    @Schema(
            description = "Color of QR code in HEX format",
            example = "#000000",
            defaultValue = "#000000"
    )
    private String color = "#000000";

    @Schema(
            description = "Background color of QR code in HEX format",
            example = "#FFFFFF",
            defaultValue = "#FFFFFF"
    )
    private String backgroundColor = "#FFFFFF";

    @Schema(
            description = "Width of QR code in pixels",
            example = "200",
            defaultValue = "200"
    )
    private int width = 200;

    @Schema(
            description = "Height of QR code in pixels",
            example = "200",
            defaultValue = "200"
    )
    private int height = 200;
}