package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public void insert (TokenAccess tokenAccess){
        entityManager.merge(tokenAccess);
    }
}
