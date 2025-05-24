package com.example.qrcodegenerator.mapper;

import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.model.QRCode;

import java.time.LocalDateTime;

public class QRCodeMapper
{
    public static QRCodeResponse toDTO(QRCode qrCode)
    {
        return QRCodeResponse.builder()
                .id(qrCode.getId())
                .data(qrCode.getData())
                .imageUrl("")
                .size("350x350")
                .colors("#000000/#FFFFFF")
                .createdAt(LocalDateTime.now())
                .userId(qrCode.getUsers().isEmpty() ? null : qrCode.getUsers().iterator().next().getId())
                .build();
    }
}