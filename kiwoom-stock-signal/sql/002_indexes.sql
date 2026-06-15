-- =============================================================
-- 조회 성능 인덱스 (계획서 13장)
-- =============================================================

CREATE INDEX IF NOT EXISTS idx_tick_event_code_time
    ON tick_event (stock_code, event_time);

CREATE INDEX IF NOT EXISTS idx_minute_bar_code_interval_time
    ON minute_bar (stock_code, bar_interval, bar_time);

CREATE INDEX IF NOT EXISTS idx_indicator_code_time
    ON indicator_snapshot (stock_code, snapshot_time);

CREATE INDEX IF NOT EXISTS idx_candidate_code_time
    ON candidate_signal (stock_code, signal_time);

CREATE INDEX IF NOT EXISTS idx_buy_signal_code_time
    ON buy_signal_log (stock_code, signal_time);

CREATE INDEX IF NOT EXISTS idx_buy_signal_decision
    ON buy_signal_log (decision);

CREATE INDEX IF NOT EXISTS idx_order_event_code_time
    ON order_event_log (stock_code, event_at);

CREATE INDEX IF NOT EXISTS idx_risk_event_time
    ON risk_event_log (event_at);

CREATE INDEX IF NOT EXISTS idx_system_event_time
    ON system_event_log (event_at);
