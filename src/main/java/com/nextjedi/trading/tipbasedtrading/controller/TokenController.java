package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.service.KiteService;
import com.nextjedi.trading.tipbasedtrading.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/token")
@Slf4j
public class TokenController {
    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping
    public List<TokenAccess> getTokens(){
        log.info("Into the get token controller");
        return tokenService.getToken();
    }
    @GetMapping("{userId}")
    public TokenAccess getTokenByUserId(@RequestParam String userId){
        log.info("Into the get token method");
        return tokenService.getLatestTokenByUserId(userId);
    }
}
