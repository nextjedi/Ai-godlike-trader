package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.TokenRepository;
import com.nextjedi.trading.tipbasedtrading.models.ApiSecret;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.models.TokenDTO;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TokenService {
    Logger logger = LoggerFactory.getLogger(TokenService.class);
    @Autowired
    private TokenRepository tokenRepository;

    public void insert(TokenDTO requestToken){
        logger.info("inside insert token service method");
        var secret =ApiSecret.apiKeys.get(requestToken.getUserId());
        KiteConnect kiteSdk = new KiteConnect(secret.getApiKey());
        try {
            User user =kiteSdk.generateSession(requestToken.getRequestToken(), secret.getApiSecret());
            TokenAccess token = new TokenAccess();
            token.setPublicToken(user.publicToken);
            token.setAccessToken(user.accessToken);
            token.setUserId(requestToken.getUserId());
            tokenRepository.save(token);
            logger.info("Token updated");
        } catch (KiteException| IOException e) {
            logger.error("exception while inserting token");
            throw new RuntimeException(e);
        }

    }

    public TokenAccess getToken(){

        List<TokenAccess> tokens =tokenRepository.findAll();
        return tokens.get(0);
    }
    public List<TokenAccess> getTokenByUserId(String userId){
        List<TokenAccess> tokens =tokenRepository.findByUserId(userId);
        return tokens;
    }
}
