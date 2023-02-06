package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.TokenRepository;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenService {
    @Autowired
    private TokenRepository tokenRepository;

    public void insert(TokenAccess token){
        tokenRepository.save(token);
    }

    public List<TokenAccess> getToken(){
        return tokenRepository.findAll();
    }
}
