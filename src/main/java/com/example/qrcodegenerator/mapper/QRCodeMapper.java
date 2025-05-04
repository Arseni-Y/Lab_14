package com.example.qrcodegenerator.mapper;

import com.example.qrcodegenerator.dto.QRCodeResponse;
import com.example.qrcodegenerator.model.QRCode;

public class QRCodeMapper {
    public static QRCodeResponse toDTO(QRCode qrCode) {
        QRCodeResponse response = new QRCodeResponse();
        response.setId(qrCode.getId());
        response.setData(qrCode.getContent()); // или getData(), в зависимости от вашей модели
        return response;
    }
}