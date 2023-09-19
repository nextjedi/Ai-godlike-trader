package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.exception.TokenNotFoundException;
import com.nextjedi.trading.tipbasedtrading.dao.TokenRepository;
import com.nextjedi.trading.tipbasedtrading.models.ApiSecret;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.models.TokenDTO;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class TokenService {
    @Autowired
    private TokenRepository tokenRepository;

    public void insert(TokenDTO requestToken){
        log.info("inside insert token service method");
        var secret =ApiSecret.apiKeys.get(requestToken.getUserId());
        KiteConnect kiteSdk = new KiteConnect(secret.getApiKey());
        try {
            User user =kiteSdk.generateSession(requestToken.getRequestToken(), secret.getApiSecret());
            TokenAccess token = new TokenAccess();
            token.setPublicToken(user.publicToken);
            token.setAccessToken(user.accessToken);
            token.setUserId(requestToken.getUserId());
            tokenRepository.save(token);
            log.info("Token updated");
        } catch (KiteException| IOException e) {
            log.error("exception while inserting token");
            throw new RuntimeException(e);
        }

    }

    public List<TokenAccess> getToken(){
        log.info("inside get token service method");
        var tokens = tokenRepository.findAll();
        log.info("number of tokens found ",tokens.size());
        return tokens;
        
    }
    public TokenAccess getLatestTokenByUserId(String userId){
        log.info("inside get token service method");
        try {
            TokenAccess token =tokenRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
            if(Objects.nonNull(token)){
                log.info(token.toString());
                log.info("Token found for " + token.getUserId());
                return token;
            }
            throw new TokenNotFoundException("Token is null");
        }catch (Exception e){
            log.error("token is not available now");
            throw new TokenNotFoundException(e.getMessage());
        }
    }
}
