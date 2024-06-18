package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.TradeModel;
import com.nextjedi.trading.tipbasedtrading.models.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TradeModelRepository extends JpaRepository<TradeModel, Long>{
    List<TradeModel> findByCreatedAtAfterAndTradeStatusNot(Date date,TradeStatus tradeStatus);
    Optional<TradeModel> findTopByInstrument_InstrumentTokenAndTradeStatusNot(Long instrumentToken,TradeStatus tradeStatus);
    Optional<TradeModel> findTopByEntryOrder_OrderId(String entryOrder_orderId);
    Optional<TradeModel> findTopByExitOrder_OrderId(String exitOrder_orderId);

}
