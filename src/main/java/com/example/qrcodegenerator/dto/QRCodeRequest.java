package com.example.qrcodegenerator.dto;

import jakarta.validation.constraints.NotBlank;

public class QRCodeRequest {
    @NotBlank(message = "Data cannot be blank")
    private String data;

    // Геттеры и сеттеры
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}