package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<InstrumentWrapper,Long> {

    public InstrumentWrapper findByTradingsymbol(String tradingsymbol);
}
