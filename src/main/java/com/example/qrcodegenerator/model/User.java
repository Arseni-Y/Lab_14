package com.example.qrcodegenerator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @ManyToMany(mappedBy = "users")
    @JsonIgnore
    private Set<QRCode> qrCodes = new HashSet<>();

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<QRCode> getQrCodes() {
        return qrCodes;
    }

    public void setQrCodes(Set<QRCode> qrCodes) {
        this.qrCodes = qrCodes;
    }

    public void addQRCode(QRCode qrCode) {
        this.qrCodes.add(qrCode);
        qrCode.getUsers().add(this);
    }

    public void removeQRCode(QRCode qrCode) {
        this.qrCodes.remove(qrCode);
        qrCode.getUsers().remove(this);
    }
}