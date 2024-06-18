package com.nextjedi.trading.tipbasedtrading.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecretModel {

    private String apiKey;
    private String apiSecret;
    private String password;
    private String totpKey;

}
