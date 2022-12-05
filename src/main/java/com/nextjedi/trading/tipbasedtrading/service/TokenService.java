package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    @Autowired
    private TokenRepository tokenRepository;
}
