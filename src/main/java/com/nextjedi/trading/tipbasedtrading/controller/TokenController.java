package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.models.TokenDTO;
import com.nextjedi.trading.tipbasedtrading.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/token")
@Slf4j
public class TokenController {
    @Autowired
    private TokenService tokenService;

    @PostMapping
    public void insertToken(@RequestBody TokenDTO requestToken){
        log.info("Inside the post token controller");
        tokenService.insert(requestToken);
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
