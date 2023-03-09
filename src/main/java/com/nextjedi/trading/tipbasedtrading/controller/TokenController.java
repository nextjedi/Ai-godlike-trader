package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/toke")
public class TokenController {

    Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public void insertToken(@RequestBody String RequestToken){
        logger.info("Inside the post token controller");
        tokenService.insert(RequestToken);
    }
    @GetMapping
    public TokenAccess getTokens(){
        logger.info("Into the get token method");
        return tokenService.getToken();
    }
}
