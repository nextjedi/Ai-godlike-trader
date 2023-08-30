package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.CallReposiotry;
import com.nextjedi.trading.tipbasedtrading.models.Call;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CallService {
    @Autowired
    private CallReposiotry callReposiotry;

    public List<Call> getAllActiveCalls(){
        return callReposiotry.findAll();
    }
}
