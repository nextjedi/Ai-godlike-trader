-- Add indexes for existing trade_model table for better performance

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_trade_model_status ON trade_model(trade_status);
CREATE INDEX IF NOT EXISTS idx_trade_model_created_at ON trade_model(created_at);
CREATE INDEX IF NOT EXISTS idx_trade_model_type ON trade_model(type);
CREATE INDEX IF NOT EXISTS idx_trade_model_user_created ON trade_model(created_by, created_at);

-- Composite index for portfolio queries
CREATE INDEX IF NOT EXISTS idx_trade_model_portfolio_status ON trade_model(created_by, trade_status, created_at);
