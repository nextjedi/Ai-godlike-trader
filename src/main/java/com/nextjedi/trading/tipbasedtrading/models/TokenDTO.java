package com.nextjedi.trading.tipbasedtrading.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {
    private String userId;
    private String requestToken;
}
