package com.nextjedi.trading.tipbasedtrading.models;

import jakarta.persistence.*;

@Entity
public class TokenAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String publicToken;
    private String accesstoken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicToken() {
        return publicToken;
    }

    public void setPublicToken(String publicToken) {
        this.publicToken = publicToken;
    }

    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }
}
