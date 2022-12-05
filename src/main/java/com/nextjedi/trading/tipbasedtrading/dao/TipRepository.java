package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.Tip;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class TipRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public void insert (List<Tip> tips){
        entityManager.merge(tips);
    }
}
