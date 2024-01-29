package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import com.nextjedi.trading.tipbasedtrading.models.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradeModelRepository extends JpaRepository<TradeModel, Long>{
    List<TradeModel> findByTradeStatusNot(TradeStatus tradeStatus);
    Optional<TradeModel> findTopByInstrument_InstrumentTokenAndTradeStatusNot(Long instrumentToken,TradeStatus tradeStatus);
//    public Optional<TradeModel> findTopByInstrument_InstrumentTokenAndTradeStatusNot(Long instrumentToken,TradeStatus tradeStatus);
    Optional<TradeModel> findTopByEntryOrder_OrderId(int entryOrder);
    Optional<TradeModel> findTopByExitOrder_OrderId(int exitOrder);

}
