package com.nextjedi.trading.tipbasedtrading.service;

import com.nextjedi.trading.tipbasedtrading.dao.TipRepository;
import com.nextjedi.trading.tipbasedtrading.models.Tip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TipService {
    @Autowired
    private TipRepository tipRepository;

    List<Tip> tips = new ArrayList<>();
//    insert tip with date
    public void addTips(List<Tip> tps){
        tips.addAll(tps);
    }
//    fetch tip for today's date
    public List<Tip> todayTips(){
        return tips;
    }
}
