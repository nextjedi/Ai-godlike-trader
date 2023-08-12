package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.InstrumentWrapper;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

public interface TokenRepository extends JpaRepository<TokenAccess, Long>{

    public List<TokenAccess> findByUserId(String userId);
}
