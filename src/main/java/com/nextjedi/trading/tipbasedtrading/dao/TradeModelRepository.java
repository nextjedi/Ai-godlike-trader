package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeModelRepository extends JpaRepository<TradeModel, Long>{
}
