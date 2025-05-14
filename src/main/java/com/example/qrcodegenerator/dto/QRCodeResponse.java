package com.example.qrcodegenerator.dto;

public class QRCodeResponse {
    private Long id;
    private String data;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}