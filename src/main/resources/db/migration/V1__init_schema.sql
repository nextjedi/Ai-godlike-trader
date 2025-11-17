-- Initial schema for trading system

-- Portfolios table
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    total_capital DECIMAL(19, 2) NOT NULL,
    available_capital DECIMAL(19, 2) NOT NULL,
    allocated_capital DECIMAL(19, 2) NOT NULL DEFAULT 0,
    utilized_capital DECIMAL(19, 2) NOT NULL DEFAULT 0,
    realized_pnl DECIMAL(19, 2) NOT NULL DEFAULT 0,
    unrealized_pnl DECIMAL(19, 2) NOT NULL DEFAULT 0,
    max_drawdown_percent DECIMAL(5, 2) NOT NULL,
    max_exposure_percent DECIMAL(5, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_portfolios_user_id (user_id),
    INDEX idx_portfolios_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Strategy allocations table
CREATE TABLE IF NOT EXISTS strategy_allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    strategy_id VARCHAR(255) NOT NULL,
    strategy_name VARCHAR(255) NOT NULL,
    allocation_type VARCHAR(50) NOT NULL,
    allocation_value DECIMAL(19, 2) NOT NULL,
    allocated_amount DECIMAL(19, 2) NOT NULL,
    max_position_size DECIMAL(19, 2) NOT NULL,
    current_exposure DECIMAL(19, 2) NOT NULL DEFAULT 0,
    realized_pnl DECIMAL(19, 2) NOT NULL DEFAULT 0,
    unrealized_pnl DECIMAL(19, 2) NOT NULL DEFAULT 0,
    total_trades INT NOT NULL DEFAULT 0,
    winning_trades INT NOT NULL DEFAULT 0,
    losing_trades INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    INDEX idx_strategy_allocations_portfolio (portfolio_id),
    INDEX idx_strategy_allocations_strategy (strategy_id),
    INDEX idx_strategy_allocations_active (is_active),
    UNIQUE KEY uk_portfolio_strategy (portfolio_id, strategy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add indexes for performance
CREATE INDEX idx_portfolios_created_at ON portfolios(created_at);
CREATE INDEX idx_strategy_allocations_created_at ON strategy_allocations(created_at);
