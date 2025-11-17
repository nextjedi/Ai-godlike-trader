package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByName(String name);

    List<Portfolio> findByUserId(String userId);

    List<Portfolio> findByIsActiveTrue();

    Optional<Portfolio> findByUserIdAndIsActiveTrue(String userId);
}
