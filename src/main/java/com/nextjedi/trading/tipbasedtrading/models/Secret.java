package com.nextjedi.trading.tipbasedtrading.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Secret {

    private String apiKey;
    private String apiSecret;
    private String password;

    private String totpKey;

}
