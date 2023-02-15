package com.nextjedi.trading.tipbasedtrading.controller;

import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import com.nextjedi.trading.tipbasedtrading.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/toke")
public class TokenController {
    @Autowired
    private TokenService tokenService;

    @PostMapping
    public void insertToken(@RequestBody String RequestToken){
        tokenService.insert(RequestToken);
    }
    @GetMapping
    public TokenAccess getTokens(){
        return tokenService.getToken();
    }
}
