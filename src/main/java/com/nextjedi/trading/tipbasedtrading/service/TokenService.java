package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.exception.TokenNotFoundException;
import com.nextjedi.trading.tipbasedtrading.dao.TokenRepository;
import com.nextjedi.trading.tipbasedtrading.util.ApiSecret;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class TokenService {
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private PlayWrightAutomationService playWrightAutomationService;

    public TokenAccess insert(String userId){
        log.info("inside insert token service method");
//        todo move secret to db(encrypting) or vault
        var secret =ApiSecret.apiKeys.get(userId);
        KiteConnect kiteSdk = new KiteConnect(secret.getApiKey());
        try {
            var requestToken =playWrightAutomationService.generateToken(secret.getApiKey(), userId,secret.getPassword(),secret.getTotpKey());
            User user =kiteSdk.generateSession(requestToken, secret.getApiSecret());

            TokenAccess token = new TokenAccess();
            token.setPublicToken(user.publicToken);
            token.setAccessToken(user.accessToken);
            token.setUserId(userId);
            tokenRepository.save(token);
            log.info("Token updated");
            return token;
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
        return getLatestTokenByUserId(userId,false);
    }
    public TokenAccess getLatestTokenByUserId(String userId,boolean forceRefresh){
        log.info("inside getLatestTokenByUserId service method");
        try {
            if(forceRefresh){
                log.info("Refresh the token without checking");
                return insert(userId);
            }
            TokenAccess token =tokenRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
            if(Objects.nonNull(token) && DateUtils.isSameDay(token.getCreatedAt(),new Date())){
                log.info(token.toString());
                log.info("Token found for " + token.getUserId());

                return token;
            }else {
                log.info("generating token");
                return insert(userId);
            }
        }catch (Exception e){
            log.error("token is not available now");
            throw new TokenNotFoundException(e.getMessage());
        }
    }
}
