package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenRepository extends JpaRepository<TokenAccess, Long>{

    public List<TokenAccess> findByUserId(String userId);
    public TokenAccess findTopByUserIdOrderByCreatedAtDesc(String userId);
}
