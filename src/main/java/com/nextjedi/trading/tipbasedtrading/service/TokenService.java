package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.TokenRepository;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
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

    public void insert(String requestToken){
        logger.info("inside insert token service method");
        TokenAccess token = new TokenAccess();
        String apikey = "2himf7a1ff5edpjy";
        String apiSecret = "87mebxtvu3226igmjnkjfjfcrgiphfxb";
        KiteConnect kiteSdk = new KiteConnect(apikey);
        try {
            User user =kiteSdk.generateSession(requestToken,apiSecret);
            token.setPublicToken(user.publicToken);
            token.setAccesstoken(user.accessToken);
            tokenRepository.deleteAll();
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
}
