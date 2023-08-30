package com.nextjedi.trading.tipbasedtrading.dao;

import com.nextjedi.trading.tipbasedtrading.models.Call;
import com.nextjedi.trading.tipbasedtrading.models.TokenAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CallReposiotry extends JpaRepository<Call, Long>{
}
